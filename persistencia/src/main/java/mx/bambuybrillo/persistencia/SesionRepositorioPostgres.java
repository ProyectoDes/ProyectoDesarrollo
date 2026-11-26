package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class SesionRepositorioPostgres implements SesionRepositorio {

    public static final String SQL_GUARDAR =
            "INSERT INTO sesiones (token_hash, id_usuario, expira) VALUES (?, ?, ?)";

    public static final String SQL_VIGENTE =
            "SELECT 1 FROM sesiones WHERE token_hash = ? AND expira > ?";

    public static final String SQL_ELIMINAR = "DELETE FROM sesiones WHERE token_hash = ?";

    public static final String SQL_ELIMINAR_VENCIDAS = "DELETE FROM sesiones WHERE expira <= ?";

    private final String url;
    private final String usuario;
    private final String clave;

    public SesionRepositorioPostgres(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public void guardar(String tokenHash, int idUsuario, Instant expira) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_GUARDAR)) {
            ps.setString(1, tokenHash);
            ps.setInt(2, idUsuario);
            ps.setTimestamp(3, Timestamp.from(expira));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la sesión.", e);
        }
    }

    @Override
    public boolean estaVigente(String tokenHash, Instant ahora) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_VIGENTE)) {
            ps.setString(1, tokenHash);
            ps.setTimestamp(2, Timestamp.from(ahora));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo revisar la sesión.", e);
        }
    }

    @Override
    public void eliminar(String tokenHash) {
        ejecutar(SQL_ELIMINAR, tokenHash);
    }

    @Override
    public void eliminarVencidas(Instant ahora) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR_VENCIDAS)) {
            ps.setTimestamp(1, Timestamp.from(ahora));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron limpiar las sesiones.", e);
        }
    }

    private void ejecutar(String sql, String tokenHash) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cerrar la sesión.", e);
        }
    }
}
