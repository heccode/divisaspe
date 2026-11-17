package pe.edu.utp.divisas.exception;

public class NoProcessableExchangeRateException extends RuntimeException {

    public NoProcessableExchangeRateException(String message) {
        super(message);
    }
}
