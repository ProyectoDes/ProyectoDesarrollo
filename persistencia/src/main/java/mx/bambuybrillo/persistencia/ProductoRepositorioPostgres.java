package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mx.bambuybrillo.entidad.Producto;

public class ProductoRepositorioPostgres implements ProductoRepositorio {

    public static final String SQL_GUARDAR =
            "INSERT INTO productos (nombre, descripcion, cantidad, precio) VALUES (?, ?, ?, ?)";

    public static final String SQL_LISTAR =
            "SELECT id_producto, nombre, descripcion, cantidad, precio "
                    + "FROM productos ORDER BY nombre";

    public static final String SQL_BUSCAR_POR_ID =
            "SELECT id_producto, nombre, descripcion, cantidad, precio "
                    + "FROM productos WHERE id_producto = ?";

    public static final String SQL_ACTUALIZAR =
            "UPDATE productos SET nombre = ?, descripcion = ?, cantidad = ?, precio = ? "
                    + "WHERE id_producto = ?";

    public static final String SQL_ELIMINAR = "DELETE FROM productos WHERE id_producto = ?";

    public static final String SQL_LIGADO_A_CITA =
            "SELECT COUNT(*) FROM cita_producto WHERE id_producto = ?";

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

    private static Producto leerProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("precio"));
    }

    @Override
    public void guardar(Producto producto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_GUARDAR)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setInt(3, producto.getCantidad());
            ps.setBigDecimal(4, producto.getPrecio());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el producto.", e);
        }
    }

    @Override
    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>();
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(leerProducto(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los productos.", e);
        }
        return productos;
    }

    @Override
    public Optional<Producto> buscarPorId(int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(leerProducto(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el producto.", e);
        }
    }

    @Override
    public boolean actualizar(Producto producto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setInt(3, producto.getCantidad());
            ps.setBigDecimal(4, producto.getPrecio());
            ps.setInt(5, producto.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el producto.", e);
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el producto.", e);
        }
    }

    @Override
    public boolean estaLigadoACita(int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LIGADO_A_CITA)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo consultar si el producto está ligado a una cita.", e);
        }
    }
}
