package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModificarProductoTest {

    static class RepositorioFalso implements ProductoRepositorio {
        final List<Producto> productos = new ArrayList<>();
        int actualizados = 0;

        @Override
        public List<Producto> listar() {
            return productos;
        }

        @Override
        public Optional<Producto> buscarPorId(int idProducto) {
            return productos.stream().filter(p -> p.getIdProducto() == idProducto).findFirst();
        }

        @Override
        public boolean actualizar(Producto producto) {
            for (int i = 0; i < productos.size(); i++) {
                if (productos.get(i).getIdProducto() == producto.getIdProducto()) {
                    productos.set(i, producto);
                    actualizados++;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean existeNombreEnOtroProducto(String nombre, int idProducto) {
            return productos.stream().anyMatch(p -> p.getIdProducto() != idProducto
                    && p.getNombre().trim().equalsIgnoreCase(nombre.trim()));
        }

        @Override
        public boolean eliminar(int idProducto) {
            return productos.removeIf(p -> p.getIdProducto() == idProducto);
        }

        @Override
        public boolean estaLigadoACita(int idProducto) {
            return false;
        }
    }

    private RepositorioFalso repositorio;
    private ModificarProducto modificar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        repositorio.productos.add(new Producto(1, "Gel limpiador de aloe", "Cuidado facial", 12));
        repositorio.productos.add(new Producto(2, "Aceite de masaje", null, 4));
        modificar = new ModificarProducto(repositorio);
    }

    @Test
    void cargaLosDatosActualesDelProducto() {
        Producto producto = modificar.buscar(1);
        assertEquals("Gel limpiador de aloe", producto.getNombre());
        assertEquals(12, producto.getCantidad());
    }

    @Test
    void productoInexistenteNoSeCarga() {
        assertThrows(IllegalArgumentException.class, () -> modificar.buscar(99));
    }

    @Test
    void productoValidoSeActualiza() {
        Producto cambios = new Producto(1, "  Gel de aloe  ", "  ", 8);
        assertEquals("Producto actualizado correctamente.", modificar.modificar(cambios));
        Producto guardado = repositorio.buscarPorId(1).orElseThrow();
        assertEquals("Gel de aloe", guardado.getNombre());
        assertNull(guardado.getDescripcion());
        assertEquals(8, guardado.getCantidad());
    }

    @Test
    void puedeConservarSuMismoNombre() {
        Producto cambios = new Producto(1, "Gel limpiador de aloe", "Cuidado facial", 20);
        modificar.modificar(cambios);
        assertEquals(1, repositorio.actualizados);
    }

    @Test
    void nombreVacioFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> modificar.modificar(new Producto(1, "   ", null, 5)));
        assertEquals("nombre", e.getCampo());
        assertEquals(0, repositorio.actualizados);
    }

    @Test
    void cantidadNegativaFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> modificar.modificar(new Producto(1, "Gel limpiador de aloe", null, -1)));
        assertEquals("cantidad", e.getCampo());
        assertEquals(0, repositorio.actualizados);
    }

    @Test
    void nombreDeOtroProductoFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> modificar.modificar(new Producto(1, "aceite de masaje", null, 5)));
        assertEquals("nombre", e.getCampo());
        assertEquals(0, repositorio.actualizados);
    }

    @Test
    void productoSinIdFalla() {
        assertThrows(IllegalArgumentException.class,
                () -> modificar.modificar(new Producto(0, "Gel", null, 5)));
        assertEquals(0, repositorio.actualizados);
    }

    @Test
    void productoBorradoNoSeActualiza() {
        assertThrows(IllegalArgumentException.class,
                () -> modificar.modificar(new Producto(99, "Crema", null, 5)));
    }
}
