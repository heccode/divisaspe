package pe.edu.utp.divisas.exception;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.edu.utp.divisas.model.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "REQUEST_INVALIDO", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "REQUEST_INVALIDO",
                "Cuerpo de la solicitud invalido. Revisa los campos y sus valores permitidos (monedas: USD, PEN).");
    }

    @ExceptionHandler(InvalidConversionException.class)
    public ResponseEntity<ApiError> conversionInvalida(InvalidConversionException e) {
        return error(HttpStatus.BAD_REQUEST, "CONVERSION_INVALIDA", e.getMessage());
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiError> usuarioNoEncontrado(UsuarioNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ApiError> emailDuplicado(EmailDuplicadoException e) {
        return error(HttpStatus.CONFLICT, "EMAIL_DUPLICADO", e.getMessage());
    }

    @ExceptionHandler(CuentaBancariaInvalidaException.class)
    public ResponseEntity<ApiError> cuentaInvalida(CuentaBancariaInvalidaException e) {
        return error(HttpStatus.BAD_REQUEST, "CUENTA_BANCARIA_INVALIDA", e.getMessage());
    }

    @ExceptionHandler(CuentaBancariaDuplicadaException.class)
    public ResponseEntity<ApiError> cuentaDuplicada(CuentaBancariaDuplicadaException e) {
        return error(HttpStatus.CONFLICT, "CUENTA_BANCARIA_DUPLICADA", e.getMessage());
    }

    @ExceptionHandler(DatosUsuarioInvalidosException.class)
    public ResponseEntity<ApiError> datosUsuarioInvalidos(DatosUsuarioInvalidosException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_USUARIO_INVALIDOS", e.getMessage());
    }

    @ExceptionHandler(PasswordActualIncorrectaException.class)
    public ResponseEntity<ApiError> passwordActualIncorrecta(PasswordActualIncorrectaException e) {
        return error(HttpStatus.BAD_REQUEST, "PASSWORD_ACTUAL_INCORRECTA", e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(CredencialesInvalidasException e) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", e.getMessage());
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<ApiError> noAutenticado(NoAutenticadoException e) {
        return error(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", e.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ApiError> accesoDenegado(AccesoDenegadoException e) {
        return error(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", e.getMessage());
    }

    @ExceptionHandler(SunatNoIntegradoException.class)
    public ResponseEntity<ApiError> sunatNoIntegrado(SunatNoIntegradoException e) {
        return error(HttpStatus.NOT_IMPLEMENTED, "SUNAT_NO_INTEGRADO", e.getMessage());
    }

    @ExceptionHandler(ServicioConversionNoDisponibleException.class)
    public ResponseEntity<ApiError> conversionNoDisponible(ServicioConversionNoDisponibleException e) {
        log.warn("Servicio de conversion no disponible: {}", e.getMessage());
        return error(HttpStatus.BAD_GATEWAY, "CONVERSION_NO_DISPONIBLE", "No fue posible contactar el servicio de conversion.");
    }

    @ExceptionHandler(BcrpUnavailableException.class)
    public ResponseEntity<ApiError> bcrpNoDisponible(BcrpUnavailableException e) {
        log.warn("BCRPData no disponible: {}", e.getMessage());
        return error(HttpStatus.BAD_GATEWAY, "BCRP_NO_DISPONIBLE", "No fue posible consultar BCRPData.");
    }

    @ExceptionHandler(NoProcessableExchangeRateException.class)
    public ResponseEntity<ApiError> sinDatos(NoProcessableExchangeRateException e) {
        log.warn("Sin tipo de cambio procesable: {}", e.getMessage());
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "TIPO_CAMBIO_NO_PROCESABLE", e.getMessage());
    }

    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<ApiError> baseDatos(Exception e) {
        log.error("Base de datos no disponible o con error", e);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "BASE_DATOS_NO_DISPONIBLE",
                "No fue posible acceder a la base de datos.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "REQUEST_INVALIDO", "El parametro '" + e.getName() + "' no es valido.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> rutaInexistente(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA", "El recurso solicitado no existe.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNoPermitido(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO", "Metodo HTTP no permitido para este recurso.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> tipoNoSoportado(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_NO_SOPORTADO", "El Content-Type debe ser application/json.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        log.error("Error interno no previsto", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno no previsto.");
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(new ApiError(codigo, mensaje));
    }
}
