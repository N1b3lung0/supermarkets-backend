# 08 — Git, CI/CD y Releases

> [← Índice](../../CLAUDE.md)

---

## Conventional Commits: formato de mensajes

Todo commit sigue la especificación [Conventional Commits](https://www.conventionalcommits.org).

### Formato

```
<tipo>(<scope>): <descripción en imperativo, minúsculas, sin punto final>

[cuerpo opcional — explica el POR QUÉ, no el qué]

[footer opcional — referencias a issues, breaking changes]
```

### Tipos permitidos

| Tipo | Cuándo usarlo |
|---|---|
| `feat` | Nueva funcionalidad de negocio |
| `fix` | Corrección de bug |
| `refactor` | Cambio de código sin modificar comportamiento |
| `test` | Añadir o corregir tests |
| `docs` | Solo documentación |
| `chore` | Tareas de mantenimiento (deps, config, CI) |
| `perf` | Mejora de rendimiento |
| `revert` | Revertir un commit anterior |

### Scope: nombre de la feature afectada

```bash
feat(order): add cancellation by customer request
fix(product): correct stock calculation on concurrent updates
refactor(shared): extract Money value object from Order
test(customer): add ObjectMother for CustomerAggregate
chore(deps): bump Spring Boot to 4.0.3
```

### Breaking changes

```bash
feat(order)!: replace OrderId Long with UUID

BREAKING CHANGE: OrderId is now UUID. Migrate existing data with V5__migrate_order_ids.sql
```

### Reglas

- Descripción en **imperativo** y **minúsculas**: `add`, `fix`, `remove` — nunca `Added`, `Fixes`.
- Máximo **72 caracteres** en la primera línea.
- El cuerpo explica **por qué**, no qué — el diff ya muestra el qué.
- Un commit = un cambio cohesionado. No mezclar feat + refactor.

---

## Pre-commit Hooks

```bash
# scripts/setup-hooks.sh  — ejecutar una vez tras clonar el repo
#!/bin/bash
set -e

HOOKS_DIR=".git/hooks"
PRE_COMMIT="$HOOKS_DIR/pre-commit"

cat > "$PRE_COMMIT" << 'EOF'
#!/bin/bash
echo "▶ Running pre-commit checks..."

echo "  → Spotless format check"
./gradlew spotlessCheck --daemon -q
if [ $? -ne 0 ]; then
  echo "  ✗ Format errors found. Run: ./gradlew spotlessApply"
  exit 1
fi

echo "  → Checkstyle"
./gradlew checkstyleMain --daemon -q
if [ $? -ne 0 ]; then
  echo "  ✗ Checkstyle violations found."
  exit 1
fi

echo "  ✓ Pre-commit checks passed"
EOF

chmod +x "$PRE_COMMIT"
echo "✓ Git hooks installed"
```

```bash
chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh
```

### Reglas

- El hook usa `--daemon` para reutilizar el proceso Gradle (~3-5s).
- **Nunca** ejecutar los tests completos en el hook — son responsabilidad del CI.
- Si hay errores de formato, el hook muestra: `./gradlew spotlessApply`.

---

## Git: Branch Strategy

**Trunk-based development simplificado**: ramas de feature cortas que se mergean a `main` frecuentemente.

### Ramas permanentes

| Rama | Propósito |
|---|---|
| `main` | Siempre desplegable a producción. Protegida. |
| `develop` | Integración continua. Base para features. |

### Ramas temporales (vida máxima: 2 días)

```
feature/<issue-id>-<descripcion-kebab-case>
fix/<issue-id>-<descripcion-kebab-case>
refactor/<descripcion-kebab-case>
chore/<descripcion-kebab-case>
hotfix/<issue-id>-<descripcion-kebab-case>   ← sale de main, mergea a main y develop
```

**Ejemplos:**
```
feature/42-order-cancellation
fix/87-concurrent-stock-update
hotfix/103-payment-timeout-production
```

### Flujos

```
# Normal
develop → feature/X → PR → CI verde → merge a develop → PR → CI verde → merge a main → deploy

# Hotfix
main → hotfix/X → PR → CI verde → merge a main → deploy → cherry-pick a develop
```

### Reglas

- Las ramas de feature nunca duran más de 2 días.
- `main` **nunca** recibe push directo. Todo pasa por PR.
- El nombre de la rama referencia siempre el issue cuando existe.

---

## Git: Branch Protection y PR Template

### Branch protection rules (GitHub Settings → Branches)

Configurar en `main` y `develop`:

```
✅ Require a pull request before merging
   ✅ Require approvals: 0  (solo dev — el CI es suficiente guardia)

✅ Require status checks to pass before merging
   - Format check (spotlessCheck)
   - Style check (checkstyle)
   - Unit tests
   - Architecture tests
   - Integration tests

✅ Require branches to be up to date before merging
✅ Do not allow bypassing the above settings
✅ Restrict who can push to matching branches → solo tú
```

### PR Template

```markdown
<!-- .github/pull_request_template.md -->

## ¿Qué hace este PR?

## Motivación / contexto
<!-- Closes #XX -->

## Tipo de cambio
- [ ] `feat` — nueva funcionalidad
- [ ] `fix` — corrección de bug
- [ ] `refactor` — sin cambio de comportamiento
- [ ] `chore` — mantenimiento, dependencias, CI
- [ ] `docs` — solo documentación

## Checklist
- [ ] Tests añadidos o actualizados
- [ ] ArchUnit sigue pasando (`./gradlew test --tests "*ArchitectureTest"`)
- [ ] Sin anotaciones Spring en `domain/` ni `application/`
- [ ] Migraciones Flyway añadidas si hay cambios de esquema
- [ ] `.env.example` actualizado si hay nuevas variables de entorno

## Notas para el revisor
```

---

## Pipeline CI: Checks Obligatorios antes de Merge

```
1. ./gradlew spotlessCheck
2. ./gradlew checkstyleMain
3. ./gradlew test --tests "..domain..*"
4. ./gradlew test --tests "..application..*"
5. ./gradlew test --tests "..infrastructure..*"
6. ./gradlew test --tests "*ArchitectureTest"
```

### Reglas del pipeline

- Los pasos 1–4 deben completarse en **menos de 2 minutos**.
- Testcontainers en paso 5 puede reutilizar el contenedor (`reuse = true` en local).
- **El build de `main` nunca puede estar en rojo.**
- La cobertura no es una métrica de CI — ArchUnit y la pirámide son la garantía real.

### Workflow GitHub Actions

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}

permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: gradle/actions/setup-gradle@v4
        with:
          java-version: '25'
          distribution: 'temurin'
          gradle-version: '9.4.0'
          cache-encryption-key: ${{ secrets.GRADLE_CACHE_ENCRYPTION_KEY }}

      - name: Format check
        run: ./gradlew spotlessCheck

      - name: Style check
        run: ./gradlew checkstyleMain

      - name: Unit tests — domain + application
        run: ./gradlew test --tests "..domain..*" --tests "..application..*"

      - name: Architecture tests
        run: ./gradlew test --tests "*ArchitectureTest"

      - name: Integration & E2E tests
        run: ./gradlew test --tests "..infrastructure..*"
        env:
          TESTCONTAINERS_REUSE_ENABLE: true
          TEST_DATABASE_URL: ${{ secrets.TEST_DATABASE_URL }}
          PAYMENT_API_KEY: ${{ secrets.PAYMENT_API_KEY }}

      - name: Upload test results on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: build/reports/tests/
          retention-days: 7
```

---

## Dependabot: actualización automática de dependencias

```yaml
# .github/dependabot.yml
version: 2

updates:
  - package-ecosystem: gradle
    directory: /
    schedule:
      interval: weekly
      day: monday
      time: "08:00"
      timezone: "Europe/Madrid"
    open-pull-requests-limit: 5
    groups:
      spring:
        patterns: ["org.springframework*", "io.spring*"]
      testing:
        patterns: ["org.junit*", "org.mockito*", "org.testcontainers*", "com.tngtech*"]
    labels:
      - dependencies
      - gradle

  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
      day: monday
      time: "08:00"
      timezone: "Europe/Madrid"
    labels:
      - dependencies
      - github-actions
```

### Reglas para PRs de Dependabot

- Deben pasar el pipeline CI completo antes de mergear.
- **Patch y minor** se pueden mergear sin revisión si el CI pasa.
- **Major** requiere revisión manual — puede haber breaking changes.
- Si un PR lleva más de 2 semanas sin mergear, revisar si hay un motivo bloqueante.

---

## Versionado Semántico y Release

### Estrategia: tags de Git + CHANGELOG automático

| Incremento | Cuándo |
|---|---|
| `PATCH` | `fix:` commits desde el último release |
| `MINOR` | Al menos un `feat:` commit |
| `MAJOR` | Al menos un `BREAKING CHANGE` |

```bash
git checkout main && git pull
git tag -a v1.2.0 -m "chore(release): v1.2.0"
git push origin v1.2.0
```

### Workflow de release automático

```yaml
# .github/workflows/release.yml
name: Release

on:
  push:
    tags: ['v*.*.*']

permissions:
  contents: write
  packages: write

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: gradle/actions/setup-gradle@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Run full CI
        run: ./gradlew spotlessCheck checkstyleMain test

      - name: Build Docker image
        run: |
          docker build -t ghcr.io/${{ github.repository }}:${{ github.ref_name }} .
          docker build -t ghcr.io/${{ github.repository }}:latest .

      - name: Push Docker image to GHCR
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}:${{ github.ref_name }}
          docker push ghcr.io/${{ github.repository }}:latest

      - name: Generate CHANGELOG and create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true
          token: ${{ secrets.GITHUB_TOKEN }}
```

