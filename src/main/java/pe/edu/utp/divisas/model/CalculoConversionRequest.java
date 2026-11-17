package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CalculoConversionRequest(
        @NotNull(message = "monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "monto debe ser mayor que cero") BigDecimal monto,
        @NotNull(message = "monedaOrigen es obligatoria") Moneda monedaOrigen,
        @NotNull(message = "monedaDestino es obligatoria") Moneda monedaDestino,
        @NotNull(message = "compra es obligatoria")
        @DecimalMin(value = "0", inclusive = false, message = "compra debe ser mayor que cero") BigDecimal compra,
        @NotNull(message = "venta es obligatoria")
        @DecimalMin(value = "0", inclusive = false, message = "venta debe ser mayor que cero") BigDecimal venta,
        @NotNull(message = "fechaReferencia es obligatoria") LocalDate fechaReferencia) {
}
