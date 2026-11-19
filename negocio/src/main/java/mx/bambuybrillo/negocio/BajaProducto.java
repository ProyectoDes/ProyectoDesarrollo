package mx.bambuybrillo.negocio;

import mx.bambuybrillo.persistencia.ProductoRepositorio;

public class BajaProducto {

    private final ProductoRepositorio repositorio;

    public BajaProducto(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String eliminar(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("El producto a eliminar no existe.");
        }
        if (repositorio.estaLigadoACita(idProducto)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el producto porque está ligado a una cita.");
        }
        if (!repositorio.eliminar(idProducto)) {
            throw new IllegalArgumentException("El producto a eliminar no existe.");
        }
        return "Producto eliminado correctamente.";
    }
}
