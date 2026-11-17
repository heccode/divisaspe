package pe.edu.utp.divisas.model;

import java.time.LocalDateTime;

public record CuentaBancariaResponse(
        Long id,
        FlujoCuenta flujo,
        Moneda moneda,
        BancoResponse banco,
        String numeroCuenta,
        String cci,
        String titular,
        LocalDateTime fechaRegistro) {
}
