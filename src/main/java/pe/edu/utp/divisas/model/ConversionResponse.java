package pe.edu.utp.divisas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConversionResponse(
        BigDecimal montoOriginal,
        Moneda monedaOrigen,
        Moneda monedaDestino,
        BigDecimal tipoCambioAplicado,
        BigDecimal resultado,
        LocalDate fechaReferencia,
        String fuente,
        String mensaje,
        Long operacionId) {

    public static final String MENSAJE_REFERENCIAL =
            "Conversion referencial. No representa una operacion financiera real.";

    public ConversionResponse(BigDecimal montoOriginal, Moneda monedaOrigen, Moneda monedaDestino,
                              BigDecimal tipoCambioAplicado, BigDecimal resultado, LocalDate fechaReferencia,
                              String fuente, String mensaje) {
        this(montoOriginal, monedaOrigen, monedaDestino, tipoCambioAplicado, resultado,
                fechaReferencia, fuente, mensaje, null);
    }

    public ConversionResponse conOperacionId(Long id) {
        return new ConversionResponse(montoOriginal, monedaOrigen, monedaDestino, tipoCambioAplicado,
                resultado, fechaReferencia, fuente, mensaje, id);
    }
}
