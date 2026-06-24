#!/usr/bin/env bash
# ============================================================================
# ICMS — MySQL dump restore
# Recreates the `icms` database from a dump produced by dump-db.sh.
# The dump includes DROP DATABASE + CREATE DATABASE, so this is destructive
# for the target schema. Config is environment-driven (12-factor).
#
#   DB_HOST     (default: 127.0.0.1)
#   DB_PORT     (default: 3306)
#   DB_USER     (default: root)
#   DB_PASSWORD (default: empty)
#   IN_FILE     (default: <script dir>/icms-dump.sql)
#
# Usage:
#   DB_PASSWORD=secret ./restore-db.sh
#   DB_HOST=db.staging IN_FILE=/backups/icms-2026-06-24.sql ./restore-db.sh
# ============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
IN_FILE="${IN_FILE:-$SCRIPT_DIR/icms-dump.sql}"

if [[ ! -f "$IN_FILE" ]]; then
  echo "[restore-db] ERROR: dump file not found: $IN_FILE" >&2
  exit 1
fi

export MYSQL_PWD="$DB_PASSWORD"

echo "[restore-db] Restoring $IN_FILE into $DB_HOST:$DB_PORT as '$DB_USER' (DESTRUCTIVE)"

mysql \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  --default-character-set=utf8mb4 < "$IN_FILE"

echo "[restore-db] Done."
