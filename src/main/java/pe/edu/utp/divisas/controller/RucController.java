package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/ruc")
public class RucController {

    private final OperacionOrchestrator orchestrator;

    public RucController(OperacionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/{ruc}")
    @Operation(summary = "Consulta de RUC en SUNAT (no disponible: responde 501 controlado)")
    public void consultar(@PathVariable String ruc) {
        orchestrator.consultarRuc(ruc);
    }
}
