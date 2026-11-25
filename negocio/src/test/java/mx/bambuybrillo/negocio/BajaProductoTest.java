package mx.bambuybrillo.negocio;

import java.util.HashSet;
import java.util.Set;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoLigadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BajaProductoTest {

    // Usa el mismo repositorio falso de ModificarProductoTest y agrega citas ligadas
    static class RepositorioFalso extends ModificarProductoTest.RepositorioFalso {
        final Set<Integer> ligadosACita = new HashSet<>();
        // Simula que la base rechaza el DELETE por la llave foránea de cita_producto
        boolean baseRechazaBorrado = false;

        @Override
        public boolean eliminar(int idProducto) {
            if (baseRechazaBorrado) {
                throw new ProductoLigadoException("El producto está ligado a una cita.", null);
            }
            return productos.removeIf(p -> p.getIdProducto() == idProducto);
        }

        @Override
        public boolean estaLigadoACita(int idProducto) {
            return ligadosACita.contains(idProducto);
        }
    }

    private RepositorioFalso repositorio;
    private BajaProducto baja;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        repositorio.productos.add(new Producto(1, "Gel limpiador de aloe", null, 10));
        repositorio.productos.add(new Producto(2, "Sérum hialurónico", null, 10));
        baja = new BajaProducto(repositorio);
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
        assertEquals(1, repositorio.listar().size());
        assertTrue(repositorio.buscarPorId(1).isEmpty());
    }

    @Test
    void idCeroSeRechaza() {
        verificarRechazo(0, "El producto a eliminar no existe.");
    }

    @Test
    void productoInexistenteSeRechaza() {
        verificarRechazo(99, "El producto a eliminar no existe.");
    }

    @Test
    void productoLigadoACitaNoSeElimina() {
        repositorio.ligadosACita.add(1);
        verificarRechazo(1, BajaProducto.MENSAJE_LIGADO);
    }

    @Test
    void siLaBaseRechazaElBorradoSeAvisaQueEstaLigado() {
        repositorio.baseRechazaBorrado = true;
        verificarRechazo(1, BajaProducto.MENSAJE_LIGADO);
    }
}
