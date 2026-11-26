package mx.bambuybrillo.negocio;

import java.time.LocalDate;
import java.time.ZoneId;

import mx.bambuybrillo.entidad.Cita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModificarCitaTest {

    static class RepositorioFalso extends RegistrarCitaTest.RepositorioFalso {
        int actualizadas = 0;

        @Override
        public void actualizar(Cita cita) {
            actualizadas++;
        }
    }

    private static final ZoneId ZONA = ZoneId.of("America/Tijuana");

    private RepositorioFalso repositorio;
    private ModificarCita modificar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        modificar = new ModificarCita(repositorio);
    }

    private String manana() {
        return LocalDate.now(ZONA).plusDays(1).toString();
    }

    @Test
    void citaValidaSeActualiza() {
        Cita cita = new Cita(5, 1, manana(), "10:00", "11:00", "Manicure");
        assertEquals("Cita actualizada correctamente.", modificar.modificar(cita));
        assertEquals(1, repositorio.actualizadas);
    }

    @Test
    void citaSinIdFalla() {
        Cita cita = new Cita(0, 1, manana(), "10:00", "11:00", "Manicure");
        assertThrows(IllegalArgumentException.class, () -> modificar.modificar(cita));
        assertEquals(0, repositorio.actualizadas);
    }

    @Test
    void horaFinAntesDeInicioFalla() {
        Cita cita = new Cita(5, 1, manana(), "11:00", "10:00", "Manicure");
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> modificar.modificar(cita));
        assertEquals("horaFin", e.getCampo());
        assertEquals(0, repositorio.actualizadas);
    }

    @Test
    void servicioVacioFalla() {
        Cita cita = new Cita(5, 1, manana(), "10:00", "11:00", "  ");
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> modificar.modificar(cita));
        assertEquals("servicio", e.getCampo());
        assertEquals(0, repositorio.actualizadas);
    }
}
