package pe.edu.utp.divisas.integration.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import pe.edu.utp.divisas.integration.bcrp.BcrpTipoCambioAdapter;

@Component
public class TipoCambioRoute extends RouteBuilder {

    public static final String ENDPOINT = "direct:tipoCambio";

    private final BcrpTipoCambioAdapter adapter;

    public TipoCambioRoute(BcrpTipoCambioAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void configure() {
        from(ENDPOINT)
                .routeId("tipo-cambio-bcrp")
                .bean(adapter, "obtenerTipoCambio");
    }
}
