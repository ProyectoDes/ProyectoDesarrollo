package mx.bambuybrillo.persistencia;

// La base de datos no dejó borrar el producto porque alguna cita lo usa
public class ProductoLigadoException extends RuntimeException {

    public ProductoLigadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
