package pe.edu.utp.divisas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;
import pe.edu.utp.divisas.seguridad.SecurityConfig;
import pe.edu.utp.divisas.service.AuthService;
import pe.edu.utp.divisas.service.CuentaBancariaService;
import pe.edu.utp.divisas.service.UsuarioService;

@WebMvcTest({HistorialController.class, UsuarioController.class, ConversionController.class, AuthController.class,
        CuentaBancariaController.class})
@Import(SecurityConfig.class)
class AccesoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperacionOrchestrator orchestrator;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CuentaBancariaService cuentaBancariaService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void historialSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/5/historial"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        verifyNoInteractions(orchestrator);
    }

    @Test
    void historialPropioDevuelve200() throws Exception {
        when(orchestrator.consultarHistorial(5L)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/usuarios/5/historial").with(jwt().jwt(j -> j.subject("5"))))
                .andExpect(status().isOk());
    }

    @Test
    void historialDeOtroUsuarioDevuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/6/historial").with(jwt().jwt(j -> j.subject("5"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        verifyNoInteractions(orchestrator);
    }

    @Test
    void perfilDeOtroUsuarioDevuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/6").with(jwt().jwt(j -> j.subject("5"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(usuarioService);
    }

    @Test
    void conversionConUsuarioSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/conversiones/referencial").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":10,\"monedaOrigen\":\"USD\",\"monedaDestino\":\"PEN\",\"usuarioId\":5}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(orchestrator);
    }

    @Test
    void conversionConOtroUsuarioDevuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/conversiones/referencial").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("7")))
                        .content("{\"monto\":10,\"monedaOrigen\":\"USD\",\"monedaDestino\":\"PEN\",\"usuarioId\":5}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orchestrator);
    }

    @Test
    void registroEsPublicoYExigeContrasenaMinima() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Lucia Quispe Flores\",\"email\":\"lucia.quispe@gmail.com\",\"password\":\"abc12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"));
        verifyNoInteractions(authService);
    }

    @Test
    void meSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void cuentasBancariasSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/5/cuentas-bancarias")).andExpect(status().isUnauthorized());
        verifyNoInteractions(cuentaBancariaService);
    }

    @Test
    void cuentasBancariasDeOtroUsuarioDevuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/6/cuentas-bancarias").with(jwt().jwt(j -> j.subject("5"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/usuarios/6/cuentas-bancarias").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("5")))
                        .content("{\"flujo\":\"ENVIO\",\"moneda\":\"USD\",\"bancoId\":1,\"numeroCuenta\":\"1930012345678\","
                                + "\"cci\":\"00219300123456789012\",\"titular\":\"Lucia Quispe Flores\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cuentaBancariaService);
    }

    @Test
    void registroDeCuentaConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/5/cuentas-bancarias").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("5")))
                        .content("{\"flujo\":\"ENVIO\",\"moneda\":\"USD\",\"bancoId\":1,\"numeroCuenta\":\"12\","
                                + "\"cci\":\"123\",\"titular\":\"Lucia\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"));
        verifyNoInteractions(cuentaBancariaService);
    }

    @Test
    void registroDeCuentaValidoDevuelve201() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/5/cuentas-bancarias").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("5")))
                        .content("{\"flujo\":\"RECEPCION\",\"moneda\":\"PEN\",\"bancoId\":1,"
                                + "\"numeroCuenta\":\"1930012345678\",\"cci\":\"00219300123456789012\","
                                + "\"titular\":\"Lucia Quispe Flores\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void cambioDeContrasenaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(put("/api/v1/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"clave1234\",\"passwordNueva\":\"nueva5678\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    void cambioDeContrasenaConNuevaCortaDevuelve400() throws Exception {
        mockMvc.perform(put("/api/v1/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("5")))
                        .content("{\"passwordActual\":\"clave1234\",\"passwordNueva\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUEST_INVALIDO"));
        verifyNoInteractions(authService);
    }

    @Test
    void cambioDeContrasenaValidoDevuelve200() throws Exception {
        mockMvc.perform(put("/api/v1/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("5")))
                        .content("{\"passwordActual\":\"clave1234\",\"passwordNueva\":\"nueva5678\"}"))
                .andExpect(status().isOk());
    }
}
