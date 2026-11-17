package pe.edu.utp.divisas.model;

import java.util.regex.Pattern;

public enum TipoDocumento {
    DNI("\\d{8}", "El DNI debe tener 8 dígitos."),
    CE("\\d{9,12}", "El carné de extranjería debe tener entre 9 y 12 dígitos."),
    PASAPORTE("[A-Z0-9]{6,12}", "El pasaporte debe tener entre 6 y 12 letras o números.");

    private final Pattern formato;
    private final String mensaje;

    TipoDocumento(String formato, String mensaje) {
        this.formato = Pattern.compile(formato);
        this.mensaje = mensaje;
    }

    public boolean admite(String numero) {
        return formato.matcher(numero).matches();
    }

    public String mensajeFormato() {
        return mensaje;
    }
}
