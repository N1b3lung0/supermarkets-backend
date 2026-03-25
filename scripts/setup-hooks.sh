#!/usr/bin/env bash
# setup-hooks.sh
# Installs a Git pre-commit hook that runs Spotless + Checkstyle before every commit.
# Usage: chmod +x scripts/setup-hooks.sh && ./scripts/setup-hooks.sh

set -euo pipefail

HOOKS_DIR="$(git rev-parse --git-dir)/hooks"
HOOK_FILE="$HOOKS_DIR/pre-commit"

cat > "$HOOK_FILE" << 'EOF'
#!/usr/bin/env bash
# Pre-commit hook: Spotless format check + Checkstyle
# Auto-installed by scripts/setup-hooks.sh

set -euo pipefail

echo "▶ Running Spotless check..."
./gradlew spotlessCheck --quiet || {
  echo ""
  echo "✗ Spotless found formatting issues."
  echo "  Run './gradlew spotlessApply' to fix them automatically, then re-stage and commit."
  exit 1
}

echo "▶ Running Checkstyle..."
./gradlew checkstyleMain checkstyleTest --quiet || {
  echo ""
  echo "✗ Checkstyle violations found. Fix them before committing."
  exit 1
}

echo "✓ Pre-commit checks passed."
EOF

chmod +x "$HOOK_FILE"
echo "✓ Pre-commit hook installed at $HOOK_FILE"

