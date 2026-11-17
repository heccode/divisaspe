package pe.edu.utp.divisas.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OperacionResponse(
        Long id,
        Long usuarioId,
        BigDecimal monto,
        Moneda monedaOrigen,
        Moneda monedaDestino,
        BigDecimal tipoCambioAplicado,
        BigDecimal resultado,
        LocalDate fechaReferencia,
        String fuente,
        LocalDateTime fechaOperacion) {
}
