package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.entidad.CitaConCliente;
import mx.bambuybrillo.entidad.ClienteOpcion;

public class CitaRepositorioPostgres implements CitaRepositorio {

    public static final String SQL_GUARDAR =
            "INSERT INTO citas (id_cliente, fecha, hora_inicio, hora_fin, servicio) "
                    + "VALUES (?, ?::date, ?::time, ?::time, ?)";

    public static final String SQL_LISTAR_CON_CLIENTE =
            "SELECT c.id_cita, c.id_cliente, cl.nombre, c.fecha::text AS fecha, "
                    + "c.hora_inicio::text AS hora_inicio, c.hora_fin::text AS hora_fin, c.servicio "
                    + "FROM citas c "
                    + "INNER JOIN clientes cl ON cl.id_cliente = c.id_cliente "
                    + "ORDER BY c.fecha, c.hora_inicio";

    public static final String SQL_ELIMINAR = "DELETE FROM citas WHERE id_cita = ?";

    public static final String SQL_ACTUALIZAR =
            "UPDATE citas SET id_cliente = ?, fecha = ?::date, hora_inicio = ?::time, "
                    + "hora_fin = ?::time, servicio = ? WHERE id_cita = ?";

    public static final String SQL_LISTAR_CLIENTES =
            "SELECT id_cliente, nombre FROM clientes ORDER BY nombre";

    public static final String SQL_EXISTE_CLIENTE =
            "SELECT 1 FROM clientes WHERE id_cliente = ?";

    // Dos citas se enciman si una empieza antes de que termine la otra
    public static final String SQL_HAY_TRASLAPE =
            "SELECT 1 FROM citas WHERE fecha = ?::date AND id_cita <> ? "
                    + "AND hora_inicio < ?::time AND hora_fin > ?::time LIMIT 1";

    private final String url;
    private final String usuario;
    private final String clave;

    public CitaRepositorioPostgres(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String recortarHora(String hora) {
        if (hora != null && hora.length() > 5) {
            return hora.substring(0, 5);
        }
        return hora;
    }

    @Override
    public void guardar(Cita cita) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_GUARDAR)) {
            ps.setInt(1, cita.getIdCliente());
            ps.setString(2, cita.getFecha());
            ps.setString(3, cita.getHoraInicio());
            ps.setString(4, cita.getHoraFin());
            ps.setString(5, cita.getServicio());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la cita.", e);
        }
    }

    @Override
    public void actualizar(Cita cita) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setInt(1, cita.getIdCliente());
            ps.setString(2, cita.getFecha());
            ps.setString(3, cita.getHoraInicio());
            ps.setString(4, cita.getHoraFin());
            ps.setString(5, cita.getServicio());
            ps.setInt(6, cita.getIdCita());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la cita.", e);
        }
    }

    @Override
    public List<CitaConCliente> listarConCliente() {
        List<CitaConCliente> citas = new ArrayList<>();
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_CON_CLIENTE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                citas.add(new CitaConCliente(
                        rs.getInt("id_cita"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("fecha"),
                        recortarHora(rs.getString("hora_inicio")),
                        recortarHora(rs.getString("hora_fin")),
                        rs.getString("servicio")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar las citas.", e);
        }
        return citas;
    }

    @Override
    public void eliminar(int idCita) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idCita);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la cita.", e);
        }
    }

    @Override
    public List<ClienteOpcion> listarClientes() {
        List<ClienteOpcion> clientes = new ArrayList<>();
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_CLIENTES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(new ClienteOpcion(rs.getInt("id_cliente"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los clientes.", e);
        }
        return clientes;
    }

    @Override
    public boolean existeCliente(int idCliente) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_EXISTE_CLIENTE)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar el cliente.", e);
        }
    }

    @Override
    public boolean hayTraslape(String fecha, String horaInicio, String horaFin, int idCitaExcluir) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_HAY_TRASLAPE)) {
            ps.setString(1, fecha);
            ps.setInt(2, idCitaExcluir);
            ps.setString(3, horaFin);
            ps.setString(4, horaInicio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar la disponibilidad del horario.", e);
        }
    }
}
