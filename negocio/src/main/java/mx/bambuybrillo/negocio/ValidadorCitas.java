package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Cita;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ValidadorCitas {
    private static final LocalTime INICIO_LABORAL = LocalTime.of(9, 0);
    private static final LocalTime FIN_LABORAL = LocalTime.of(19, 0);

    public static class ResultadoVerificacion {
        private final String estado;
        private final String mensaje;

        public ResultadoVerificacion(String estado, String mensaje) {
            this.estado = estado;
            this.mensaje = mensaje;
        }

        public String getEstado() { return estado; }
        public String getMensaje() { return mensaje; }
    }

    public static ResultadoVerificacion verificarDisponibilidad(
            String nuevaFecha,
            String horaInicioStr,
            String horaFinStr,
            List<Cita> citasExistentes
    ) {
        LocalTime nuevoInicio;
        LocalTime nuevoFin;

        try {
            nuevoInicio = LocalTime.parse(horaInicioStr);
            nuevoFin = LocalTime.parse(horaFinStr);
        } catch (DateTimeParseException e) {
            return new ResultadoVerificacion("ocupado", "Formato de hora inválido. Usa HH:mm (ejemplo: 10:30).");
        }

        if (!nuevoInicio.isBefore(nuevoFin)) {
            return new ResultadoVerificacion("ocupado", "La hora de inicio debe ser anterior a la hora de fin.");
        }

        if (nuevoInicio.isBefore(INICIO_LABORAL) || nuevoFin.isAfter(FIN_LABORAL)) {
            return new ResultadoVerificacion(
                    "ocupado",
                    "El horario está fuera del horario laboral (" + INICIO_LABORAL + " a " + FIN_LABORAL + ")."
            );
        }

        if (citasExistentes != null) {
            for (Cita cita : citasExistentes) {
                if (cita.getFecha() != null && cita.getFecha().equals(nuevaFecha)) {
                    LocalTime citaInicio = LocalTime.parse(cita.getHoraInicio());
                    LocalTime citaFin = LocalTime.parse(cita.getHoraFin());

                    String nombreCliente = (cita.getCliente() != null && !cita.getCliente().isBlank())
                            ? cita.getCliente()
                            : "Cliente ID: " + cita.getIdCliente();

                    // 1. Cita duplicada exacta en la misma fecha y horario
                    boolean mismaHoraExacta = nuevoInicio.equals(citaInicio) && nuevoFin.equals(citaFin);
                    if (mismaHoraExacta) {
                        return new ResultadoVerificacion(
                                "ocupado",
                                "Ya existe una cita registrada en ese mismo horario (" + cita.getHoraInicio() + " a " + cita.getHoraFin() + ") para " + nombreCliente + ". No se puede duplicar."
                        );
                    }

                    // 2. Traslape parcial de horario en la misma fecha
                    boolean hayTraslape = nuevoInicio.isBefore(citaFin) && nuevoFin.isAfter(citaInicio);
                    if (hayTraslape) {
                        return new ResultadoVerificacion(
                                "ocupado",
                                "El horario se empalma con " + nombreCliente + " (" + cita.getHoraInicio() + " a " + cita.getHoraFin() + "). Elige otro horario."
                        );
                    }
                }
            }
        }

        return new ResultadoVerificacion("libre", "El horario se encuentra disponible.");
    }
}
