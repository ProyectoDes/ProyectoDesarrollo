package mx.bambuybrillo.negocio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.persistencia.CitaRepositorio;

public class RegistrarCita {

    private static final int MAX_SERVICIO = 60;

    private final CitaRepositorio repositorio;

    public RegistrarCita(CitaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String registrar(Cita cita) {
        validar(cita);
        repositorio.guardar(cita);
        return "Cita registrada correctamente.";
    }

    public void validar(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException("Los datos de la cita son obligatorios.");
        }
        if (cita.getIdCliente() <= 0) {
            throw new IllegalArgumentException("Selecciona un cliente.");
        }
        if (!repositorio.existeCliente(cita.getIdCliente())) {
            throw new IllegalArgumentException("El cliente seleccionado no está registrado.");
        }
        leerFecha(cita.getFecha());
        LocalTime inicio = leerHora(cita.getHoraInicio(), "La hora de inicio");
        LocalTime fin = leerHora(cita.getHoraFin(), "La hora de fin");
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        String servicio = cita.getServicio() == null ? "" : cita.getServicio().trim();
        if (servicio.isEmpty()) {
            throw new IllegalArgumentException("Selecciona o escribe un servicio.");
        }
        if (servicio.length() > MAX_SERVICIO) {
            throw new IllegalArgumentException("El servicio no puede tener más de 60 caracteres.");
        }
    }

    private void leerFecha(String texto) {
        try {
            LocalDate.parse(texto == null ? "" : texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Indica una fecha válida.");
        }
    }

    private LocalTime leerHora(String texto, String campo) {
        try {
            return LocalTime.parse(texto == null ? "" : texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(campo + " no es válida.");
        }
    }
}
