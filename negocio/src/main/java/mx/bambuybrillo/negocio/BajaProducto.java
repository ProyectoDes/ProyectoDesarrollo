package mx.bambuybrillo.negocio;

import mx.bambuybrillo.persistencia.ProductoLigadoException;
import mx.bambuybrillo.persistencia.ProductoRepositorio;

public class BajaProducto {

    public static final String MENSAJE_LIGADO =
            "No se puede eliminar el producto porque está ligado a una cita.";

    private final ProductoRepositorio repositorio;

    public BajaProducto(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String eliminar(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("El producto a eliminar no existe.");
        }
        if (repositorio.estaLigadoACita(idProducto)) {
            throw new IllegalArgumentException(MENSAJE_LIGADO);
        }
        boolean eliminado;
        try {
            eliminado = repositorio.eliminar(idProducto);
        } catch (ProductoLigadoException e) {
            // Por si una cita lo usó entre la revisión y el borrado
            throw new IllegalArgumentException(MENSAJE_LIGADO);
        }
        if (!eliminado) {
            throw new IllegalArgumentException("El producto a eliminar no existe.");
        }
        return "Producto eliminado correctamente.";
    }
}
