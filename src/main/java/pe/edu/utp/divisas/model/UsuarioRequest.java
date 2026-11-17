package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "nombre es obligatorio")
        @Size(max = 120, message = "nombre admite maximo 120 caracteres") String nombre,
        @Size(max = 120, message = "apellidos admite maximo 120 caracteres") String apellidos,
        TipoDocumento tipoDocumento,
        @Size(max = 20, message = "numeroDocumento admite maximo 20 caracteres") String numeroDocumento,
        @NotBlank(message = "email es obligatorio")
        @Email(message = "email no es valido")
        @Size(max = 160, message = "email admite maximo 160 caracteres") String email,
        String ruc) {
}
