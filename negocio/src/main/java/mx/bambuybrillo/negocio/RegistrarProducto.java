package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;

public class RegistrarProducto {

    private final ProductoRepositorio repositorio;

    public RegistrarProducto(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String registrar(String nombre, String descripcion, String cantidadTexto) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCantidad(leerCantidad(cantidadTexto));
        return registrar(producto);
    }

    public String registrar(Producto producto) {
        validar(producto);
        String nombre = producto.getNombre().trim();
        if (repositorio.existeNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre.");
        }
        producto.setNombre(nombre);
        if (producto.getDescripcion() != null) {
            String descripcion = producto.getDescripcion().trim();
            producto.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        }
        repositorio.guardar(producto);
        return "Producto registrado correctamente.";
    }

    private int leerCantidad(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("La cantidad es obligatoria.");
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }
    }

    private void validar(Producto producto) {
        if (producto == null || producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (producto.getCantidad() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
    }
}