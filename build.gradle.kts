plugins {
    java
    checkstyle
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependency)
    alias(libs.plugins.spotless)
}

group = "com.n1b3lung0"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion =
            JavaLanguageVersion.of(
                libs.versions.java
                    .get()
                    .toInt(),
            )
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Web + Validation + Actuator
    implementation(libs.bundles.spring.web)

    // JPA + Flyway
    implementation(libs.bundles.spring.data)
    runtimeOnly(libs.postgresql)

    // OpenAPI / Swagger
    implementation(libs.springdoc.openapi)

    // ShedLock — distributed scheduler lock
    implementation(libs.shedlock.spring)
    implementation(libs.shedlock.jdbc)

    // Observability
    implementation(libs.bundles.observability)

    // Tooling
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.mapstruct)

    // Dev tools
    developmentOnly(libs.spring.boot.devtools)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens",
        "java.base/java.lang=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.util=ALL-UNNAMED",
        "--add-opens",
        "java.base/java.lang.reflect=ALL-UNNAMED",
        // Allow sun.misc.Unsafe usage (ArchUnit 1.3.0 + Mockito byte-buddy internals)
        // and suppress the "terminally deprecated" JVM warning on Java 21+
        "--sun-misc-unsafe-memory-access=allow",
        // Allow dynamic agent loading (Mockito byte-buddy agent)
        "-XX:+EnableDynamicAgentLoading",
    )
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    workingDir = rootProject.projectDir
    jvmArgs(
        "--sun-misc-unsafe-memory-access=allow",
        "-XX:+EnableDynamicAgentLoading",
    )
}

spotless {
    java {
        googleJavaFormat("1.27.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
    }
}

// google-java-format requires access to internal JDK APIs on Java 17+
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        ),
    )
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}
