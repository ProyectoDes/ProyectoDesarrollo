package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.persistencia.CitaRepositorio;

public class ModificarCita {

    private final CitaRepositorio repositorio;
    private final RegistrarCita reglas;

    public ModificarCita(CitaRepositorio repositorio) {
        this.repositorio = repositorio;
        this.reglas = new RegistrarCita(repositorio);
    }

    public String modificar(Cita cita) {
        if (cita != null && cita.getIdCita() <= 0) {
            throw new IllegalArgumentException("La cita a modificar no existe.");
        }
        validar(cita);
        repositorio.actualizar(cita);
        return "Cita actualizada correctamente.";
    }

    // Una cita editada debe cumplir las mismas reglas que una cita nueva
    public void validar(Cita cita) {
        reglas.validar(cita);
    }
}
