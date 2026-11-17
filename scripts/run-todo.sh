#!/usr/bin/env bash
cd "$(dirname "$0")/.."
. scripts/entorno.sh
export CONVERSION_PORT="${CONVERSION_PORT:-8081}"
export CONVERSION_URL="${CONVERSION_URL:-http://localhost:$CONVERSION_PORT}"
./mvnw -q -f conversion-service/pom.xml spring-boot:run &
S01_PID=$!
trap 'kill $S01_PID 2>/dev/null' EXIT INT TERM
until curl -s -o /dev/null "http://localhost:$CONVERSION_PORT/api/v1/conversion/calcular"; do sleep 1; done
./mvnw -q spring-boot:run
