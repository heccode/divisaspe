package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.CotizacionRequest;
import pe.edu.utp.divisas.model.CotizacionResponse;
import pe.edu.utp.divisas.service.CotizacionService;

@RestController
@Tag(name = "Resources")
@RequestMapping("/api/v1/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping("/referencial")
    @Operation(summary = "Cotiza un monto entre USD y PEN sin guardarlo en el historial")
    public CotizacionResponse cotizar(@Valid @RequestBody CotizacionRequest request) {
        return cotizacionService.generarCotizacion(request);
    }
}
