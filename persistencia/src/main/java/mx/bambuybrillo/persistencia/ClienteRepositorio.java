package mx.bambuybrillo.persistencia;

import java.util.List;

import mx.bambuybrillo.entidad.Cliente;

public interface ClienteRepositorio {
    void guardar(Cliente cliente);

    void actualizar(Cliente cliente);

    List<Cliente> listar();

    // Borra al cliente junto con sus citas y su historia clínica
    void eliminar(int idCliente);
}
