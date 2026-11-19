package mx.bambuybrillo.negocio;

import java.math.BigDecimal;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;

public class RegistrarProducto {

    private final ProductoRepositorio repositorio;

    public RegistrarProducto(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String registrar(Producto producto) {
        validar(producto);
        producto.setNombre(producto.getNombre().trim());
        if (producto.getDescripcion() != null) {
            String descripcion = producto.getDescripcion().trim();
            producto.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        }
        repositorio.guardar(producto);
        return "Producto registrado correctamente.";
    }

    private void validar(Producto producto) {
        if (producto == null || producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (producto.getCantidad() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio debe ser un número mayor o igual a cero.");
        }
    }
}
