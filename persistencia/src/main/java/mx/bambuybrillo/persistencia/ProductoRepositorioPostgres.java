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

    public static final String SQL_LISTAR =
            "SELECT id_producto, nombre, descripcion, cantidad FROM productos ORDER BY nombre";

    public static final String SQL_BUSCAR_POR_ID =
            "SELECT id_producto, nombre, descripcion, cantidad FROM productos WHERE id_producto = ?";

    // fecha_modificacion la actualiza el trigger trg_productos_modificacion
    public static final String SQL_ACTUALIZAR =
            "UPDATE productos SET nombre = ?, descripcion = ?, cantidad = ? WHERE id_producto = ?";

    public static final String SQL_NOMBRE_EN_OTRO =
            "SELECT 1 FROM productos WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_producto <> ?";

    public static final String SQL_ELIMINAR = "DELETE FROM productos WHERE id_producto = ?";

    public static final String SQL_LIGADO_A_CITA =
            "SELECT 1 FROM cita_producto WHERE id_producto = ? LIMIT 1";

    // Código de PostgreSQL cuando un DELETE choca con una llave foránea
    private static final String VIOLACION_LLAVE_FORANEA = "23503";

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

    @Override
    public Optional<Producto> buscarPorId(int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getInt("cantidad")));
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
            ps.setInt(4, producto.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el producto.", e);
        }
    }

    @Override
    public boolean existeNombreEnOtroProducto(String nombre, int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_NOMBRE_EN_OTRO)) {
            ps.setString(1, nombre);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo revisar el nombre del producto.", e);
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // cita_producto tiene ON DELETE NO ACTION: si alguna cita usa el producto,
            // la base rechaza el borrado aunque no se haya revisado antes
            if (VIOLACION_LLAVE_FORANEA.equals(e.getSQLState())) {
                throw new ProductoLigadoException(
                        "El producto está ligado a una cita.", e);
            }
            throw new IllegalStateException("No se pudo eliminar el producto.", e);
        }
    }

    @Override
    public boolean estaLigadoACita(int idProducto) {
        try (Connection conexion = conectar();
             PreparedStatement ps = conexion.prepareStatement(SQL_LIGADO_A_CITA)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo consultar si el producto está ligado a una cita.", e);
        }
    }
}
