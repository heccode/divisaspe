package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.OperacionResponse;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;
import pe.edu.utp.divisas.seguridad.SesionActual;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/usuarios")
public class HistorialController {

    private final OperacionOrchestrator orchestrator;

    public HistorialController(OperacionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial de operaciones simuladas de un usuario (mas recientes primero)")
    public List<OperacionResponse> historial(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        SesionActual.exigirPropietario(jwt, id);
        return orchestrator.consultarHistorial(id);
    }
}
