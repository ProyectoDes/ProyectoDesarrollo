package mx.desarrollo.proyecto;

import java.util.List;

public interface CitaRepositorio {
    void guardar(Cita cita);

    List<CitaConCliente> listarConCliente();
}