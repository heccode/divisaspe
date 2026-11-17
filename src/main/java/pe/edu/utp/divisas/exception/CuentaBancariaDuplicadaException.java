package pe.edu.utp.divisas.exception;

public class CuentaBancariaDuplicadaException extends RuntimeException {

    public CuentaBancariaDuplicadaException() {
        super("Ya registraste una cuenta con ese CCI para este tipo de operación.");
    }
}
