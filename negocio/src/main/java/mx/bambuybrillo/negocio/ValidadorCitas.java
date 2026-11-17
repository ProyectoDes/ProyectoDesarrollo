package mx.bambuybrillo.negocio;
import mx.bambuybrillo.entidad.Cita;
import java.time.LocalTime;
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

    public static ResultadoVerificacion verificarDisponibilidad(String nuevaFecha, String horaInicioStr, String horaFinStr, List<Cita> citasExistentes) {
        LocalTime nuevoInicio = LocalTime.parse(horaInicioStr);
        LocalTime nuevoFin = LocalTime.parse(horaFinStr);

        if (!nuevoInicio.isBefore(nuevoFin)) {
            return new ResultadoVerificacion("ocupado", "La hora de inicio debe ser anterior a la hora de fin.");
        }
        if (nuevoInicio.isBefore(INICIO_LABORAL) || nuevoFin.isAfter(FIN_LABORAL)) {
            return new ResultadoVerificacion("ocupado", "El horario está fuera del horario laboral (" + INICIO_LABORAL + " a " + FIN_LABORAL + ").");
        }

        for (Cita cita : citasExistentes) {
            if (cita.getFecha().equals(nuevaFecha)) {
                LocalTime citaInicio = LocalTime.parse(cita.getHoraInicio());
                LocalTime citaFin = LocalTime.parse(cita.getHoraFin());
                boolean hayTraslape = nuevoInicio.isBefore(citaFin) && nuevoFin.isAfter(citaInicio);

                if (hayTraslape) {
                    String nombreCliente = cita.getCliente() != null ? cita.getCliente() : "Cliente ID: " + cita.getIdCliente();
                    return new ResultadoVerificacion("ocupado", "Coincide con " + nombreCliente + ", de " + cita.getHoraInicio() + " a " + cita.getHoraFin() + ". Cambia la hora antes de guardar.");
                }
            }
        }
        return new ResultadoVerificacion("libre", "El horario se encuentra disponible.");
    }
}
