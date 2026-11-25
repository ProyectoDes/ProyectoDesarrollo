package mx.bambuybrillo.persistencia;

import java.util.List;
import java.util.Optional;

import mx.bambuybrillo.entidad.Producto;

public interface ProductoRepositorio {
    List<Producto> listar();

    Optional<Producto> buscarPorId(int idProducto);

    // Regresa true si se actualizó algún producto
    boolean actualizar(Producto producto);

    // Revisa si otro producto (distinto al indicado) ya usa ese nombre
    boolean existeNombreEnOtroProducto(String nombre, int idProducto);
}
