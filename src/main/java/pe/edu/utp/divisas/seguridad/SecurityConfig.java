package pe.edu.utp.divisas.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.edu.utp.divisas.model.ApiError;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, ObjectMapper json) throws Exception {
        AuthenticationEntryPoint noAutenticado = (req, res, e) -> escribir(res, json, HttpServletResponse.SC_UNAUTHORIZED,
                new ApiError("NO_AUTENTICADO", "Tu sesion no es valida o expiro. Inicia sesion de nuevo."));
        AccessDeniedHandler denegado = (req, res, e) -> escribir(res, json, HttpServletResponse.SC_FORBIDDEN,
                new ApiError("ACCESO_DENEGADO", "No tienes permiso para acceder a este recurso."));

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/registro", "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tipo-cambio", "/api/v1/divisas",
                                "/api/v1/ruc/*", "/api/v1/evidencias/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/conversiones/referencial",
                                "/api/v1/cotizaciones/referencial").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(noAutenticado)
                        .accessDeniedHandler(denegado))
                .exceptionHandling(e -> e.authenticationEntryPoint(noAutenticado).accessDeniedHandler(denegado));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${seguridad.cors.origenes}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> patrones = Arrays.stream(origenes.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList();
        config.setAllowedOriginPatterns(patrones);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void escribir(HttpServletResponse res, ObjectMapper json, int status, ApiError error)
            throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getWriter(), error);
    }
}
