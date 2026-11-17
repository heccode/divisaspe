package pe.edu.utp.divisas.model;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String apellidos,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String email,
        String ruc,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion) {
}
