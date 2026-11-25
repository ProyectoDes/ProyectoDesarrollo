package mx.bambuybrillo.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarProductoTest {

    // Repositorio falso: guarda en una lista para no usar la base de datos
    static class RepositorioFalso implements ProductoRepositorio {
        final List<Producto> guardados = new ArrayList<>();
        boolean nombreExiste = false;

        @Override
        public void guardar(Producto producto) {
            guardados.add(producto);
        }

        @Override
        public List<Producto> listar() {
            return new ArrayList<>(guardados);
        }

        @Override
        public Optional<Producto> buscarPorId(int idProducto) {
            return Optional.empty();
        }

        @Override
        public boolean actualizar(Producto producto) {
            return false;
        }

        @Override
        public boolean eliminar(int idProducto) {
            return false;
        }

        @Override
        public boolean estaLigadoACita(int idProducto) {
            return false;
        }

        @Override
        public boolean existeNombre(String nombre) {
            return nombreExiste;
        }
    }

    private RepositorioFalso repositorio;
    private RegistrarProducto registrar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        registrar = new RegistrarProducto(repositorio);
    }

    private Producto productoValido() {
        return new Producto(0, "Gel limpiador de aloe", "Limpiador facial suave",
                10, new BigDecimal("149.90"));
    }

    private void verificarRechazo(Producto producto, String mensaje) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> registrar.registrar(producto));
        assertEquals(mensaje, e.getMessage());
        assertEquals(0, repositorio.guardados.size());
    }

    // ---------- Datos válidos: se guarda ----------

    @Test
    void productoValidoSeGuarda() {
        assertEquals("Producto registrado correctamente.", registrar.registrar(productoValido()));
        assertEquals(1, repositorio.guardados.size());
    }

    @Test
    void nombreSeGuardaSinEspaciosSobrantes() {
        Producto producto = productoValido();
        producto.setNombre("   Sérum hialurónico   ");
        registrar.registrar(producto);
        assertEquals("Sérum hialurónico", repositorio.guardados.get(0).getNombre());
    }

    @Test
    void descripcionVaciaSeGuardaComoNula() {
        Producto producto = productoValido();
        producto.setDescripcion("   ");
        registrar.registrar(producto);
        assertNull(repositorio.guardados.get(0).getDescripcion());
    }

    @Test
    void descripcionNulaEsValida() {
        Producto producto = productoValido();
        producto.setDescripcion(null);
        registrar.registrar(producto);
        assertEquals(1, repositorio.guardados.size());
    }

    @Test
    void cantidadCeroYPrecioCeroSonValidos() {
        Producto producto = productoValido();
        producto.setCantidad(0);
        producto.setPrecio(BigDecimal.ZERO);
        registrar.registrar(producto);
        assertEquals(1, repositorio.guardados.size());
    }

    // ---------- Datos inválidos: se rechaza y no se guarda ----------

    @Test
    void productoNuloSeRechaza() {
        verificarRechazo(null, "El nombre del producto es obligatorio.");
    }

    @Test
    void nombreNuloSeRechaza() {
        Producto producto = productoValido();
        producto.setNombre(null);
        verificarRechazo(producto, "El nombre del producto es obligatorio.");
    }

    @Test
    void nombreSoloEspaciosSeRechaza() {
        Producto producto = productoValido();
        producto.setNombre("   ");
        verificarRechazo(producto, "El nombre del producto es obligatorio.");
    }

    @Test
    void cantidadNegativaSeRechaza() {
        Producto producto = productoValido();
        producto.setCantidad(-1);
        verificarRechazo(producto, "La cantidad no puede ser negativa.");
    }

    @Test
    void precioNuloSeRechaza() {
        Producto producto = productoValido();
        producto.setPrecio(null);
        verificarRechazo(producto, "El precio debe ser un número mayor o igual a cero.");
    }

    @Test
    void precioNegativoSeRechaza() {
        Producto producto = productoValido();
        producto.setPrecio(new BigDecimal("-0.01"));
        verificarRechazo(producto, "El precio debe ser un número mayor o igual a cero.");
    }

    @Test
    void nombreDuplicadoSeRechaza() {
        repositorio.nombreExiste = true;
        verificarRechazo(productoValido(), "Ya existe un producto con ese nombre.");
    }
}