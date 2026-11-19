package mx.bambuybrillo.persistencia;

import java.util.List;

import mx.bambuybrillo.entidad.Cliente;

public interface ClienteRepositorio {
    void guardar(Cliente cliente);

    void actualizar(Cliente cliente);

    List<Cliente> listar();
}
