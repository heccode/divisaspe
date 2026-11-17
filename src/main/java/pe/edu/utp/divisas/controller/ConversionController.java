package pe.edu.utp.divisas.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.utp.divisas.model.ConversionRequest;
import pe.edu.utp.divisas.model.ConversionResponse;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;
import pe.edu.utp.divisas.seguridad.SesionActual;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/conversiones")
public class ConversionController {

    private final OperacionOrchestrator orchestrator;

    public ConversionController(OperacionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/referencial")
    @Operation(summary = "Convierte un monto entre USD y PEN con el tipo de cambio vigente")
    public ConversionResponse convertir(@Valid @RequestBody ConversionRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        if (request.usuarioId() != null) {
            SesionActual.exigirPropietario(jwt, request.usuarioId());
        }
        return orchestrator.convertirReferencial(request);
    }
}
