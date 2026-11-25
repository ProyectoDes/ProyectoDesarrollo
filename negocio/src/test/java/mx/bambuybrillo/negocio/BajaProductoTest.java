package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BajaProductoTest {

    // Repositorio falso: guarda los productos en una lista
    static class RepositorioFalso implements ProductoRepositorio {
        final List<Producto> productos = new ArrayList<>();
        final Set<Integer> ligadosACita = new HashSet<>();

        @Override
        public void guardar(Producto producto) {
            productos.add(producto);
        }

        @Override
        public List<Producto> listar() {
            return new ArrayList<>(productos);
        }

        @Override
        public Optional<Producto> buscarPorId(int idProducto) {
            return productos.stream().filter(p -> p.getIdProducto() == idProducto).findFirst();
        }

        @Override
        public boolean actualizar(Producto producto) {
            return false;
        }

        @Override
        public boolean eliminar(int idProducto) {
            return productos.removeIf(p -> p.getIdProducto() == idProducto);
        }

        @Override
        public boolean estaLigadoACita(int idProducto) {
            return ligadosACita.contains(idProducto);
        }

        @Override
        public boolean existeNombre(String nombre) {
            return false;
        }
    }

    private RepositorioFalso repositorio;
    private BajaProducto baja;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        baja = new BajaProducto(repositorio);
        agregar(1, "Gel limpiador de aloe");
        agregar(2, "Sérum hialurónico");
    }

    private void agregar(int id, String nombre) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto.setNombre(nombre);
        producto.setCantidad(10);
        repositorio.productos.add(producto);
    }

    private void verificarRechazo(int id, String mensaje) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> baja.eliminar(id));
        assertEquals(mensaje, e.getMessage());
        assertEquals(2, repositorio.listar().size());
    }

    @Test
    void productoSinCitasSeEliminaYDesapareceDelListado() {
        assertEquals("Producto eliminado correctamente.", baja.eliminar(1));

        List<Producto> restantes = repositorio.listar();
        assertEquals(1, restantes.size());
        assertEquals(2, restantes.get(0).getIdProducto());
        assertTrue(repositorio.buscarPorId(1).isEmpty());
    }

    @Test
    void idCeroSeRechaza() {
        verificarRechazo(0, "El producto a eliminar no existe.");
    }

    @Test
    void idNegativoSeRechaza() {
        verificarRechazo(-5, "El producto a eliminar no existe.");
    }

    @Test
    void productoInexistenteSeRechaza() {
        verificarRechazo(99, "El producto a eliminar no existe.");
    }

    @Test
    void productoLigadoACitaNoSeElimina() {
        repositorio.ligadosACita.add(1);
        verificarRechazo(1, "No se puede eliminar el producto porque está ligado a una cita.");
        assertTrue(repositorio.buscarPorId(1).isPresent());
    }
}