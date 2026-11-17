package pe.edu.utp.divisas.integration.bcrp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.edu.utp.divisas.exception.BcrpUnavailableException;
import pe.edu.utp.divisas.exception.NoProcessableExchangeRateException;
import pe.edu.utp.divisas.model.TipoCambio;

@Component
public class BcrpTipoCambioAdapter {

    public static final String FUENTE = "BCRPData";

    private static final int MAX_INTENTOS = 6;
    private static final long PRESUPUESTO_MS = 12_000;

    private static final Logger log = LoggerFactory.getLogger(BcrpTipoCambioAdapter.class);

    private static final Map<String, Integer> MESES = Map.ofEntries(
            Map.entry("ene", 1), Map.entry("feb", 2), Map.entry("mar", 3), Map.entry("abr", 4),
            Map.entry("may", 5), Map.entry("jun", 6), Map.entry("jul", 7), Map.entry("ago", 8),
            Map.entry("set", 9), Map.entry("sep", 9), Map.entry("oct", 10), Map.entry("nov", 11),
            Map.entry("dic", 12));

    private final RestClient restClient;
    private final BcrpProperties properties;
    private final ObjectMapper objectMapper;

    public BcrpTipoCambioAdapter(RestClient.Builder restClientBuilder, BcrpProperties properties,
                                 ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public TipoCambio obtenerTipoCambio() {
        String path = properties.consultaPath();
        BcrpUnavailableException ultimo = null;
        // El proveedor a veces responde una pagina HTML intermedia en lugar del JSON; se reintenta.
        long inicio = System.currentTimeMillis();
        for (int intento = 1; intento <= MAX_INTENTOS && System.currentTimeMillis() - inicio < PRESUPUESTO_MS; intento++) {
            log.info("Consultando BCRPData (intento {}/{}): {}{}", intento, MAX_INTENTOS,
                    properties.baseUrl(), path);
            try {
                String cuerpo = restClient.get().uri(path).retrieve().body(String.class);
                if (cuerpo == null || !cuerpo.stripLeading().startsWith("{")) {
                    throw new BcrpUnavailableException("BCRPData no devolvio un cuerpo JSON.");
                }
                TipoCambio tipoCambio = normalizar(cuerpo);
                log.info("Tipo de cambio normalizado: fecha={} compra={} venta={}",
                        tipoCambio.fechaReferencia(), tipoCambio.compra(), tipoCambio.venta());
                return tipoCambio;
            } catch (RestClientException e) {
                ultimo = new BcrpUnavailableException("No fue posible consultar BCRPData.", e);
            } catch (BcrpUnavailableException e) {
                ultimo = e;
            }
            log.warn("Intento {} fallido: {}", intento, ultimo.getMessage());
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw ultimo;
    }

    public BcrpRespuestaCruda consultarCrudo() {
        String path = properties.consultaPath();
        try {
            return restClient.get().uri(path).exchange((req, res) -> {
                String cuerpo = new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return new BcrpRespuestaCruda(properties.baseUrl() + path, res.getStatusCode().value(),
                        String.valueOf(res.getHeaders().getContentType()),
                        cuerpo.stripLeading().startsWith("{"), cuerpo);
            });
        } catch (RestClientException e) {
            throw new BcrpUnavailableException("No fue posible consultar BCRPData.", e);
        }
    }

    TipoCambio normalizar(String json) {
        JsonNode raiz;
        try {
            raiz = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new BcrpUnavailableException("BCRPData devolvio un cuerpo que no es JSON valido.", e);
        }
        JsonNode periodos = raiz == null ? null : raiz.path("periods");
        if (periodos == null || !periodos.isArray() || periodos.isEmpty()) {
            throw new NoProcessableExchangeRateException("BCRPData no devolvio periodos.");
        }
        // Recorre del periodo mas reciente al mas antiguo y toma el primero con valores numericos
        // (el proveedor puede devolver "n.d." en los dias sin dato).
        for (int i = periodos.size() - 1; i >= 0; i--) {
            JsonNode periodo = periodos.get(i);
            JsonNode valores = periodo.path("values");
            if (!valores.isArray() || valores.size() < 2) {
                continue;
            }
            BigDecimal compra = parsearNumero(valores.get(0).asText());
            BigDecimal venta = parsearNumero(valores.get(1).asText());
            LocalDate fecha = parsearFecha(periodo.path("name").asText());
            if (compra != null && venta != null && fecha != null) {
                return new TipoCambio(fecha, compra, venta, FUENTE);
            }
            log.debug("Periodo '{}' descartado (no procesable)", periodo.path("name").asText());
        }
        throw new NoProcessableExchangeRateException(
                "BCRPData respondio, pero ningun periodo tiene compra y venta numericas.");
    }

    private static BigDecimal parsearNumero(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim().replace(',', '.');
        try {
            BigDecimal valor = new BigDecimal(limpio);
            return valor.signum() > 0 ? valor : null;
        } catch (NumberFormatException e) {
            return null; // por ejemplo "n.d."
        }
    }

    // Nombre de periodo con formato dd.Mmm.yy, por ejemplo 12.Ago.26.
    private static LocalDate parsearFecha(String nombre) {
        String[] partes = nombre == null ? new String[0] : nombre.trim().split("\\.");
        if (partes.length != 3) {
            return null;
        }
        try {
            Integer mes = MESES.get(partes[1].toLowerCase(Locale.ROOT));
            if (mes == null) {
                return null;
            }
            return LocalDate.of(2000 + Integer.parseInt(partes[2]), mes, Integer.parseInt(partes[0]));
        } catch (NumberFormatException | java.time.DateTimeException e) {
            return null;
        }
    }
}
