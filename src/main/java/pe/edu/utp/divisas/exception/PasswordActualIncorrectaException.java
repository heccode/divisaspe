package pe.edu.utp.divisas.exception;

public class PasswordActualIncorrectaException extends RuntimeException {

    public PasswordActualIncorrectaException() {
        super("La contraseña actual no es correcta.");
    }
}
