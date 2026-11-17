package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email es obligatorio") String email,
        @NotBlank(message = "password es obligatoria") String password) {
}
