package pe.edu.utp.divisas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TipoCambio(LocalDate fechaReferencia, BigDecimal compra, BigDecimal venta, String fuente) {
}
