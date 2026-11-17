package pe.edu.utp.divisas.conversion;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.CalculoConversionRequest;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;
import pe.edu.utp.divisas.service.ConversionService;

@RestController
@RequestMapping("/api/v1/conversion")
public class CalculoController {

    private static final Logger log = LoggerFactory.getLogger(CalculoController.class);

    private final ConversionService conversionService;

    public CalculoController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping("/calcular")
    public ResultadoConversion calcular(@Valid @RequestBody CalculoConversionRequest r) {
        log.info("Calculo {} -> {} con tasa de la fecha {}", r.monedaOrigen(), r.monedaDestino(), r.fechaReferencia());
        TipoCambio tasa = new TipoCambio(r.fechaReferencia(), r.compra(), r.venta(), "entrada");
        return conversionService.convertir(r.monto(), r.monedaOrigen(), r.monedaDestino(), tasa);
    }
}
