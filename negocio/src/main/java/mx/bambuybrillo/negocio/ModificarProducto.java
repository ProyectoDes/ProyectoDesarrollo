package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;

public class ModificarProducto {

    private static final int LARGO_MAXIMO_NOMBRE = 60;

    private final ProductoRepositorio repositorio;

    public ModificarProducto(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Carga los datos actuales del producto para mostrarlos en la pantalla de edición
    public Producto buscar(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("El producto a modificar no existe.");
        }
        return repositorio.buscarPorId(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto ya no existe."));
    }

    public String modificar(Producto producto) {
        if (producto == null || producto.getIdProducto() <= 0) {
            throw new IllegalArgumentException("El producto a modificar no existe.");
        }
        validar(producto);
        producto.setNombre(producto.getNombre().trim());
        if (producto.getDescripcion() != null) {
            String descripcion = producto.getDescripcion().trim();
            producto.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        }
        if (!repositorio.actualizar(producto)) {
            throw new IllegalArgumentException("El producto ya no existe.");
        }
        return "Producto actualizado correctamente.";
    }

    // Mismas reglas que al registrar un producto
    public void validar(Producto producto) {
        String nombre = producto.getNombre() == null ? "" : producto.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ValidacionException("nombre", "El nombre del producto es obligatorio.");
        }
        if (nombre.length() > LARGO_MAXIMO_NOMBRE) {
            throw new ValidacionException("nombre",
                    "El nombre no puede tener más de " + LARGO_MAXIMO_NOMBRE + " caracteres.");
        }
        if (producto.getCantidad() < 0) {
            throw new ValidacionException("cantidad", "La cantidad no puede ser negativa.");
        }
        if (repositorio.existeNombreEnOtroProducto(nombre, producto.getIdProducto())) {
            throw new ValidacionException("nombre", "Ya existe otro producto con ese nombre.");
        }
    }
}
