package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CotizacionRequest(
        @NotNull(message = "monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "monto debe ser mayor que cero")
        BigDecimal monto,
        @NotNull(message = "monedaOrigen es obligatoria") Moneda monedaOrigen,
        @NotNull(message = "monedaDestino es obligatoria") Moneda monedaDestino) {
}
