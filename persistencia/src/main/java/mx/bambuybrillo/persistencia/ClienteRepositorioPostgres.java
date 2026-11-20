package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Cliente;

public class ClienteRepositorioPostgres implements ClienteRepositorio {

    public static final String SQL_GUARDAR =
            "INSERT INTO clientes (nombre, edad, telefono) VALUES (?, ?, ?)";

    public static final String SQL_ACTUALIZAR =
            "UPDATE clientes SET nombre = ?, edad = ?, telefono = ? WHERE id_cliente = ?";

    public static final String SQL_LISTAR =
            "SELECT id_cliente, nombre, edad, telefono FROM clientes ORDER BY nombre";

    // Primero se borra lo que depende del cliente: su historia y sus citas
    // (al borrar una cita se borran solos sus productos usados)
    public static final String SQL_ELIMINAR_HISTORIA = "DELETE FROM historia_clinica WHERE id_cliente = ?";
    public static final String SQL_ELIMINAR_CITAS = "DELETE FROM citas WHERE id_cliente = ?";
    public static final String SQL_ELIMINAR = "DELETE FROM clientes WHERE id_cliente = ?";

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

    @Override
    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getInt("edad"),
                        rs.getString("telefono")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar los clientes.", e);
        }
        return clientes;
    }

    @Override
    public void eliminar(int idCliente) {
        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try {
                for (String sql : new String[]{SQL_ELIMINAR_HISTORIA, SQL_ELIMINAR_CITAS}) {
                    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setInt(1, idCliente);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
                    ps.setInt(1, idCliente);
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException("El cliente ya no existe.");
                    }
                }
                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo dar de baja al cliente.", e);
        }
    }
}
