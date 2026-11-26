package mx.bambuybrillo.persistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import mx.bambuybrillo.entidad.Producto;

public interface ProductoRepositorio {
    // Registra un producto nuevo (queda activo)
    void guardar(Producto producto);

    List<Producto> listar();

    Optional<Producto> buscarPorId(int idProducto);

    // Regresa true si se actualizó algún producto
    boolean actualizar(Producto producto);

    // Revisa si algún producto activo ya usa ese nombre
    boolean existeNombre(String nombre);

    // Revisa si otro producto (distinto al indicado) ya usa ese nombre
    boolean existeNombreEnOtroProducto(String nombre, int idProducto);

    // Da de baja el producto: deja de salir en el inventario, pero sigue en la historia clínica.
    // Regresa true si se dio de baja algún producto
    boolean eliminar(int idProducto);

    // Revisa si el producto está en alguna cita que todavía no pasa
    boolean estaEnCitaPendiente(int idProducto, LocalDate hoy, LocalTime ahora);
}
