package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.RegistroHistoria;
import mx.bambuybrillo.persistencia.HistoriaRepositorio;

public class ConsultarHistoria {

    private final HistoriaRepositorio repositorio;

    public ConsultarHistoria(HistoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<RegistroHistoria> listarPorCliente(int idCliente) {
        if (idCliente <= 0) {
            throw new IllegalArgumentException("No se indicó el cliente.");
        }
        List<RegistroHistoria> registros = repositorio.listarPorCliente(idCliente);
        return registros == null ? new ArrayList<>() : registros;
    }

    public RegistroHistoria buscarPorCita(int idCita) {
        RegistroHistoria registro = idCita > 0 ? repositorio.buscarPorCita(idCita) : null;
        if (registro == null) {
            throw new IllegalArgumentException("La cita ya no existe.");
        }
        return registro;
    }
}
