package mx.desarrollo.proyecto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConsultarCitas {

    public static final String SQL_LISTAR_CON_CLIENTE =
            "SELECT c.id_cita, c.id_cliente, cl.nombre, c.fecha, "
                    + "c.hora_inicio, c.hora_fin, c.servicio "
                    + "FROM citas c "
                    + "INNER JOIN clientes cl ON cl.id_cliente = c.id_cliente "
                    + "ORDER BY c.fecha, c.hora_inicio";

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
}