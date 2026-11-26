package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mx.bambuybrillo.entidad.CitaConCliente;
import mx.bambuybrillo.entidad.ClienteOpcion;
import mx.bambuybrillo.persistencia.CitaRepositorio;

public class ConsultarCitas {

    private final CitaRepositorio repositorio;

    public ConsultarCitas(CitaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<CitaConCliente> listar() {
        List<CitaConCliente> citas = repositorio.listarConCliente();
        if (citas == null) {
            return new ArrayList<>();
        }
        List<CitaConCliente> ordenadas = new ArrayList<>(citas);
        ordenadas.sort(Comparator
                .comparing(CitaConCliente::getFecha)
                .thenComparing(CitaConCliente::getHoraInicio));
        return ordenadas;
    }

    public List<ClienteOpcion> listarClientes() {
        List<ClienteOpcion> clientes = repositorio.listarClientes();
        if (clientes == null) {
            return new ArrayList<>();
        }
        return clientes;
    }
}