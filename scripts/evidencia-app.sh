#!/usr/bin/env bash
BASE="${BASE:-http://localhost:8080}"
set -x
curl -i "$BASE/api/v1/tipo-cambio"
echo
curl -i -X POST "$BASE/api/v1/conversiones/referencial" \
  -H "Content-Type: application/json" \
  -d '{"monto":100,"monedaOrigen":"USD","monedaDestino":"PEN"}'
echo
