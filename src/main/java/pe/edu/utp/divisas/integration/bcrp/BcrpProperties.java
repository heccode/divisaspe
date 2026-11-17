package pe.edu.utp.divisas.integration.bcrp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bcrp")
public record BcrpProperties(String baseUrl, Series series) {

    public record Series(String compra, String venta) {
    }

    public String consultaPath() {
        return "/" + series.compra() + "-" + series.venta() + "/json";
    }
}
