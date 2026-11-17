#!/usr/bin/env bash
# Inicia MySQL (Docker), backend (8080) y frontend (5500). Ctrl+C detiene backend y frontend.
cd "$(dirname "$0")/.."
. scripts/entorno.sh

docker compose up -d mysql
echo "Esperando MySQL..."
until docker exec divisape-mysql mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null; do sleep 2; done

(cd frontend && python3 servidor.py 5500 >/dev/null 2>&1) &
FRONT_PID=$!
trap 'kill $FRONT_PID 2>/dev/null' EXIT INT TERM
echo "Frontend: http://localhost:5500   Backend: http://localhost:8080"
./mvnw spring-boot:run
