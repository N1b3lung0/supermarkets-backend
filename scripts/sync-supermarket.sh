#!/usr/bin/env bash
# sync-supermarket.sh — triggers a full catalog sync for one or all supermarkets.
#
# Usage:
#   ./scripts/sync-supermarket.sh                       # syncs all 6 supermarkets
#   ./scripts/sync-supermarket.sh mercadona             # syncs only Mercadona
#   ./scripts/sync-supermarket.sh carrefour alcampo     # syncs multiple
#
# Requirements: curl, jq
# The mock-oauth2-server must be running (docker compose up -d mock-oauth2-server)

set -euo pipefail

BASE_URL="${API_BASE_URL:-http://localhost:8080}"
TOKEN_URL="${TOKEN_URL:-http://localhost:9000/default/token}"

# ── Supermarket registry (from V3 migration) ───────────────────────────────────
declare -A SUPERMARKET_IDS=(
  [mercadona]="00000000-0000-0000-0000-000000000001"
  [carrefour]="00000000-0000-0000-0000-000000000002"
  [alcampo]="00000000-0000-0000-0000-000000000003"
  [aldi]="00000000-0000-0000-0000-000000000004"
  [lidl]="00000000-0000-0000-0000-000000000005"
  [dia]="00000000-0000-0000-0000-000000000006"
)

# ── Obtain a JWT from the local mock-oauth2-server ─────────────────────────────
echo "→ Obtaining JWT from ${TOKEN_URL} ..."
TOKEN=$(curl -s -X POST "${TOKEN_URL}" \
  -d "grant_type=client_credentials" \
  | jq -r '.access_token')

if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
  echo "✗ Failed to obtain JWT. Is mock-oauth2-server running?"
  echo "  Run: docker compose up -d mock-oauth2-server"
  exit 1
fi
echo "✓ JWT obtained."

# ── Determine which supermarkets to sync ──────────────────────────────────────
if [[ $# -eq 0 ]]; then
  TARGETS=("${!SUPERMARKET_IDS[@]}")
else
  TARGETS=("$@")
fi

# ── Trigger sync for each target ──────────────────────────────────────────────
for NAME in "${TARGETS[@]}"; do
  NAME_LOWER=$(echo "${NAME}" | tr '[:upper:]' '[:lower:]')
  ID="${SUPERMARKET_IDS[${NAME_LOWER}]:-}"

  if [[ -z "${ID}" ]]; then
    echo "✗ Unknown supermarket: ${NAME_LOWER}. Valid: ${!SUPERMARKET_IDS[*]}"
    continue
  fi

  echo ""
  echo "→ Syncing ${NAME_LOWER} (${ID}) ..."
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "${BASE_URL}/api/v1/sync/supermarkets/${ID}" \
    -H "Authorization: Bearer ${TOKEN}")

  if [[ "${HTTP_STATUS}" == "202" ]]; then
    echo "✓ ${NAME_LOWER}: sync accepted (202). Running in the background."
  else
    echo "✗ ${NAME_LOWER}: unexpected status ${HTTP_STATUS}"
  fi
done

echo ""
echo "Done. Query results with:"
echo "  curl -s '${BASE_URL}/api/v1/compare?q=leche' | jq"

