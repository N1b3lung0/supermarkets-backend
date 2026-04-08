## ¿Qué hace este PR?

<!-- Descripción concisa del cambio. Una o dos frases. -->

## Motivación / contexto

<!-- Explica el POR QUÉ, no el qué — el diff ya muestra el qué. -->
<!-- Closes #XX -->

## Tipo de cambio

- [ ] `feat` — nueva funcionalidad de negocio
- [ ] `fix` — corrección de bug
- [ ] `refactor` — sin cambio de comportamiento observable
- [ ] `test` — añadir o corregir tests
- [ ] `chore` — mantenimiento, dependencias, CI/CD
- [ ] `docs` — solo documentación
- [ ] `perf` — mejora de rendimiento

## Checklist

### Código
- [ ] Sin anotaciones Spring (`@Service`, `@Transactional`, `@Value`…) en `domain/` ni `application/`
- [ ] Nuevos Aggregates usan `create()` para negocio y `reconstitute()` para persistencia
- [ ] Excepciones concretas extienden la categoría base correcta (`NotFoundException`, `ConflictException`…)
- [ ] `GlobalExceptionHandler` NO tiene nuevos handlers para excepciones concretas
- [ ] Todas las asociaciones JPA son `FetchType.LAZY`
- [ ] Versiones de dependencias declaradas en `gradle/libs.versions.toml` (nada en `build.gradle.kts`)

### Tests
- [ ] Tests añadidos o actualizados para el cambio
- [ ] ArchUnit sigue pasando: `./gradlew test --tests "*ArchitectureTest"`
- [ ] Tests de dominio no usan mocks (solo Java puro)

### Persistencia (si aplica)
- [ ] Nueva migración Flyway añadida (`V{n}__descripcion.sql`)
- [ ] Migración es idempotente y nunca modifica una ya aplicada

### API (si aplica)
- [ ] Endpoints de creación/mutación llevan `Idempotency-Key` header
- [ ] Anotaciones OpenAPI (`@Tag`, `@Operation`, `@ApiResponse`) añadidas al controller
- [ ] `.env.example` actualizado si hay nuevas variables de entorno

### CI
- [ ] `./gradlew spotlessApply` ejecutado (o el pre-commit hook lo hizo)
- [ ] `./gradlew spotlessCheck checkstyleMain` pasa en local

## Notas para el revisor

<!-- Contexto adicional, decisiones de diseño, alternativas consideradas. -->

