package pe.edu.utp.divisas.exception;

public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException() {
        super("Inicia sesion para continuar.");
    }
}
