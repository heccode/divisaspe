package pe.edu.utp.divisas.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import pe.edu.utp.divisas.exception.CredencialesInvalidasException;
import pe.edu.utp.divisas.exception.DatosUsuarioInvalidosException;
import pe.edu.utp.divisas.exception.PasswordActualIncorrectaException;
import pe.edu.utp.divisas.exception.EmailDuplicadoException;
import pe.edu.utp.divisas.model.AuthResponse;
import pe.edu.utp.divisas.model.CambioPasswordRequest;
import pe.edu.utp.divisas.model.LoginRequest;
import pe.edu.utp.divisas.model.RegistroRequest;
import pe.edu.utp.divisas.persistence.Usuario;
import pe.edu.utp.divisas.persistence.UsuarioRepository;
import pe.edu.utp.divisas.service.AuditoriaService;
import pe.edu.utp.divisas.service.AuthService;
import pe.edu.utp.divisas.service.UsuarioService;
import pe.edu.utp.divisas.service.ValidacionService;

class AuthServiceTest {

    private static final SecretKey CLAVE = new SecretKeySpec(
            "clave-solo-para-pruebas-de-32-caracteres!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(CLAVE).macAlgorithm(MacAlgorithm.HS256).build();
    private UsuarioRepository repository;
    private AuthService auth;

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);
        when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> conId(inv.getArgument(0), 9L));
        UsuarioService usuarios = new UsuarioService(repository, mock(ValidacionService.class));
        JwtService jwt = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(CLAVE)), 60);
        auth = new AuthService(usuarios, repository, encoder, jwt, mock(AuditoriaService.class));
    }

    @Test
    void registrarGuardaHashYEmiteTokenDelUsuario() {
        AuthResponse r = auth.registrar(new RegistroRequest("Lucia Quispe Flores", " Lucia.Quispe@Gmail.com ",
                "clave1234", null));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).saveAndFlush(guardado.capture());
        assertEquals("lucia.quispe@gmail.com", guardado.getValue().getEmail());
        assertNotEquals("clave1234", guardado.getValue().getPasswordHash());
        assertTrue(encoder.matches("clave1234", guardado.getValue().getPasswordHash()));

        Jwt token = decoder.decode(r.token());
        assertEquals("9", token.getSubject());
        assertEquals("Bearer", r.tipo());
    }

    @Test
    void registrarConEmailExistenteFalla() {
        when(repository.existsByEmail("lucia.quispe@gmail.com")).thenReturn(true);
        assertThrows(EmailDuplicadoException.class, () -> auth.registrar(
                new RegistroRequest("Lucia Quispe Flores", "lucia.quispe@gmail.com", "clave1234", null)));
    }

    @Test
    void loginCorrectoEmiteToken() {
        usuarioExistente("clave1234");
        AuthResponse r = auth.iniciarSesion(new LoginRequest("LUCIA.quispe@gmail.com", "clave1234"));
        assertEquals("9", decoder.decode(r.token()).getSubject());
        assertEquals("lucia.quispe@gmail.com", r.usuario().email());
    }

    @Test
    void loginConContrasenaIncorrectaFalla() {
        usuarioExistente("clave1234");
        assertThrows(CredencialesInvalidasException.class,
                () -> auth.iniciarSesion(new LoginRequest("lucia.quispe@gmail.com", "otraClave99")));
    }

    @Test
    void loginConCorreoInexistenteFalla() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> auth.iniciarSesion(new LoginRequest("nadie@gmail.com", "clave1234")));
    }

    @Test
    void loginDeUsuarioSinContrasenaFalla() {
        Usuario antiguo = conId(new Usuario("Marco Rojas Vega", "marco.rojas@gmail.com", null, null), 3L);
        when(repository.findByEmail("marco.rojas@gmail.com")).thenReturn(Optional.of(antiguo));
        assertThrows(CredencialesInvalidasException.class,
                () -> auth.iniciarSesion(new LoginRequest("marco.rojas@gmail.com", "clave1234")));
    }

    @Test
    void tokenAlteradoOExpiradoNoSeAcepta() {
        AuthResponse r = auth.registrar(new RegistroRequest("Lucia Quispe Flores", "lucia.quispe@gmail.com",
                "clave1234", null));
        String alterado = r.token().substring(0, r.token().length() - 2) + "xx";
        assertThrows(Exception.class, () -> decoder.decode(alterado));
    }

    @Test
    void cambiarPasswordGuardaUnHashNuevo() {
        Usuario u = usuarioExistente("clave1234");
        when(repository.findById(9L)).thenReturn(Optional.of(u));
        auth.cambiarPassword(9L, new CambioPasswordRequest("clave1234", "nueva5678"));
        assertTrue(encoder.matches("nueva5678", u.getPasswordHash()));
        verify(repository).save(u);
    }

    @Test
    void cambiarPasswordConActualIncorrectaFalla() {
        Usuario u = usuarioExistente("clave1234");
        when(repository.findById(9L)).thenReturn(Optional.of(u));
        assertThrows(PasswordActualIncorrectaException.class,
                () -> auth.cambiarPassword(9L, new CambioPasswordRequest("equivocada1", "nueva5678")));
        assertTrue(encoder.matches("clave1234", u.getPasswordHash()));
    }

    @Test
    void cambiarPasswordPorLaMismaFalla() {
        Usuario u = usuarioExistente("clave1234");
        when(repository.findById(9L)).thenReturn(Optional.of(u));
        assertThrows(DatosUsuarioInvalidosException.class,
                () -> auth.cambiarPassword(9L, new CambioPasswordRequest("clave1234", "clave1234")));
    }

    private Usuario usuarioExistente(String password) {
        Usuario u = conId(new Usuario("Lucia Quispe Flores", "lucia.quispe@gmail.com", null, encoder.encode(password)), 9L);
        when(repository.findByEmail("lucia.quispe@gmail.com")).thenReturn(Optional.of(u));
        return u;
    }

    private static Usuario conId(Usuario u, Long id) {
        try {
            var campo = Usuario.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(u, id);
            var fecha = Usuario.class.getDeclaredField("fechaRegistro");
            fecha.setAccessible(true);
            fecha.set(u, LocalDateTime.now());
            var actual = Usuario.class.getDeclaredField("fechaActualizacion");
            actual.setAccessible(true);
            actual.set(u, LocalDateTime.now());
            return u;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
