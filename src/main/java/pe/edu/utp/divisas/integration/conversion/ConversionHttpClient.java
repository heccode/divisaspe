package pe.edu.utp.divisas.integration.conversion;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.edu.utp.divisas.exception.InvalidConversionException;
import pe.edu.utp.divisas.exception.ServicioConversionNoDisponibleException;
import pe.edu.utp.divisas.model.ApiError;
import pe.edu.utp.divisas.model.CalculoConversionRequest;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;
import pe.edu.utp.divisas.service.ConversionPort;

@Component
@ConditionalOnExpression("!'${conversion.remote.url:}'.isEmpty()")
public class ConversionHttpClient implements ConversionPort {

    private static final Logger log = LoggerFactory.getLogger(ConversionHttpClient.class);

    private final RestClient restClient;

    public ConversionHttpClient(RestClient.Builder builder, @Value("${conversion.remote.url}") String url) {
        this.restClient = builder.baseUrl(url).build();
        log.info("S01 Conversion se consume por HTTP en {}", url);
    }

    @Override
    public ResultadoConversion convertir(BigDecimal monto, Moneda origen, Moneda destino, TipoCambio tc) {
        CalculoConversionRequest request = new CalculoConversionRequest(
                monto, origen, destino, tc.compra(), tc.venta(), tc.fechaReferencia());
        try {
            return restClient.post().uri("/api/v1/conversion/calcular").body(request)
                    .retrieve().body(ResultadoConversion.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                ApiError error = e.getResponseBodyAs(ApiError.class);
                throw new InvalidConversionException(error != null && error.mensaje() != null
                        ? error.mensaje() : "El servicio de conversion rechazo la solicitud.");
            }
            throw new ServicioConversionNoDisponibleException("El servicio de conversion respondio " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            throw new ServicioConversionNoDisponibleException("No fue posible contactar el servicio de conversion.", e);
        }
    }
}
