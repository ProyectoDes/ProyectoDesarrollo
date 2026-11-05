package mx.desarrollo.proyecto;

public class EliminarCita {

    public static final String SQL_ELIMINAR = "DELETE FROM citas WHERE id_cita = ?";

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