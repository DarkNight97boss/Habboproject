#!/bin/sh
# backup.sh — esegue mysqldump full + compresso, ruota i backup vecchi.
#
# Chiamato dal servizio "backup" in docker-compose.yml (profilo "backup").
# Volume `backup_data` esposto a `/backups` nel container — montalo su host
# per portarli fuori (es. rsync a S3/Backblaze ogni mattina).
#
# Retention: 14 giorni (modifica DAYS sotto).

set -eu

DB_HOST="db"
DB_USER="root"
DB_PWD="${MARIADB_ROOT_PASSWORD}"
DB_NAME="${DB_NAME:-ms}"
OUT_DIR="/backups"
DAYS="14"
TS=$(date -u +"%Y%m%dT%H%M%SZ")
FILE="$OUT_DIR/${DB_NAME}_${TS}.sql.gz"

mkdir -p "$OUT_DIR"

echo "[backup] dump $DB_NAME -> $FILE"
mariadb-dump \
    --host="$DB_HOST" \
    --user="$DB_USER" \
    --password="$DB_PWD" \
    --single-transaction \
    --quick \
    --routines \
    --triggers \
    --events \
    --hex-blob \
    --default-character-set=utf8mb4 \
    "$DB_NAME" | gzip -9 > "$FILE"

echo "[backup] done: $(du -h "$FILE" | cut -f1)"

# Retention
find "$OUT_DIR" -type f -name "${DB_NAME}_*.sql.gz" -mtime "+$DAYS" -print -delete
