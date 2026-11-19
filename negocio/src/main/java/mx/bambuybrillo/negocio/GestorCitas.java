package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.persistencia.CitaRepositorio;

import java.util.ArrayList;
import java.util.List;

public class GestorCitas {
    private final CitaRepositorio repositorio;
    private final List<Cita> citasExistentes;

    public GestorCitas(CitaRepositorio repositorio, List<Cita> citasExistentes) {
        this.repositorio = repositorio;
        this.citasExistentes = citasExistentes != null ? citasExistentes : new ArrayList<>();
    }

    public boolean registrarCita(Cita nuevaCita) {
        ValidadorCitas.ResultadoVerificacion resultado = ValidadorCitas.verificarDisponibilidad(
                nuevaCita.getFecha(),
                nuevaCita.getHoraInicio(),
                nuevaCita.getHoraFin(),
                citasExistentes
        );

        if ("ocupado".equalsIgnoreCase(resultado.getEstado())) {
            System.out.println("\n[AVISO - CITA RECHAZADA] " + resultado.getMensaje());
            return false;
        }

        repositorio.guardar(nuevaCita);
        if (!citasExistentes.contains(nuevaCita)) {
            citasExistentes.add(nuevaCita);
        }
        System.out.println("¡Cita registrada con éxito!");
        return true;
    }
}