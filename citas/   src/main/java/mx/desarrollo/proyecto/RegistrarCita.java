package mx.desarrollo.proyecto;

public class RegistrarCita {

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
    }
}