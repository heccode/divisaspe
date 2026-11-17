package pe.edu.utp.divisas.integration.camel;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import pe.edu.utp.divisas.model.TipoCambio;

@Component
public class CamelTipoCambioProvider implements TipoCambioProvider {

    private final ProducerTemplate producerTemplate;

    public CamelTipoCambioProvider(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public TipoCambio obtenerTipoCambio() {
        try {
            return producerTemplate.requestBody(TipoCambioRoute.ENDPOINT, null, TipoCambio.class);
        } catch (CamelExecutionException e) {
            if (e.getCause() instanceof RuntimeException causa) {
                throw causa;
            }
            throw e;
        }
    }
}
