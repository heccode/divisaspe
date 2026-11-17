package pe.edu.utp.divisas.seguridad;

import org.springframework.security.oauth2.jwt.Jwt;
import pe.edu.utp.divisas.exception.AccesoDenegadoException;
import pe.edu.utp.divisas.exception.NoAutenticadoException;

public final class SesionActual {

    private SesionActual() {
    }

    public static Long usuarioId(Jwt jwt) {
        if (jwt == null) {
            throw new NoAutenticadoException();
        }
        return Long.valueOf(jwt.getSubject());
    }

    public static void exigirPropietario(Jwt jwt, Long usuarioId) {
        if (!usuarioId(jwt).equals(usuarioId)) {
            throw new AccesoDenegadoException();
        }
    }
}
