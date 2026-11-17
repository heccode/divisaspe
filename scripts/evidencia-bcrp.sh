#!/usr/bin/env bash
URL="https://estadisticas.bcrp.gob.pe/estadisticas/series/api/PD04639PD-PD04640PD/json"
OUT="$(dirname "$0")/../evidencia-bcrp.txt"
echo "\$ curl -i \"$URL\""
curl -i "$URL" | tee "$OUT"
echo
echo "Respuesta guardada en: $OUT"
