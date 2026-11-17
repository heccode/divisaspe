package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.UsuarioRequest;
import pe.edu.utp.divisas.model.UsuarioResponse;
import pe.edu.utp.divisas.seguridad.SesionActual;
import pe.edu.utp.divisas.service.UsuarioService;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el usuario de la sesion (solo el propio)")
    public UsuarioResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        SesionActual.exigirPropietario(jwt, id);
        return usuarioService.obtenerUsuario(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza el usuario de la sesion (solo el propio)")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        SesionActual.exigirPropietario(jwt, id);
        return usuarioService.actualizarUsuario(id, request);
    }
}
