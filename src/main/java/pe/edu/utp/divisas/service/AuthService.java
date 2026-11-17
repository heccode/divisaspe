package pe.edu.utp.divisas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.divisas.exception.CredencialesInvalidasException;
import pe.edu.utp.divisas.exception.DatosUsuarioInvalidosException;
import pe.edu.utp.divisas.exception.PasswordActualIncorrectaException;
import pe.edu.utp.divisas.exception.UsuarioNoEncontradoException;
import pe.edu.utp.divisas.model.AuthResponse;
import pe.edu.utp.divisas.model.CambioPasswordRequest;
import pe.edu.utp.divisas.model.LoginRequest;
import pe.edu.utp.divisas.model.RegistroRequest;
import pe.edu.utp.divisas.model.UsuarioResponse;
import pe.edu.utp.divisas.persistence.Usuario;
import pe.edu.utp.divisas.persistence.UsuarioRepository;
import pe.edu.utp.divisas.seguridad.JwtService;

@Service
public class AuthService {

    private static final String TIPO_TOKEN = "Bearer";

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;
    private final String hashFicticio;

    public AuthService(UsuarioService usuarioService, UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
        this.hashFicticio = passwordEncoder.encode("sin-usuario");
    }

    public AuthResponse registrar(RegistroRequest request) {
        UsuarioResponse usuario = usuarioService.registrarUsuario(request, passwordEncoder.encode(request.password()));
        auditoriaService.registrarEvento("USUARIO_REGISTRADO", usuario.id(), "registro correcto");
        return emitir(usuario);
    }

    public AuthResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarios.findByEmail(UsuarioService.normalizarEmail(request.email())).orElse(null);
        // Se compara siempre contra un hash para que el tiempo de respuesta no revele si el correo existe.
        String hash = usuario != null && usuario.getPasswordHash() != null ? usuario.getPasswordHash() : hashFicticio;
        boolean coincide = passwordEncoder.matches(request.password(), hash);
        if (usuario == null || usuario.getPasswordHash() == null || !coincide) {
            auditoriaService.registrarEvento("LOGIN_FALLIDO", usuario != null ? usuario.getId() : null,
                    "credenciales invalidas");
            throw new CredencialesInvalidasException();
        }
        auditoriaService.registrarEvento("LOGIN_CORRECTO", usuario.getId(), "inicio de sesion");
        return emitir(UsuarioService.aRespuesta(usuario));
    }

    @Transactional
    public void cambiarPassword(Long usuarioId, CambioPasswordRequest request) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
        if (usuario.getPasswordHash() == null || !passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            auditoriaService.registrarEvento("PASSWORD_CAMBIO_FALLIDO", usuarioId, "contrasena actual incorrecta");
            throw new PasswordActualIncorrectaException();
        }
        if (request.passwordActual().equals(request.passwordNueva())) {
            throw new DatosUsuarioInvalidosException("La contraseña nueva debe ser distinta de la actual.");
        }
        usuario.cambiarPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        usuarios.save(usuario);
        auditoriaService.registrarEvento("PASSWORD_CAMBIADA", usuarioId, "contrasena actualizada");
    }

    private AuthResponse emitir(UsuarioResponse usuario) {
        JwtService.TokenEmitido t = jwtService.emitir(usuario.id());
        return new AuthResponse(t.token(), TIPO_TOKEN, t.expira(), usuario);
    }
}
