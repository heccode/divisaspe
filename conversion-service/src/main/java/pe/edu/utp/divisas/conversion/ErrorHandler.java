package pe.edu.utp.divisas.conversion;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.utp.divisas.exception.InvalidConversionException;
import pe.edu.utp.divisas.model.ApiError;

@RestControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).sorted().collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "REQUEST_INVALIDO", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "REQUEST_INVALIDO", "Cuerpo de la solicitud invalido.");
    }

    @ExceptionHandler(InvalidConversionException.class)
    public ResponseEntity<ApiError> conversionInvalida(InvalidConversionException e) {
        return error(HttpStatus.BAD_REQUEST, "CONVERSION_INVALIDA", e.getMessage());
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
