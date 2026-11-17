package pe.edu.utp.divisas.orchestration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.edu.utp.divisas.integration.camel.TipoCambioProvider;
import pe.edu.utp.divisas.model.ConversionRequest;
import pe.edu.utp.divisas.model.ConversionResponse;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.model.TipoCambio;
import pe.edu.utp.divisas.service.ConversionService;

class OperacionOrchestratorTest {

    @Test
    void obtieneTipoCambioInvocaConversionYConsolidaRespuesta() {
        TipoCambioProvider provider = mock(TipoCambioProvider.class);
        TipoCambio tc = new TipoCambio(LocalDate.of(2026, 8, 12),
                new BigDecimal("3.350"), new BigDecimal("3.360"), "BCRPData");
        when(provider.obtenerTipoCambio()).thenReturn(tc);
        ConversionService conversion = new ConversionService();
        pe.edu.utp.divisas.service.DivisaService divisaService = new pe.edu.utp.divisas.service.DivisaService();
        OperacionOrchestrator orchestrator = new OperacionOrchestrator(provider, conversion,
                new pe.edu.utp.divisas.service.ValidacionService(divisaService), divisaService,
                mock(pe.edu.utp.divisas.service.UsuarioService.class),
                mock(pe.edu.utp.divisas.service.HistorialService.class),
                new pe.edu.utp.divisas.service.AuditoriaService(),
                new pe.edu.utp.divisas.integration.sunat.ConsultaRucAdapter());

        ConversionResponse r = orchestrator.convertirReferencial(
                new ConversionRequest(new BigDecimal("100"), Moneda.USD, Moneda.PEN));

        verify(provider).obtenerTipoCambio();
        assertThat(r.resultado()).isEqualByComparingTo("335.00");
        assertThat(r.tipoCambioAplicado()).isEqualByComparingTo("3.350");
        assertThat(r.fechaReferencia()).isEqualTo(LocalDate.of(2026, 8, 12));
        assertThat(r.fuente()).isEqualTo("BCRPData");
        assertThat(r.mensaje()).isEqualTo(ConversionResponse.MENSAJE_REFERENCIAL);
    }
}
