package pe.edu.utp.divisas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotizacionResponse(
        BigDecimal montoOriginal,
        Moneda monedaOrigen,
        Moneda monedaDestino,
        BigDecimal tipoCambioAplicado,
        BigDecimal montoEstimado,
        LocalDate fechaReferencia,
        String fuente,
        String mensaje) {

    public static final String MENSAJE_COTIZACION =
            "Cotizacion referencial. No se guarda en el historial ni representa una operacion financiera real.";
}
