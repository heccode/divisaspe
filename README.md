# DivisaPE

Backend Spring Boot (Java 17, Apache Camel embebido, Spring Data JPA + MySQL) y frontend estatico en `frontend/`.
Las conversiones son referenciales (operacion simulada).

## Inicio rapido

```bash
./scripts/iniciar.sh     # MySQL (Docker) + backend :8080 + frontend :5500
```

Abrir `http://localhost:5500`. Requiere Docker, Java 17 y Python 3.

## Ejecucion local

```bash
cp .env.example .env          # completar contrasenas
docker compose up -d          # MySQL local (contenedor divisape-mysql)
set -a; source .env; set +a   # exporta DB_URL, DB_USER, DB_PASSWORD
./mvnw spring-boot:run        # backend en http://localhost:8080
```

Frontend: ver `frontend/README.md`.

## Notas operativas

- La consulta a BCRPData tiene timeouts cortos (3 s conexion, 6 s lectura) y hasta 3 intentos; si falla, la API responde 502 `BCRP_NO_DISPONIBLE`.
- El backend necesita MySQL arriba para iniciar.
- La consulta de RUC en SUNAT no esta integrada (`docs/LIMITACION_SUNAT.md`): el registro funciona con o sin RUC, que solo se valida por formato.
- S01 puede ejecutarse como proceso aparte: `./scripts/run-todo.sh`.
