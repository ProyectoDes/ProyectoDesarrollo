package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.persistencia.CitaRepositorio;

public class ModificarCita {

    private final CitaRepositorio repositorio;

    public ModificarCita(CitaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String modificar(Cita cita) {
        if (cita != null && cita.getIdCita() <= 0) {
            throw new IllegalArgumentException("La cita a modificar no existe.");
        }
        validar(cita);
        repositorio.actualizar(cita);
        return "Cita actualizada correctamente.";
    }

    public void validar(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException("Los datos de la cita son obligatorios.");
        }
        if (cita.getIdCliente() <= 0) {
            throw new IllegalArgumentException("Selecciona un cliente.");
        }
    }
}