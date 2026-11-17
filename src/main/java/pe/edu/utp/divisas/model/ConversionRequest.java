package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ConversionRequest(
        @NotNull(message = "monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "monto debe ser mayor que cero")
        BigDecimal monto,
        @NotNull(message = "monedaOrigen es obligatoria") Moneda monedaOrigen,
        @NotNull(message = "monedaDestino es obligatoria") Moneda monedaDestino,
        @Positive(message = "usuarioId debe ser positivo") Long usuarioId) {

    public ConversionRequest(BigDecimal monto, Moneda monedaOrigen, Moneda monedaDestino) {
        this(monto, monedaOrigen, monedaDestino, null);
    }
}
