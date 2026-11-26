package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mx.bambuybrillo.entidad.RegistroHistoria;
import mx.bambuybrillo.persistencia.HistoriaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgregarObservacionesTest {

    static class RepositorioFalso implements HistoriaRepositorio {
        int guardados = 0;
        String ultimaObservacion;
        List<Integer> ultimosProductos;

        @Override
        public List<RegistroHistoria> listarPorCliente(int idCliente) {
            return new ArrayList<>();
        }

        @Override
        public RegistroHistoria buscarPorCita(int idCita) {
            return null;
        }

        @Override
        public void agregar(int idCita, String observaciones, List<Integer> idsProductos) {
            guardados++;
            ultimaObservacion = observaciones;
            ultimosProductos = idsProductos;
        }
    }

    private RepositorioFalso repositorio;
    private AgregarObservaciones agregar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        agregar = new AgregarObservaciones(repositorio);
    }

    @Test
    void sinObservacionesNiProductosFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> agregar.agregar(3, "   ", new ArrayList<>()));
        assertEquals("observaciones", e.getCampo());
        assertEquals(0, repositorio.guardados);
    }

    @Test
    void observacionMuyLargaFalla() {
        assertThrows(ValidacionException.class,
                () -> agregar.agregar(3, "a".repeat(1001), null));
        assertEquals(0, repositorio.guardados);
    }

    @Test
    void citaInvalidaFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> agregar.agregar(0, "Hidratación", null));
        assertEquals("cita", e.getCampo());
    }

    @Test
    void guardaVariosProductosSinRepetir() {
        agregar.agregar(3, "  Piel sensible  ", Arrays.asList(2, 5, 2));
        assertEquals(1, repositorio.guardados);
        assertEquals("Piel sensible", repositorio.ultimaObservacion);
        assertEquals(Arrays.asList(2, 5), repositorio.ultimosProductos);
    }

    @Test
    void soloProductosTambienSeGuarda() {
        agregar.agregar(3, "", Arrays.asList(4));
        assertEquals(1, repositorio.guardados);
        assertEquals("", repositorio.ultimaObservacion);
    }
}
