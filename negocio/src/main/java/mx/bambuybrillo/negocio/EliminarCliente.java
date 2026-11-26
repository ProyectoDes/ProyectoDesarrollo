package mx.bambuybrillo.negocio;

import mx.bambuybrillo.persistencia.ClienteRepositorio;

public class EliminarCliente {

    private final ClienteRepositorio repositorio;

    public EliminarCliente(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Da de baja al cliente; también se borran sus citas y su historia clínica
    public String eliminar(int idCliente) {
        if (idCliente <= 0) {
            throw new IllegalArgumentException("El cliente a dar de baja no existe.");
        }
        repositorio.eliminar(idCliente);
        return "Cliente dado de baja correctamente.";
    }
}
