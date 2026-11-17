package pe.edu.utp.divisas.orchestration;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.utp.divisas.integration.camel.TipoCambioProvider;
import pe.edu.utp.divisas.integration.sunat.ConsultaRucAdapter;
import pe.edu.utp.divisas.model.ConversionRequest;
import pe.edu.utp.divisas.model.ConversionResponse;
import pe.edu.utp.divisas.model.Divisa;
import pe.edu.utp.divisas.model.OperacionResponse;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;
import pe.edu.utp.divisas.service.AuditoriaService;
import pe.edu.utp.divisas.service.ConversionPort;
import pe.edu.utp.divisas.service.DivisaService;
import pe.edu.utp.divisas.service.HistorialService;
import pe.edu.utp.divisas.service.UsuarioService;
import pe.edu.utp.divisas.service.ValidacionService;

@Service
public class OperacionOrchestrator {

    private final TipoCambioProvider tipoCambioProvider;
    private final ConversionPort conversionService;
    private final ValidacionService validacionService;
    private final DivisaService divisaService;
    private final UsuarioService usuarioService;
    private final HistorialService historialService;
    private final AuditoriaService auditoriaService;
    private final ConsultaRucAdapter consultaRucAdapter;

    public OperacionOrchestrator(TipoCambioProvider tipoCambioProvider, ConversionPort conversionService,
                                 ValidacionService validacionService, DivisaService divisaService,
                                 UsuarioService usuarioService, HistorialService historialService,
                                 AuditoriaService auditoriaService, ConsultaRucAdapter consultaRucAdapter) {
        this.consultaRucAdapter = consultaRucAdapter;
        this.usuarioService = usuarioService;
        this.historialService = historialService;
        this.auditoriaService = auditoriaService;
        this.tipoCambioProvider = tipoCambioProvider;
        this.conversionService = conversionService;
        this.validacionService = validacionService;
        this.divisaService = divisaService;
    }

    public List<Divisa> listarDivisas() {
        return divisaService.listarDivisas();
    }

    public TipoCambio consultarTipoCambio() {
        return tipoCambioProvider.obtenerTipoCambio();
    }

    public ConversionResponse convertirReferencial(ConversionRequest request) {
        validacionService.validarMonto(request.monto());
        validacionService.validarParMonedas(request.monedaOrigen(), request.monedaDestino());
        Long usuarioId = request.usuarioId();
        if (usuarioId != null) {
            usuarioService.obtenerUsuario(usuarioId);
        }
        TipoCambio tipoCambio = tipoCambioProvider.obtenerTipoCambio();
        ResultadoConversion resultado = conversionService.convertir(
                request.monto(), request.monedaOrigen(), request.monedaDestino(), tipoCambio);
        ConversionResponse respuesta = new ConversionResponse(
                request.monto(),
                request.monedaOrigen(),
                request.monedaDestino(),
                resultado.tipoCambioAplicado(),
                resultado.resultado(),
                tipoCambio.fechaReferencia(),
                tipoCambio.fuente(),
                ConversionResponse.MENSAJE_REFERENCIAL);

        if (usuarioId == null) {
            auditoriaService.registrarEvento("CONVERSION_NO_REGISTRADA", null, resumen(respuesta));
            return respuesta;
        }
        OperacionResponse operacion = historialService.registrarOperacion(usuarioId, respuesta);
        auditoriaService.registrarEvento("CONVERSION_REGISTRADA", usuarioId,
                "operacionId=" + operacion.id() + " " + resumen(respuesta));
        return respuesta.conOperacionId(operacion.id());
    }

    public void consultarRuc(String ruc) {
        validacionService.validarRuc(ruc);
        consultaRucAdapter.consultarRuc(ruc);
    }

    public List<OperacionResponse> consultarHistorial(Long usuarioId) {
        List<OperacionResponse> historial = historialService.consultarHistorial(usuarioId);
        auditoriaService.registrarEvento("HISTORIAL_CONSULTADO", usuarioId, "operaciones=" + historial.size());
        return historial;
    }

    private static String resumen(ConversionResponse r) {
        return r.montoOriginal() + " " + r.monedaOrigen() + " -> " + r.resultado() + " " + r.monedaDestino();
    }
}
