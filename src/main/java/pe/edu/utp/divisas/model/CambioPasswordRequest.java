package pe.edu.utp.divisas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank(message = "passwordActual es obligatoria") String passwordActual,
        @NotBlank(message = "passwordNueva es obligatoria")
        @Size(min = 6, max = 72, message = "la contrasena nueva debe tener entre 6 y 72 caracteres")
        String passwordNueva) {
}
