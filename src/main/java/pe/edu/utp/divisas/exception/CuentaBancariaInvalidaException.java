package pe.edu.utp.divisas.exception;

public class CuentaBancariaInvalidaException extends RuntimeException {

    public CuentaBancariaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
