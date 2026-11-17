package pe.edu.utp.divisas.exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("Ya existe un usuario con el email " + email + ".");
    }
}
