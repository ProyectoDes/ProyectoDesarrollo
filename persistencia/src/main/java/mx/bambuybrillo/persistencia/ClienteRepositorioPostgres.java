package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mx.bambuybrillo.entidad.Cliente;

public class ClienteRepositorioPostgres implements ClienteRepositorio {

    public static final String SQL_GUARDAR =
            "INSERT INTO clientes (nombre, edad, telefono) VALUES (?, ?, ?)";

    public static final String SQL_ACTUALIZAR =
            "UPDATE clientes SET nombre = ?, edad = ?, telefono = ? WHERE id_cliente = ?";

    private final String url;
    private final String usuario;
    private final String clave;

    public ClienteRepositorioPostgres(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public void guardar(Cliente cliente) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_GUARDAR)) {
            ps.setString(1, cliente.getNombreCliente().trim());
            ps.setInt(2, cliente.getEdadCliente());
            ps.setString(3, cliente.getTelefonoCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el cliente.", e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, cliente.getNombreCliente().trim());
            ps.setInt(2, cliente.getEdadCliente());
            ps.setString(3, cliente.getTelefonoCliente());
            ps.setInt(4, cliente.getIdCliente());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("El cliente a modificar no existe.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el cliente.", e);
        }
    }
}