#!/bin/sh
: "${API_URL:?Define API_URL con la URL publica del backend}"
printf 'window.DIVISAPE_API = "%s";\n' "${API_URL%/}" > config.js
exec python3 servidor.py "${PORT:-8080}"
