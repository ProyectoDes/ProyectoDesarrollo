package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.entidad.RegistroHistoria;

public class HistoriaRepositorioPostgres implements HistoriaRepositorio {

    private static final String SELECT_REGISTRO =
            "SELECT c.id_cita, c.id_cliente, cl.nombre, c.fecha, "
          + "to_char(c.hora_inicio, 'HH24:MI') AS hora_inicio, "
          + "to_char(c.hora_fin, 'HH24:MI') AS hora_fin, c.servicio, h.observaciones "
          + "FROM citas c JOIN clientes cl ON cl.id_cliente = c.id_cliente "
          + "LEFT JOIN historia_clinica h ON h.id_cita = c.id_cita ";

    public static final String SQL_LISTAR_POR_CLIENTE = SELECT_REGISTRO
          + "WHERE c.id_cliente = ? ORDER BY c.fecha DESC, c.hora_inicio DESC";

    public static final String SQL_BUSCAR_POR_CITA = SELECT_REGISTRO + "WHERE c.id_cita = ?";

    public static final String SQL_PRODUCTOS_POR_CLIENTE =
            "SELECT cp.id_cita, p.id_producto, p.nombre, p.descripcion, p.cantidad "
          + "FROM cita_producto cp JOIN productos p ON p.id_producto = cp.id_producto "
          + "JOIN citas c ON c.id_cita = cp.id_cita WHERE c.id_cliente = ? ORDER BY p.nombre";

    public static final String SQL_PRODUCTOS_POR_CITA =
            "SELECT cp.id_cita, p.id_producto, p.nombre, p.descripcion, p.cantidad "
          + "FROM cita_producto cp JOIN productos p ON p.id_producto = cp.id_producto "
          + "WHERE cp.id_cita = ? ORDER BY p.nombre";

    // Si la cita ya tiene registro, la nueva observación se agrega en otra línea
    public static final String SQL_AGREGAR_OBSERVACIONES =
            "INSERT INTO historia_clinica (id_cliente, fecha, observaciones, id_cita) "
          + "SELECT id_cliente, fecha, ?, id_cita FROM citas WHERE id_cita = ? "
          + "ON CONFLICT (id_cita) DO UPDATE SET observaciones = "
          + "CASE WHEN historia_clinica.observaciones IS NULL OR historia_clinica.observaciones = '' "
          + "THEN EXCLUDED.observaciones "
          + "ELSE historia_clinica.observaciones || E'\\n' || EXCLUDED.observaciones END";

    public static final String SQL_AGREGAR_PRODUCTO =
            "INSERT INTO cita_producto (id_cita, id_producto, cantidad_usada) "
                    + "SELECT ?, id_producto, 1 FROM productos WHERE id_producto = ? AND activo "
                    + "ON CONFLICT (id_cita, id_producto) DO NOTHING";

    public static final String SQL_EXISTE_CITA = "SELECT 1 FROM citas WHERE id_cita = ?";
    public static final String SQL_CREAR_REGISTROS_PASADOS =
            "INSERT INTO historia_clinica (id_cliente, fecha, id_cita) "
                    + "SELECT id_cliente, fecha, id_cita FROM citas "
                    + "WHERE id_cliente = ? "
                    + "AND (fecha + hora_fin) <= (now() AT TIME ZONE 'America/Tijuana') "
                    + "ON CONFLICT (id_cita) DO NOTHING";

    private final String url;
    private final String usuario;
    private final String clave;

    public HistoriaRepositorioPostgres(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    private void crearRegistrosDeCitasPasadas(Connection conexion, int idCliente) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_CREAR_REGISTROS_PASADOS)) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    private RegistroHistoria leerRegistro(ResultSet rs) throws SQLException {
        RegistroHistoria r = new RegistroHistoria();
        r.setIdCita(rs.getInt("id_cita"));
        r.setIdCliente(rs.getInt("id_cliente"));
        r.setNombreCliente(rs.getString("nombre"));
        r.setFecha(rs.getString("fecha"));
        r.setHoraInicio(rs.getString("hora_inicio"));
        r.setHoraFin(rs.getString("hora_fin"));
        r.setServicio(rs.getString("servicio"));
        r.setObservaciones(rs.getString("observaciones"));
        return r;
    }

    private void leerProductos(PreparedStatement ps, Map<Integer, RegistroHistoria> porCita)
            throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RegistroHistoria r = porCita.get(rs.getInt("id_cita"));
                if (r != null) {
                    r.getProductos().add(new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getInt("cantidad")));
                }
            }
        }
    }

    @Override
    public List<RegistroHistoria> listarPorCliente(int idCliente) {
        Map<Integer, RegistroHistoria> porCita = new LinkedHashMap<>();
        try (Connection conexion = conectar()) {
            crearRegistrosDeCitasPasadas(conexion, idCliente);
            try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_POR_CLIENTE)) {
                ps.setInt(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        RegistroHistoria r = leerRegistro(rs);
                        porCita.put(r.getIdCita(), r);
                    }
                }
            }
            try (PreparedStatement ps = conexion.prepareStatement(SQL_PRODUCTOS_POR_CLIENTE)) {
                ps.setInt(1, idCliente);
                leerProductos(ps, porCita);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la historia clínica.", e);
        }
        return new ArrayList<>(porCita.values());
    }

    @Override
    public RegistroHistoria buscarPorCita(int idCita) {
        Map<Integer, RegistroHistoria> porCita = new LinkedHashMap<>();
        try (Connection conexion = conectar()) {
            try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_CITA)) {
                ps.setInt(1, idCita);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    RegistroHistoria r = leerRegistro(rs);
                    porCita.put(r.getIdCita(), r);
                }
            }
            try (PreparedStatement ps = conexion.prepareStatement(SQL_PRODUCTOS_POR_CITA)) {
                ps.setInt(1, idCita);
                leerProductos(ps, porCita);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el registro.", e);
        }
        return porCita.get(idCita);
    }

    @Override
    public void agregar(int idCita, String observaciones, List<Integer> idsProductos) {
        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(SQL_EXISTE_CITA)) {
                    ps.setInt(1, idCita);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("La cita ya no existe.");
                        }
                    }
                }
                if (observaciones != null && !observaciones.isEmpty()) {
                    try (PreparedStatement ps = conexion.prepareStatement(SQL_AGREGAR_OBSERVACIONES)) {
                        ps.setString(1, observaciones);
                        ps.setInt(2, idCita);
                        ps.executeUpdate();
                    }
                }
                if (idsProductos != null && !idsProductos.isEmpty()) {
                    try (PreparedStatement ps = conexion.prepareStatement(SQL_AGREGAR_PRODUCTO)) {
                        for (Integer idProducto : idsProductos) {
                            ps.setInt(1, idCita);
                            ps.setInt(2, idProducto);
                            if (ps.executeUpdate() > 0) {
                                StockProductos.descontarUnidad(conexion, idProducto);
                            }
                        }
                    }
                }
                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron guardar las observaciones.", e);
        }
    }
}
