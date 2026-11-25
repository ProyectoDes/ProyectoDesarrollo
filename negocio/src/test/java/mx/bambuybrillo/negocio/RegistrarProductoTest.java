package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarProductoTest {

    // Usa el mismo repositorio falso de ModificarProductoTest
    private ModificarProductoTest.RepositorioFalso repositorio;
    private RegistrarProducto registrar;

    @BeforeEach
    void preparar() {
        repositorio = new ModificarProductoTest.RepositorioFalso();
        registrar = new RegistrarProducto(repositorio);
    }

    private void verificarRechazo(String nombre, String cantidad, String campo, String mensaje) {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> registrar.registrar(nombre, "", cantidad));
        assertEquals(campo, e.getCampo());
        assertEquals(mensaje, e.getMessage());
        assertEquals(0, repositorio.listar().size());
    }

    @Test
    void productoValidoSeGuarda() {
        assertEquals("Producto registrado correctamente.",
                registrar.registrar("Gel limpiador de aloe", "Limpiador facial suave", "10"));
        assertEquals(1, repositorio.listar().size());
        assertEquals(10, repositorio.listar().get(0).getCantidad());
    }

    @Test
    void nombreYDescripcionSeGuardanSinEspaciosSobrantes() {
        registrar.registrar("   Sérum hialurónico   ", "   ", " 3 ");
        Producto guardado = repositorio.listar().get(0);
        assertEquals("Sérum hialurónico", guardado.getNombre());
        assertNull(guardado.getDescripcion());
        assertEquals(3, guardado.getCantidad());
    }

    @Test
    void cantidadCeroEsValida() {
        registrar.registrar("Toallas", null, "0");
        assertEquals(1, repositorio.listar().size());
    }

    @Test
    void nombreVacioSeRechaza() {
        verificarRechazo("   ", "5", "nombre", "El nombre del producto es obligatorio.");
    }

    @Test
    void nombreMuyLargoSeRechaza() {
        verificarRechazo("a".repeat(61), "5", "nombre", "El nombre no puede tener más de 60 caracteres.");
    }

    @Test
    void cantidadVaciaSeRechaza() {
        verificarRechazo("Toallas", "", "cantidad", "La cantidad es obligatoria.");
    }

    @Test
    void cantidadConLetrasSeRechaza() {
        verificarRechazo("Toallas", "diez", "cantidad", "La cantidad debe ser un número entero.");
    }

    @Test
    void cantidadNegativaSeRechaza() {
        verificarRechazo("Toallas", "-1", "cantidad", "La cantidad no puede ser negativa.");
    }

    @Test
    void nombreDuplicadoSeRechazaSinImportarMayusculas() {
        registrar.registrar("Toallas", null, "5");
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> registrar.registrar("TOALLAS", null, "2"));
        assertEquals("nombre", e.getCampo());
        assertEquals("Ya existe un producto con ese nombre.", e.getMessage());
        assertEquals(1, repositorio.listar().size());
    }
}
