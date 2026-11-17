package pe.edu.utp.divisas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.utp.divisas.model.TipoCambio;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/tipo-cambio")
public class TipoCambioController {

    private final OperacionOrchestrator orchestrator;

    public TipoCambioController(OperacionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping
    @Operation(summary = "Obtiene el tipo de cambio compra/venta normalizado desde BCRPData")
    public TipoCambio obtener() {
        return orchestrator.consultarTipoCambio();
    }
}
