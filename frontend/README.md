# Frontend DivisaPE

Interfaz en HTML, CSS y JavaScript puro (`fetch`), sin herramientas de compilacion. La parte visual usa Bootstrap 5.3, Bootstrap Icons y la fuente Manrope cargados por CDN (con SRI); el tema propio esta en `styles.css`. Se necesita conexion a Internet para cargar esos recursos.
Todos los datos provienen de los endpoints reales del backend (BCRPData y MySQL). Las conversiones y cotizaciones son referenciales (operacion simulada).

## Ejecucion

1. Levantar MySQL (desde la raiz del repositorio):

   ```bash
   cp .env.example .env          # completar contrasenas
   docker compose up -d
   ```

2. Levantar el backend (`http://localhost:8080`):

   ```bash
   . scripts/entorno.sh   # carga .env y genera JWT_SECRET si falta
   ./mvnw spring-boot:run
   ```

3. Servir el frontend en otra terminal:

   ```bash
   cd frontend
   python3 servidor.py 5500
   ```

4. Abrir `http://localhost:5500`.

`servidor.py` es un servidor estatico minimo (solo libreria estandar de Python) que ademas resuelve las rutas limpias `/iniciar-sesion`, `/registro`, `/cuentas-bancarias`, `/historial` y `/perfil`; `python3 -m http.server` no las entiende.

## Paginas

| Ruta | Archivo | Funcion |
|------|---------|---------|
| `/` | `index.html` + `app.js` | Conversion y tipo de cambio |
| `/historial` | `historial.html` + `historial.js` | Conversiones guardadas del usuario; requiere sesion |
| `/perfil` | `perfil.html` + `perfil.js` | Datos personales (nombre, apellidos, documento, RUC) y cambio de contrasena; requiere sesion |
| `/iniciar-sesion` | `iniciar-sesion.html` + `iniciar-sesion.js` | Entrada con correo y contrasena |
| `/registro` | `registro.html` + `registro.js` | Crear cuenta con contrasena (RUC opcional) |
| `/cuentas-bancarias` | `cuentas.html` + `cuentas.js` | Cuentas bancarias para enviar y recibir dinero (USD/PEN), con modal de alta y de confirmacion; requiere sesion |

`comun.js` reune lo compartido (llamadas a la API, errores, avisos, sesion, el menu desplegable de navegacion y el modal de exito). El menu (`montarMenu`) muestra Convertir, Mi historial, Cuentas bancarias, Mis datos y Cerrar sesion con sesion iniciada, y Convertir, Iniciar sesion y Crear cuenta sin ella; las paginas protegidas llaman a `protegerPagina()` y redirigen al login si no hay sesion. La sesion es el token JWT y el usuario guardados en `localStorage` (`divisape.token`, `divisape.usuario`); `api()` envia `Authorization: Bearer` en cada llamada. Al abrir `/` se revalida con `GET /api/v1/auth/me`; si responde 401 (token vencido) o 404, se cierra la sesion.

`config.js` define la URL del backend (`window.DIVISAPE_API`, por defecto `http://localhost:8080`). En el contenedor (`Dockerfile`) `entrypoint.sh` lo regenera con la variable `API_URL`.

## Recorrido de demostracion

| Paso | Seccion | Accion | Endpoint |
|------|---------|--------|----------|
| 1 | Tipo de cambio | Se consulta al abrir; boton "Actualizar" | `GET /api/v1/tipo-cambio` |
| 2 | `/registro` | Crear cuenta (RUC opcional); deja la sesion iniciada | `POST /api/v1/auth/registro` |
| 3 | `/iniciar-sesion` | Entrar con correo y contrasena | `POST /api/v1/auth/login` |
| 4 | Conversion | "Solo cotizar" (no persiste) | `POST /api/v1/cotizaciones/referencial` |
| 5 | Conversion | "Solo convertir" (no persiste) | `POST /api/v1/conversiones/referencial` |
| 6 | Conversion | "Convertir y guardar" (requiere sesion iniciada) | `POST /api/v1/conversiones/referencial` con `usuarioId` |
| 7 | `/historial` | Lista las conversiones guardadas; boton "Actualizar" | `GET /api/v1/usuarios/{id}/historial` |
| 8 | `/perfil` | Guardar datos personales y cambiar la contrasena | `PUT /api/v1/usuarios/{id}`, `PUT /api/v1/auth/password` |

Al inicio tambien se consulta `GET /api/v1/divisas` (catalogo USD/PEN) para llenar los selectores de moneda.

Para comprobar que la cotizacion y la conversion sin registrar no persisten: hacer ambas y verificar que el historial no cambia; solo "Convertir y guardar" agrega una fila.

## Validaciones y errores

- En el navegador: monto mayor que cero, monedas distintas, nombre y email validos, RUC de 11 digitos.
- Los errores del backend se muestran en lenguaje claro: los tecnicos (`BCRP_NO_DISPONIBLE`, `CONVERSION_NO_DISPONIBLE`, `BASE_DATOS_NO_DISPONIBLE`, `ERROR_INTERNO`) se traducen en `MENSAJES_ERROR` de `comun.js`; el resto usa el `mensaje` del backend.
- Si el backend esta apagado se muestra "No pudimos conectar con el servidor".
- BCRPData puede fallar de forma intermitente: el frontend muestra el error y se puede reintentar.

## Notas

- El backend permite CORS segun `CORS_ORIGENES` (por defecto `localhost` y `127.0.0.1`, ver `seguridad/SecurityConfig.java`).
- Si el backend usa otro puerto o dominio, cambiar `config.js` (o `API_URL` en el contenedor).
- El token vive en `localStorage`: cerrar sesion solo lo borra en este navegador.
- Despliegue: ver `docs/DESPLIEGUE_RAILWAY.md`.
