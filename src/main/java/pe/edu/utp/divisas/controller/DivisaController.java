package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.Divisa;
import pe.edu.utp.divisas.orchestration.OperacionOrchestrator;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/divisas")
public class DivisaController {

    private final OperacionOrchestrator orchestrator;

    public DivisaController(OperacionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping
    @Operation(summary = "Lista las divisas admitidas (USD y PEN)")
    public List<Divisa> listar() {
        return orchestrator.listarDivisas();
    }
}
