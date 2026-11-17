package pe.edu.utp.divisas.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import pe.edu.utp.divisas.exception.InvalidConversionException;
import pe.edu.utp.divisas.model.Moneda;

@Service
public class ValidacionService {

    private static final int[] PESOS_RUC = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private final DivisaService divisaService;

    public ValidacionService(DivisaService divisaService) {
        this.divisaService = divisaService;
    }

    public void validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new InvalidConversionException("El monto debe ser mayor que cero.");
        }
    }

    public void validarParMonedas(Moneda origen, Moneda destino) {
        if (origen == null || destino == null) {
            throw new InvalidConversionException("Moneda de origen y destino son obligatorias.");
        }
        divisaService.obtenerDivisa(origen.name());
        divisaService.obtenerDivisa(destino.name());
        if (origen == destino) {
            throw new InvalidConversionException("La moneda de origen y destino deben ser distintas.");
        }
    }

    public void validarRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            throw new InvalidConversionException("El RUC debe tener 11 dígitos numéricos.");
        }
        String prefijo = ruc.substring(0, 2);
        if (!prefijo.equals("10") && !prefijo.equals("15") && !prefijo.equals("17") && !prefijo.equals("20")) {
            throw new InvalidConversionException("El RUC tiene un prefijo no válido.");
        }
        // Digito verificador: suma ponderada de los 10 primeros digitos, modulo 11.
        int suma = 0;
        for (int i = 0; i < PESOS_RUC.length; i++) {
            suma += (ruc.charAt(i) - '0') * PESOS_RUC[i];
        }
        int verificador = (11 - suma % 11) % 10;
        if (verificador != ruc.charAt(10) - '0') {
            throw new InvalidConversionException("El dígito verificador del RUC no es válido.");
        }
    }
}
