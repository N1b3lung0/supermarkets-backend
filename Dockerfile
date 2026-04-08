# =============================================================================
# Stage 1 — builder
# Compiles the application and produces the executable fat JAR.
# =============================================================================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace

# Copy dependency manifests first for better layer caching:
# these layers are only invalidated when build files change, not source files.
COPY gradle/ gradle/
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

# Download dependencies (cached layer — only refreshed when build files change)
RUN ./gradlew dependencies --no-daemon -q

# Copy source and produce the executable JAR
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test -x spotlessCheck -x checkstyleMain

# =============================================================================
# Stage 2 — runtime
# Minimal JRE image, non-root user, only the fat JAR.
# =============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime

# Non-root user for security
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Transfer ownership to the non-root user
RUN chown appuser:appgroup app.jar
USER appuser

# Expose the default application port
EXPOSE 8080

# Health check — polls the Spring Boot Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# JVM flags:
#   UseContainerSupport — respects Docker CPU/memory limits
#   EnableDynamicAgentLoading — required for Micrometer/Mockito on JDK 21+
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:+EnableDynamicAgentLoading", \
    "--sun-misc-unsafe-memory-access=allow", \
    "-jar", "app.jar"]

