package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.integration.bcrp.BcrpRespuestaCruda;
import pe.edu.utp.divisas.integration.bcrp.BcrpTipoCambioAdapter;

@RestController
@RequestMapping("/api/v1/evidencias")
@Tag(name = "Resources")
public class EvidenciaController {

    private final BcrpTipoCambioAdapter adapter;

    public EvidenciaController(BcrpTipoCambioAdapter adapter) {
        this.adapter = adapter;
    }

    @GetMapping("/bcrp-directo")
    @Operation(summary = "Consulta directa a BCRPData sin normalizar la respuesta")
    public BcrpRespuestaCruda bcrpDirecto() {
        return adapter.consultarCrudo();
    }
}
