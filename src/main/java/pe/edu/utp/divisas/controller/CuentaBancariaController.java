package pe.edu.utp.divisas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.divisas.model.BancoResponse;
import pe.edu.utp.divisas.model.CuentaBancariaRequest;
import pe.edu.utp.divisas.model.CuentaBancariaResponse;
import pe.edu.utp.divisas.seguridad.SesionActual;
import pe.edu.utp.divisas.service.CuentaBancariaService;

@RestController
@Tag(name = "Resources")
public class CuentaBancariaController {

    private final CuentaBancariaService service;

    public CuentaBancariaController(CuentaBancariaService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/bancos")
    @Operation(summary = "Bancos autorizados para registrar cuentas")
    public List<BancoResponse> bancos() {
        return service.listarBancos();
    }

    @GetMapping("/api/v1/usuarios/{id}/cuentas-bancarias")
    @Operation(summary = "Cuentas bancarias del usuario de la sesion (mas recientes primero)")
    public List<CuentaBancariaResponse> listar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        SesionActual.exigirPropietario(jwt, id);
        return service.listar(id);
    }

    @PostMapping("/api/v1/usuarios/{id}/cuentas-bancarias")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una cuenta bancaria para enviar o recibir dinero (solo el propio usuario)")
    public CuentaBancariaResponse registrar(@PathVariable Long id, @Valid @RequestBody CuentaBancariaRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        SesionActual.exigirPropietario(jwt, id);
        return service.registrar(id, request);
    }
}
