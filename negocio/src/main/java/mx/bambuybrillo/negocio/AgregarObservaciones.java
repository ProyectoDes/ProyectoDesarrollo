package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import mx.bambuybrillo.persistencia.HistoriaRepositorio;

public class AgregarObservaciones {

    private static final int MAX_OBSERVACIONES = 1000;

    private final HistoriaRepositorio repositorio;

    public AgregarObservaciones(HistoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Solo agrega: lo que ya estaba registrado en la cita no se borra ni se cambia
    public String agregar(int idCita, String observaciones, List<Integer> idsProductos) {
        if (idCita <= 0) {
            throw new ValidacionException("cita", "No se indicó la cita.");
        }
        String texto = observaciones == null ? "" : observaciones.trim();
        List<Integer> productos = new ArrayList<>(new LinkedHashSet<>(
                idsProductos == null ? new ArrayList<>() : idsProductos));
        productos.removeIf(id -> id == null || id <= 0);

        if (texto.isEmpty() && productos.isEmpty()) {
            throw new ValidacionException("observaciones",
                    "Escribe una observación o elige al menos un producto.");
        }
        if (texto.length() > MAX_OBSERVACIONES) {
            throw new ValidacionException("observaciones",
                    "Las observaciones no pueden tener más de 1000 caracteres.");
        }
        repositorio.agregar(idCita, texto, productos);
        return "Cambios guardados en la historia clínica.";
    }
}
