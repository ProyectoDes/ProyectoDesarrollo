package mx.bambuybrillo.persistencia;

import java.util.List;
import java.util.Optional;

import mx.bambuybrillo.entidad.Producto;

public interface ProductoRepositorio {

    void guardar(Producto producto);

    List<Producto> listar();

    Optional<Producto> buscarPorId(int idProducto);

    boolean actualizar(Producto producto);

    boolean eliminar(int idProducto);

    boolean estaLigadoACita(int idProducto);
}
