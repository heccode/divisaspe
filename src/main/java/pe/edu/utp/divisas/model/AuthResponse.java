package pe.edu.utp.divisas.model;

import java.time.Instant;

public record AuthResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario) {
}
