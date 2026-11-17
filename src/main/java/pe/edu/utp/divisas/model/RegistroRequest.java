package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "nombre es obligatorio")
        @Size(max = 120, message = "nombre admite maximo 120 caracteres") String nombre,
        @NotBlank(message = "email es obligatorio")
        @Email(message = "email no es valido")
        @Size(max = 160, message = "email admite maximo 160 caracteres") String email,
        @NotBlank(message = "password es obligatoria")
        @Size(min = 6, max = 72, message = "la contrasena debe tener entre 6 y 72 caracteres")
        String password,
        String ruc) {
}
