package pe.edu.utp.divisas.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import pe.edu.utp.divisas.exception.InvalidConversionException;
import pe.edu.utp.divisas.model.Divisa;

@Service
public class DivisaService {

    private static final List<Divisa> CATALOGO = List.of(
            new Divisa("USD", "Dolar estadounidense", "US$"),
            new Divisa("PEN", "Sol peruano", "S/"));

    public List<Divisa> listarDivisas() {
        return CATALOGO;
    }

    public Divisa obtenerDivisa(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new InvalidConversionException("El codigo de divisa es obligatorio.");
        }
        String buscado = codigo.trim().toUpperCase(Locale.ROOT);
        return CATALOGO.stream()
                .filter(d -> d.codigo().equals(buscado))
                .findFirst()
                .orElseThrow(() -> new InvalidConversionException(
                        "Divisa no admitida: " + codigo + ". Divisas admitidas: USD, PEN."));
    }
}
