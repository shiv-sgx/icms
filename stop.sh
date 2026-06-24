#!/usr/bin/env bash
# Stop the Tomcat instance running the Basic Struts2 app.
# Usage: ./stop.sh
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@11}"
TOMCAT="${TOMCAT:-$PROJECT_DIR/../tools/apache-tomcat-9.0.118}"
export CATALINA_HOME="$TOMCAT"
export CATALINA_PID="$TOMCAT/temp/catalina.pid"

"$TOMCAT/bin/catalina.sh" stop 10 -force || true
echo ">> Tomcat stopped."
