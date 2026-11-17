package pe.edu.utp.divisas.integration.bcrp;

public record BcrpRespuestaCruda(String urlConsultada, int estadoHttp, String contentType,
                                 boolean jsonValido, String cuerpo) {
}
