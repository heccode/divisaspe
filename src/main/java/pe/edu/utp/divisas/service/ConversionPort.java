package pe.edu.utp.divisas.service;

import java.math.BigDecimal;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;

public interface ConversionPort {

    ResultadoConversion convertir(BigDecimal monto, Moneda origen, Moneda destino, TipoCambio tipoCambio);
}
