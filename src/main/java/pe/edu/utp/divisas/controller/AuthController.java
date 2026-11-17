package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.AuthResponse;
import pe.edu.utp.divisas.model.CambioPasswordRequest;
import pe.edu.utp.divisas.model.LoginRequest;
import pe.edu.utp.divisas.model.MensajeResponse;
import pe.edu.utp.divisas.model.RegistroRequest;
import pe.edu.utp.divisas.model.UsuarioResponse;
import pe.edu.utp.divisas.seguridad.SesionActual;
import pe.edu.utp.divisas.service.AuthService;
import pe.edu.utp.divisas.service.UsuarioService;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un usuario (RUC opcional) y devuelve su token de sesion")
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesion con correo y contrasena")
    public AuthResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return authService.iniciarSesion(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve el usuario de la sesion actual")
    public UsuarioResponse yo(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.obtenerUsuario(SesionActual.usuarioId(jwt));
    }

    @PutMapping("/password")
    @Operation(summary = "Cambia la contrasena del usuario de la sesion")
    public MensajeResponse cambiarPassword(@Valid @RequestBody CambioPasswordRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        authService.cambiarPassword(SesionActual.usuarioId(jwt), request);
        return new MensajeResponse("Contrasena actualizada.");
    }
}
