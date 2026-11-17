package pe.edu.utp.divisas.exception;

public class BcrpUnavailableException extends RuntimeException {

    public BcrpUnavailableException(String message) {
        super(message);
    }

    public BcrpUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
