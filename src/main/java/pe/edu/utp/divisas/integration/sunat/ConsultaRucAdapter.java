package pe.edu.utp.divisas.integration.sunat;

import org.springframework.stereotype.Component;
import pe.edu.utp.divisas.exception.SunatNoIntegradoException;

@Component
public class ConsultaRucAdapter {

    public void consultarRuc(String ruc) {
        throw new SunatNoIntegradoException(
                "La consulta de RUC en SUNAT no esta disponible: no existe un servicio SOAP publico de "
                        + "consulta de RUC sin credenciales. El registro de usuarios sigue funcionando; "
                        + "el RUC solo se valida por formato.");
    }
}
