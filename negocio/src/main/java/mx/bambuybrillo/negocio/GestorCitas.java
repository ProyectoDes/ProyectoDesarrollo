package mx.bambuybrillo.negocio;
import mx.bambuybrillo.persistencia.CitaRepositorio;
import mx.bambuybrillo.entidad.Cita;

import java.util.List;

public class GestorCitas {
    private CitaRepositorio repositorio;
    private List<Cita> citasExistentes;

    public GestorCitas(CitaRepositorio repositorio, List<Cita> citasExistentes) {
        this.repositorio = repositorio;
        this.citasExistentes = citasExistentes;
    }

    public boolean registrarCita(Cita nuevaCita) {
        ValidadorCitas.ResultadoVerificacion resultado = ValidadorCitas.verificarDisponibilidad(
                nuevaCita.getFecha(), nuevaCita.getHoraInicio(), nuevaCita.getHoraFin(), citasExistentes
        );

        if (resultado.getEstado().equalsIgnoreCase("ocupado")) {
            System.out.println("[ALERTA] " + resultado.getMensaje());
            return false;
        }

        repositorio.guardar(nuevaCita);
        citasExistentes.add(nuevaCita);
        System.out.println("¡Cita registrada con éxito!");
        return true;
    }
}