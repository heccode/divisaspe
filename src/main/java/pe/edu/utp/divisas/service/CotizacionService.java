package pe.edu.utp.divisas.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.divisas.integration.camel.TipoCambioProvider;
import pe.edu.utp.divisas.model.CotizacionRequest;
import pe.edu.utp.divisas.model.CotizacionResponse;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;

@Service
public class CotizacionService {

    private final ValidacionService validacionService;
    private final TipoCambioProvider tipoCambioProvider;
    private final ConversionPort conversionService;

    public CotizacionService(ValidacionService validacionService, TipoCambioProvider tipoCambioProvider,
                             ConversionPort conversionService) {
        this.validacionService = validacionService;
        this.tipoCambioProvider = tipoCambioProvider;
        this.conversionService = conversionService;
    }

    public CotizacionResponse generarCotizacion(CotizacionRequest request) {
        validacionService.validarMonto(request.monto());
        validacionService.validarParMonedas(request.monedaOrigen(), request.monedaDestino());
        TipoCambio tipoCambio = tipoCambioProvider.obtenerTipoCambio();
        ResultadoConversion calculo = conversionService.convertir(
                request.monto(), request.monedaOrigen(), request.monedaDestino(), tipoCambio);
        return new CotizacionResponse(
                request.monto(),
                request.monedaOrigen(),
                request.monedaDestino(),
                calculo.tipoCambioAplicado(),
                calculo.resultado(),
                tipoCambio.fechaReferencia(),
                tipoCambio.fuente(),
                CotizacionResponse.MENSAJE_COTIZACION);
    }
}
