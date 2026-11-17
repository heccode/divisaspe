package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CuentaBancariaRequest(
        @NotNull(message = "flujo es obligatorio") FlujoCuenta flujo,
        @NotNull(message = "moneda es obligatoria") Moneda moneda,
        @NotNull(message = "bancoId es obligatorio") Long bancoId,
        @NotBlank(message = "numeroCuenta es obligatorio")
        @Pattern(regexp = "^\\d{10,20}$", message = "numeroCuenta debe tener entre 10 y 20 digitos") String numeroCuenta,
        @NotBlank(message = "cci es obligatorio")
        @Pattern(regexp = "^\\d{20}$", message = "cci debe tener 20 digitos") String cci,
        @NotBlank(message = "titular es obligatorio")
        @Size(max = 120, message = "titular admite maximo 120 caracteres")
        @Pattern(regexp = "^\\p{L}[\\p{L}'.-]*( +\\p{L}[\\p{L}'.-]*)+$",
                message = "titular debe incluir nombres y apellidos, solo letras") String titular) {
}
