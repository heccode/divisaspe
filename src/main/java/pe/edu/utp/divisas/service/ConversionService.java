package pe.edu.utp.divisas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import pe.edu.utp.divisas.exception.InvalidConversionException;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;

@Service
@ConditionalOnExpression("'${conversion.remote.url:}'.isEmpty()")
public class ConversionService implements ConversionPort {

    private static final int ESCALA_RESULTADO = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

    @Override
    public ResultadoConversion convertir(BigDecimal monto, Moneda origen, Moneda destino, TipoCambio tipoCambio) {
        if (monto == null || monto.signum() <= 0) {
            throw new InvalidConversionException("El monto debe ser mayor que cero.");
        }
        if (origen == null || destino == null) {
            throw new InvalidConversionException("Moneda de origen y destino son obligatorias.");
        }
        if (origen == destino) {
            throw new InvalidConversionException("La moneda de origen y destino deben ser distintas.");
        }

        // USD -> PEN usa el tipo de compra; PEN -> USD usa el tipo de venta.
        if (origen == Moneda.USD) {
            BigDecimal resultado = monto.multiply(tipoCambio.compra()).setScale(ESCALA_RESULTADO, REDONDEO);
            return new ResultadoConversion(tipoCambio.compra(), resultado);
        }
        BigDecimal resultado = monto.divide(tipoCambio.venta(), ESCALA_RESULTADO, REDONDEO);
        return new ResultadoConversion(tipoCambio.venta(), resultado);
    }
}
