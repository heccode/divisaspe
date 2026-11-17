package pe.edu.utp.divisas.seguridad;

import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private static final String EMISOR = "divisape";

    private final JwtEncoder encoder;
    private final Duration vigencia;

    public JwtService(JwtEncoder encoder, @Value("${seguridad.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.encoder = encoder;
        this.vigencia = Duration.ofMinutes(expiracionMinutos);
    }

    public TokenEmitido emitir(Long usuarioId) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(vigencia);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .subject(String.valueOf(usuarioId))
                .issuedAt(ahora)
                .expiresAt(expira)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(token, expira);
    }

    public record TokenEmitido(String token, Instant expira) {
    }
}
