package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Producto;

public class ProductoRepositorioPostgres implements ProductoRepositorio {

    public static final String SQL_LISTAR =
            "SELECT id_producto, nombre, descripcion, cantidad FROM productos ORDER BY nombre";

    private final String url;
    private final String usuario;
    private final String clave;

    public ProductoRepositorioPostgres(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>();
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("cantidad")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el inventario.", e);
        }
        return productos;
    }
}
