#!/bin/bash
# Seed del DB di load test (usa e getta): BaseDB 3.5.5 + sqlupdates non gia'
# inclusi nella base + migrations CMS. Riusa la logica di apply-sql.sh.
set -euo pipefail
DB="${MARIADB_DATABASE:-ms}"
M() { mariadb --protocol=socket -u root -p"${MARIADB_ROOT_PASSWORD}" -D "$DB" "$@"; }
log() { echo "[seed] $*"; }

BASE="/seed/base/BaseDB MS 3.5.5.sql"
MANIFEST="/seed/manifest/INCLUDED-IN-BASE.txt"

log "import BaseDB"; M < "$BASE"
M -e "CREATE TABLE IF NOT EXISTS schema_migrations (filename VARCHAR(255) PRIMARY KEY, applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, checksum VARCHAR(64) NULL);"

in_base() { [ -f "$MANIFEST" ] && grep -v '^#' "$MANIFEST" | grep -Fxq -- "$1"; }
apply_dir() { # dir  skip_base(0|1)
  local d="$1" skip="$2" f b
  [ -d "$d" ] || return 0
  while IFS= read -r f; do
    b="$(basename "$f")"
    if [ "$skip" = "1" ] && in_base "$b"; then continue; fi
    log "apply $b"; M < "$f" || log "WARN $b non applicato (continuo)"
  done < <(find "$d" -maxdepth 1 -type f -name '*.sql' | LC_ALL=C sort)
}
apply_dir /seed/emu 1
apply_dir /seed/cms 0
log "fatto: $(M -Nse 'SELECT COUNT(*) FROM users') utenti, seed completo"
