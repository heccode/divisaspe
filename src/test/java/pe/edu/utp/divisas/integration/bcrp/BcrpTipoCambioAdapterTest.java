package pe.edu.utp.divisas.integration.bcrp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import pe.edu.utp.divisas.exception.BcrpUnavailableException;
import pe.edu.utp.divisas.exception.NoProcessableExchangeRateException;
import pe.edu.utp.divisas.model.TipoCambio;

class BcrpTipoCambioAdapterTest {

    private static final String BASE = "http://bcrp.test/api";

    private final BcrpProperties props =
            new BcrpProperties(BASE, new BcrpProperties.Series("PD04639PD", "PD04640PD"));
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final BcrpTipoCambioAdapter adapter =
            new BcrpTipoCambioAdapter(builder, props, new ObjectMapper());

    private static String fixture(String nombre) throws IOException {
        return StreamUtils.copyToString(new ClassPathResource("bcrp/" + nombre).getInputStream(),
                StandardCharsets.UTF_8);
    }

    @Test
    void normalizaCompraYVentaValidasDelPeriodoMasReciente() throws IOException {
        TipoCambio tc = adapter.normalizar(fixture("respuesta-valida.json"));
        assertThat(tc.compra()).isEqualByComparingTo("3.359");
        assertThat(tc.venta()).isEqualByComparingTo("3.365");
        assertThat(tc.fechaReferencia()).isEqualTo(LocalDate.of(2026, 8, 12));
        assertThat(tc.fuente()).isEqualTo("BCRPData");
    }

    @Test
    void ignoraPeriodosRecientesConNdYSeleccionaUnoAnteriorValido() throws IOException {
        TipoCambio tc = adapter.normalizar(fixture("respuesta-con-nd.json"));
        assertThat(tc.fechaReferencia()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(tc.compra()).isEqualByComparingTo("3.364");
        assertThat(tc.venta()).isEqualByComparingTo("3.372");
    }

    @Test
    void fallaDeFormaControladaSinPeriodosUtiles() throws IOException {
        String json = fixture("respuesta-sin-datos-validos.json");
        assertThatThrownBy(() -> adapter.normalizar(json))
                .isInstanceOf(NoProcessableExchangeRateException.class);
    }

    @Test
    void aceptaComaDecimal() {
        String json = "{\"periods\":[{\"name\":\"12.Ago.26\",\"values\":[\"3,359\",\"3,365\"]}]}";
        assertThat(adapter.normalizar(json).compra()).isEqualByComparingTo("3.359");
    }

    @Test
    void consultaElEndpointConfiguradoYNormaliza() throws IOException {
        server.expect(requestTo(BASE + "/PD04639PD-PD04640PD/json"))
                .andRespond(withSuccess(fixture("respuesta-valida.json"), MediaType.APPLICATION_JSON));
        assertThat(adapter.obtenerTipoCambio().venta()).isEqualByComparingTo("3.365");
        server.verify();
    }

    @Test
    void reintentaCuandoBcrpDevuelveHtmlIntermedio() throws IOException {
        String url = BASE + "/PD04639PD-PD04640PD/json";
        server.expect(requestTo(url)).andRespond(withSuccess("<!DOCTYPE html><html></html>", MediaType.TEXT_HTML));
        server.expect(requestTo(url)).andRespond(withSuccess(fixture("respuesta-valida.json"), MediaType.APPLICATION_JSON));
        assertThat(adapter.obtenerTipoCambio().compra()).isEqualByComparingTo("3.359");
        server.verify();
    }

    @Test
    void respuestaHttpNoSatisfactoriaEsBcrpNoDisponible() {
        server.expect(ExpectedCount.times(6), requestTo(BASE + "/PD04639PD-PD04640PD/json"))
                .andRespond(withServerError());
        assertThatThrownBy(adapter::obtenerTipoCambio).isInstanceOf(BcrpUnavailableException.class);
    }
}
