package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import mx.bambuybrillo.entidad.Usuario;

public class UsuarioRepositorioPostgres implements UsuarioRepositorio {

    public static final String SQL_BUSCAR_POR_NOMBRE =
            "SELECT id_usuario, nombre_usuario, contrasena_hash FROM usuarios "
          + "WHERE LOWER(nombre_usuario) = LOWER(TRIM(?))";

    private final String url;
    private final String usuario;
    private final String clave;

    public UsuarioRepositorioPostgres(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public Optional<Usuario> buscarPorNombre(String nombreUsuario) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("contrasena_hash")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el usuario.", e);
        }
    }
}
