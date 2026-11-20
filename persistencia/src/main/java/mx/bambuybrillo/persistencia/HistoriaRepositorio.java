package mx.bambuybrillo.persistencia;

import java.util.List;

import mx.bambuybrillo.entidad.RegistroHistoria;

public interface HistoriaRepositorio {
    // Todas las citas del cliente con sus observaciones y productos, de la más reciente a la más antigua
    List<RegistroHistoria> listarPorCliente(int idCliente);

    // Un solo registro (la cita indicada); null si la cita no existe
    RegistroHistoria buscarPorCita(int idCita);

    // Agrega observaciones y productos a lo que ya tenía la cita
    void agregar(int idCita, String observaciones, List<Integer> idsProductos);
}
