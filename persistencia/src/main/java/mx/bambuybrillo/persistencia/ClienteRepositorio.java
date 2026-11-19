package mx.bambuybrillo.persistencia;

import mx.bambuybrillo.entidad.Cliente;

public interface ClienteRepositorio {
    void guardar(Cliente cliente);

    void actualizar(Cliente cliente);
}