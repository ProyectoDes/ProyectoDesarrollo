package mx.bambuybrillo.negocio;

import mx.bambuybrillo.persistencia.CitaRepositorio;

public class EliminarCita {

    private final CitaRepositorio repositorio;

    public EliminarCita(CitaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String eliminar(int idCita) {
        if (idCita <= 0) {
            throw new IllegalArgumentException("La cita a eliminar no existe.");
        }
        repositorio.eliminar(idCita);
        return "Cita eliminada correctamente.";
    }
}