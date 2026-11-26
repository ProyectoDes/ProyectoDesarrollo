package mx.bambuybrillo.negocio;

// Usuario o contraseña incorrectos (la API responde 401)
public class CredencialesIncorrectasException extends RuntimeException {

    public CredencialesIncorrectasException(String mensaje) {
        super(mensaje);
    }
}
