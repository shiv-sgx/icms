#!/usr/bin/env bash
# ============================================================================
# ICMS — MySQL dump generator
# Produces a portable, self-contained SQL dump (schema + data + routines)
# that can recreate the `icms` database on any MySQL 8.x / 9.x server.
#
# Config is environment-driven (12-factor). NEVER hardcode credentials.
#   DB_HOST     (default: 127.0.0.1)
#   DB_PORT     (default: 3306)
#   DB_NAME     (default: icms)
#   DB_USER     (default: root)
#   DB_PASSWORD (default: empty — prompt-free only if server allows)
#   OUT_FILE    (default: <script dir>/icms-dump.sql)
#
# Usage:
#   DB_PASSWORD=secret ./dump-db.sh
#   DB_HOST=db.prod DB_USER=backup OUT_FILE=/backups/icms-$(date +%F).sql ./dump-db.sh
# ============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-icms}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
OUT_FILE="${OUT_FILE:-$SCRIPT_DIR/icms-dump.sql}"

# Pass the password via env (MYSQL_PWD) instead of the command line so it never
# shows up in the process list / shell history.
export MYSQL_PWD="$DB_PASSWORD"

echo "[dump-db] Dumping '$DB_NAME' from $DB_HOST:$DB_PORT as '$DB_USER' -> $OUT_FILE"

mysqldump \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  --databases "$DB_NAME" \
  --single-transaction \
  --routines --triggers --events \
  --add-drop-database \
  --set-gtid-purged=OFF \
  --default-character-set=utf8mb4 \
  --result-file="$OUT_FILE"

echo "[dump-db] Done. $(wc -l < "$OUT_FILE") lines, $(du -h "$OUT_FILE" | cut -f1)."
