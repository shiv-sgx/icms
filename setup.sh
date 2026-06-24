#!/usr/bin/env bash
# ============================================================================
# ICMS one-time database setup.
#   1. Compiles the project (to run the BCrypt hash tool with the real library).
#   2. Generates a BCrypt hash for the demo password.
#   3. Creates the schema (schema.sql).
#   4. Injects the hash for the __PWHASH__ token and loads seed data (seed.sql).
#
# All connection settings come from the environment (with safe local defaults).
# Usage:
#   ./setup.sh
#   DB_HOST=db DB_USER=icms DB_PASSWORD=secret DEMO_PASSWORD='S3cret!' ./setup.sh
# ============================================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$PROJECT_DIR/../tools"

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@11}"
MVN="${MVN:-$TOOLS_DIR/apache-maven-3.9.9/bin/mvn}"
JAVA="${JAVA:-$JAVA_HOME/bin/java}"
MYSQL="${MYSQL:-mysql}"

# --- DB connection (env-overridable; NO secrets committed) ------------------
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DEMO_PASSWORD="${DEMO_PASSWORD:-Password@123}"

SCHEMA="$PROJECT_DIR/src/main/resources/db/schema.sql"
SEED="$PROJECT_DIR/src/main/resources/db/seed.sql"

mysql_run() {
    if [ -n "$DB_PASSWORD" ]; then
        "$MYSQL" -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$@"
    else
        "$MYSQL" -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$@"
    fi
}

echo ">> [1/4] Compiling project + resolving classpath..."
"$MVN" -q -DskipTests compile
"$MVN" -q dependency:build-classpath -Dmdep.outputFile="$PROJECT_DIR/target/cp.txt" >/dev/null
CP="$PROJECT_DIR/target/classes:$(cat "$PROJECT_DIR/target/cp.txt")"

echo ">> [2/4] Generating BCrypt hash for the demo password..."
HASH="$("$JAVA" -cp "$CP" com.sgx.icms.tools.PasswordHashTool "$DEMO_PASSWORD")"
if [ -z "$HASH" ]; then
    echo "!! Failed to generate password hash" >&2
    exit 1
fi

echo ">> [3/4] Creating schema (database 'icms')..."
mysql_run < "$SCHEMA"

echo ">> [4/4] Injecting password hash and loading seed data..."
# BCrypt hashes contain '/', '.', '$' but never '|', so '|' is a safe sed delimiter.
sed "s|__PWHASH__|$HASH|g" "$SEED" | mysql_run

cat <<EOF

============================================================
 ICMS database ready.
 Seeded logins (all share the demo password '$DEMO_PASSWORD'):
   admin     (ADMIN)
   manager   (MANAGER)
   agent     (AGENT)
   surveyor  (SURVEYOR)
   customer  (CUSTOMER)
 Next: ./run.sh   then open http://localhost:8080/
============================================================
EOF
