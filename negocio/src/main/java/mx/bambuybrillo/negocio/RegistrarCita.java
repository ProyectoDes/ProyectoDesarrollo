package mx.bambuybrillo.negocio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.persistencia.CitaRepositorio;

public class RegistrarCita {

    private static final int MAX_SERVICIO = 60;
    private static final ZoneId ZONA = ZoneId.of("America/Tijuana");   // ← línea nueva

    private final CitaRepositorio repositorio;

    public RegistrarCita(CitaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String registrar(Cita cita) {
        validar(cita);
        cita.setProductos(limpiarProductos(cita.getProductos()));
        repositorio.guardar(cita);
        return "Cita registrada correctamente.";
    }

    public void validar(Cita cita) {
        if (cita == null) {
            throw new ValidacionException("cita", "Los datos de la cita son obligatorios.");
        }
        if (cita.getIdCliente() <= 0) {
            throw new ValidacionException("cliente", "Selecciona un cliente.");
        }
        if (!repositorio.existeCliente(cita.getIdCliente())) {
            throw new ValidacionException("cliente", "El cliente seleccionado no está registrado.");
        }
        LocalDate fecha = leerFecha(cita.getFecha());
        if (fecha.isBefore(LocalDate.now(ZONA))) {
            throw new ValidacionException("fecha", "La fecha no puede ser anterior a hoy.");
        }
        LocalTime inicio = leerHora(cita.getHoraInicio(), "La hora de inicio", "horaInicio");
        LocalTime fin = leerHora(cita.getHoraFin(), "La hora de fin", "horaFin");

        if (!esMediaHora(inicio)) {
            throw new ValidacionException("horaInicio", "La hora de inicio debe ser en punto o y media.");
        }
        if (!esMediaHora(fin)) {
            throw new ValidacionException("horaFin", "La hora de fin debe ser en punto o y media.");
        }

        if (!fin.isAfter(inicio)) {
            throw new ValidacionException("horaFin", "La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (repositorio.hayTraslape(fecha.toString(), inicio.toString(), fin.toString(), cita.getIdCita())) {
            throw new ValidacionException("horario",
                    "Ese horario se encima con otra cita. Elige otra hora.");
        }
        String servicio = cita.getServicio() == null ? "" : cita.getServicio().trim();
        if (servicio.isEmpty()) {
            throw new ValidacionException("servicio", "Selecciona o escribe un servicio.");
        }
        if (servicio.length() > MAX_SERVICIO) {
            throw new ValidacionException("servicio", "El servicio no puede tener más de 60 caracteres.");
        }
    }

    private LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto == null ? "" : texto);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("fecha", "Indica una fecha válida.");
        }
    }

    private LocalTime leerHora(String texto, String descripcion, String campo) {
        try {
            return LocalTime.parse(texto == null ? "" : texto);
        } catch (DateTimeParseException e) {
            throw new ValidacionException(campo, descripcion + " no es válida.");
        }
    }

    // Quita repetidos y id inválidos de los productos elegidos
    private List<Integer> limpiarProductos(List<Integer> productos) {
        List<Integer> limpios = new ArrayList<>(new LinkedHashSet<>(productos));
        limpios.removeIf(id -> id == null || id <= 0);
        return limpios;
    }

    private boolean esMediaHora(LocalTime hora) {
        return hora.getSecond() == 0 && hora.getMinute() % 30 == 0;
    }
}
