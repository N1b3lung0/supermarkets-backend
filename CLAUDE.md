# CLAUDE.md

Guía de referencia para el desarrollo de este proyecto. Todas las convenciones, patrones y reglas que debe seguir el asistente.

---

## Índice de secciones

| # | Archivo | Contenido |
|---|---|---|
| 01 | [01-rol-stack-estructura.md](docs/claude/01-rol-stack-estructura.md) | Rol y Contexto · Stack Tecnológico · Estructura de Paquetes · Version Catalogs |
| 02 | [02-ddd-modelo-dominio.md](docs/claude/02-ddd-modelo-dominio.md) | Lenguaje Ubicuo · Entidades, Aggregates y Value Objects · Factory Methods · Domain Events · Reglas de Oro de Aggregates |
| 03 | [03-cqrs-transacciones-excepciones.md](docs/claude/03-cqrs-transacciones-excepciones.md) | CQRS Command/Query · Gestión de Transacciones · Jerarquía de Excepciones · GlobalExceptionHandler |
| 04 | [04-persistencia-jpa-patrones.md](docs/claude/04-persistencia-jpa-patrones.md) | Flyway · N+1 y Fetch Strategy · Auditoría · Optimistic Locking · Soft Delete · Registro de Beans |
| 05 | [05-api-observabilidad-configuracion.md](docs/claude/05-api-observabilidad-configuracion.md) | Convenciones REST · OpenAPI/Swagger · Observabilidad · Configuración Externalizada (12-Factor) |
| 06 | [06-seguridad.md](docs/claude/06-seguridad.md) | Spring Security sin contaminar el dominio · Secrets en CI y local · OWASP Dependency Check · Trivy |
| 07 | [07-testing-nomenclatura.md](docs/claude/07-testing-nomenclatura.md) | Nomenclatura · Pirámide de Tests · ObjectMother · Spotless · Checkstyle · ArchUnit |
| 08 | [08-git-cicd-releases.md](docs/claude/08-git-cicd-releases.md) | Conventional Commits · Pre-commit Hooks · Branch Strategy · Branch Protection · Pipeline CI · Dependabot · Releases |
| 09 | [09-dockerfile-despliegue-idempotencia.md](docs/claude/09-dockerfile-despliegue-idempotencia.md) | Dockerfile multi-stage · Estrategia de Despliegue · Zero-downtime · Rollback · Idempotencia |
| 10 | [10-nunca-y-respuestas.md](docs/claude/10-nunca-y-respuestas.md) | Lo que NUNCA se debe hacer · Cómo trabajar con Claude |

---

## Reglas absolutas (resumen ejecutivo)

Las reglas más importantes, siempre presentes sin necesidad de navegar a los archivos:

1. **El dominio no tiene dependencias de Spring, JPA ni de ninguna otra capa.** Solo Java puro.
2. **`@Transactional` solo en infraestructura** (JPA Adapter o decorador transaccional). Nunca en `domain/` ni `application/`.
3. **Capturar excepciones por categoría base** en `GlobalExceptionHandler`, nunca por excepción concreta.
4. **`create()` estático para negocio, constructor de paquete para reconstitución** desde persistencia.
5. **Publicar Domain Events siempre después de `repository.save()`**, nunca antes.
6. **Un Aggregate por transacción.** Coordinación entre Aggregates mediante Domain Events.
7. **Todo `LAZY` en JPA.** `@EntityGraph` explícito solo cuando un caso de uso concreto lo necesite.
8. **Nunca versiones hardcodeadas** en `build.gradle.kts` — todo en `gradle/libs.versions.toml`.
9. **`SecurityContextHolder` solo en `infrastructure/`**, nunca en dominio ni aplicación.
10. **Todos los endpoints que crean recursos llevan `Idempotency-Key`** y protección contra duplicados.

