package mx.bambuybrillo.negocio;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.entidad.CitaConCliente;
import mx.bambuybrillo.entidad.ClienteOpcion;
import mx.bambuybrillo.persistencia.CitaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarCitaTest {

    static class RepositorioFalso implements CitaRepositorio {
        int guardadas = 0;
        Cita ultima;
        boolean clienteExiste = true;
        boolean traslape = false;

        @Override
        public void guardar(Cita cita) {
            guardadas++;
            ultima = cita;
        }

        @Override
        public void actualizar(Cita cita) {
        }

        @Override
        public List<CitaConCliente> listarConCliente() {
            return new ArrayList<>();
        }

        @Override
        public void eliminar(int idCita) {
        }

        @Override
        public List<ClienteOpcion> listarClientes() {
            return new ArrayList<>();
        }

        @Override
        public boolean existeCliente(int idCliente) {
            return clienteExiste;
        }

        @Override
        public boolean hayTraslape(String fecha, String horaInicio, String horaFin, int idCitaExcluir) {
            return traslape;
        }
    }

    private static final ZoneId ZONA = ZoneId.of("America/Tijuana");

    private RepositorioFalso repositorio;
    private RegistrarCita registrar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        registrar = new RegistrarCita(repositorio);
    }

    private String manana() {
        return LocalDate.now(ZONA).plusDays(1).toString();
    }

    private Cita citaValida() {
        return new Cita(0, 1, manana(), "10:00", "11:00", "Limpieza facial");
    }

    private void verificarFalla(Cita cita, String campo, String mensaje) {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> registrar.registrar(cita));
        assertEquals(campo, e.getCampo());
        assertEquals(mensaje, e.getMessage());
        assertEquals(0, repositorio.guardadas);
    }

    @Test
    void citaNulaFalla() {
        verificarFalla(null, "cita", "Los datos de la cita son obligatorios.");
    }

    @Test
    void clienteSinSeleccionarFalla() {
        Cita cita = citaValida();
        cita.setIdCliente(0);
        verificarFalla(cita, "cliente", "Selecciona un cliente.");
    }

    @Test
    void clienteInexistenteFalla() {
        repositorio.clienteExiste = false;
        verificarFalla(citaValida(), "cliente", "El cliente seleccionado no está registrado.");
    }

    @Test
    void fechaAnteriorAHoyFalla() {
        Cita cita = citaValida();
        cita.setFecha("2020-01-01");
        verificarFalla(cita, "fecha", "La fecha no puede ser anterior a hoy.");
    }

    @Test
    void fechaInvalidaFalla() {
        Cita cita = citaValida();
        cita.setFecha("2026-13-45");
        verificarFalla(cita, "fecha", "Indica una fecha válida.");
    }

    @Test
    void fechaDeHoyEsValida() {
        Cita cita = citaValida();
        cita.setFecha(LocalDate.now(ZONA).toString());
        assertEquals("Cita registrada correctamente.", registrar.registrar(cita));
        assertEquals(1, repositorio.guardadas);
    }

    @Test
    void horaInicioInvalidaFalla() {
        Cita cita = citaValida();
        cita.setHoraInicio("25:00");
        verificarFalla(cita, "horaInicio", "La hora de inicio no es válida.");
    }

    @Test
    void horaFinInvalidaFalla() {
        Cita cita = citaValida();
        cita.setHoraFin("abc");
        verificarFalla(cita, "horaFin", "La hora de fin no es válida.");
    }

    @Test
    void horarioEncimadoConOtraCitaFalla() {
        repositorio.traslape = true;
        verificarFalla(citaValida(), "horario", "Ese horario se encima con otra cita. Elige otra hora.");
    }

    @Test
    void horaFinIgualAInicioFalla() {
        Cita cita = citaValida();
        cita.setHoraFin("10:00");
        verificarFalla(cita, "horaFin", "La hora de fin debe ser posterior a la hora de inicio.");
    }

    @Test
    void horaFinMenorQueInicioFalla() {
        Cita cita = citaValida();
        cita.setHoraInicio("11:00");
        cita.setHoraFin("10:00");
        verificarFalla(cita, "horaFin", "La hora de fin debe ser posterior a la hora de inicio.");
    }

    @Test
    void servicioVacioFalla() {
        Cita cita = citaValida();
        cita.setServicio("   ");
        verificarFalla(cita, "servicio", "Selecciona o escribe un servicio.");
    }

    @Test
    void servicioMuyLargoFalla() {
        Cita cita = citaValida();
        cita.setServicio("a".repeat(61));
        verificarFalla(cita, "servicio", "El servicio no puede tener más de 60 caracteres.");
    }

    @Test
    void citaValidaSeGuarda() {
        assertEquals("Cita registrada correctamente.", registrar.registrar(citaValida()));
        assertEquals(1, repositorio.guardadas);
    }

    @Test
    void horaInicioFueraDeMediaHoraFalla() {
        Cita cita = citaValida();
        cita.setHoraInicio("10:15");
        verificarFalla(cita, "horaInicio", "La hora de inicio debe ser en punto o y media.");
    }

    @Test
    void horaFinFueraDeMediaHoraFalla() {
        Cita cita = citaValida();
        cita.setHoraFin("11:10");
        verificarFalla(cita, "horaFin", "La hora de fin debe ser en punto o y media.");
    }

    @Test
    void horasEnPuntoYEnMediaSonValidas() {
        Cita cita = citaValida();
        cita.setHoraInicio("10:30");
        cita.setHoraFin("11:30");
        assertEquals("Cita registrada correctamente.", registrar.registrar(cita));
        assertEquals(1, repositorio.guardadas);
    }

    @Test
    void productosElegidosSeGuardanSinRepetidosNiInvalidos() {
        Cita cita = citaValida();
        cita.setProductos(new ArrayList<>(List.of(3, 7, 3, 0, -2, 12)));
        registrar.registrar(cita);
        assertEquals(List.of(3, 7, 12), repositorio.ultima.getProductos());
    }

    @Test
    void citaSinProductosSeGuarda() {
        registrar.registrar(citaValida());
        assertEquals(1, repositorio.guardadas);
        assertTrue(repositorio.ultima.getProductos().isEmpty());
    }
}
