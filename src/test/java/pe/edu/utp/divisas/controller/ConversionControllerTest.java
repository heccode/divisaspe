package pe.edu.utp.divisas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.divisas.exception.BcrpUnavailableException;
import pe.edu.utp.divisas.model.ConversionResponse;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;
import pe.edu.utp.divisas.seguridad.SecurityConfig;

@WebMvcTest(ConversionController.class)
@Import(SecurityConfig.class)
class ConversionControllerTest {

    private static final String URL = "/api/v1/conversiones/referencial";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperacionOrchestrator orchestrator;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requestValidoDevuelve200() throws Exception {
        when(orchestrator.convertirReferencial(any())).thenReturn(new ConversionResponse(
                new BigDecimal("100"), Moneda.USD, Moneda.PEN, new BigDecimal("3.350"),
                new BigDecimal("335.00"), LocalDate.of(2026, 8, 12), "BCRPData",
                ConversionResponse.MENSAJE_REFERENCIAL));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":100,\"monedaOrigen\":\"USD\",\"monedaDestino\":\"PEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(335.00))
                .andExpect(jsonPath("$.fuente").value("BCRPData"));
    }

    @Test
    void montoNegativoDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":-1,\"monedaOrigen\":\"USD\",\"monedaDestino\":\"PEN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"));
    }

    @Test
    void monedaNoSoportadaDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":10,\"monedaOrigen\":\"EUR\",\"monedaDestino\":\"PEN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bcrpNoDisponibleDevuelve502() throws Exception {
        when(orchestrator.convertirReferencial(any())).thenThrow(new BcrpUnavailableException("x"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":10,\"monedaOrigen\":\"USD\",\"monedaDestino\":\"PEN\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("BCRP_NO_DISPONIBLE"));
    }
}
