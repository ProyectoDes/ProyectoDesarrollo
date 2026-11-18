package mx.bambuybrillo.persistencia;

import java.util.List;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.entidad.CitaConCliente;
import mx.bambuybrillo.entidad.ClienteOpcion;

public interface CitaRepositorio {
    void guardar(Cita cita);

    void actualizar(Cita cita);

    List<CitaConCliente> listarConCliente();

    void eliminar(int idCita);

    List<ClienteOpcion> listarClientes();

    boolean existeCliente(int idCliente);
}
