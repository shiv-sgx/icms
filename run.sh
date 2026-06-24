#!/usr/bin/env bash
# Build the Maven WAR and deploy it to Tomcat 9 (JDK 11), serving at http://localhost:8080/
# Usage: ./run.sh
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$PROJECT_DIR/../tools"

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@11}"
MVN="${MVN:-$TOOLS_DIR/apache-maven-3.9.9/bin/mvn}"
TOMCAT="${TOMCAT:-$TOOLS_DIR/apache-tomcat-9.0.118}"
export CATALINA_HOME="$TOMCAT"
export CATALINA_PID="$TOMCAT/temp/catalina.pid"
PORT="${PORT:-8080}"

echo ">> Building WAR with Maven..."
"$MVN" -q clean package

echo ">> Deploying target/ROOT.war as Tomcat ROOT context..."
"$TOMCAT/bin/catalina.sh" stop 10 >/dev/null 2>&1 || true
rm -rf "$TOMCAT/webapps/ROOT" "$TOMCAT/webapps/ROOT.war"
# Remove Tomcat's bundled webapps — their context paths (notably /manager and
# /host-manager) would otherwise shadow our app's role namespaces (/manager, ...).
# This dev Tomcat is dedicated to ICMS as the ROOT app.
for app in manager host-manager examples docs; do
    rm -rf "$TOMCAT/webapps/$app"
done
cp "$PROJECT_DIR/target/ROOT.war" "$TOMCAT/webapps/ROOT.war"

echo ">> Starting Tomcat on port $PORT..."
"$TOMCAT/bin/catalina.sh" start
echo ">> App: http://localhost:$PORT/"
