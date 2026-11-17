#!/usr/bin/env bash
# Carga .env (lo crea si falta) y genera JWT_SECRET local si no existe. Se usa con: . scripts/entorno.sh
[ -f .env ] || { cp .env.example .env; echo "Se creo .env desde .env.example (solo para uso local)."; }
grep -q '^JWT_SECRET=' .env || { echo "JWT_SECRET=$(openssl rand -hex 32)" >> .env; echo "Se genero JWT_SECRET en .env."; }
set -a; . ./.env; set +a
