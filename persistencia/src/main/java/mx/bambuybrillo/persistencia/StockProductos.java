package mx.bambuybrillo.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Descuenta del inventario los productos que se usan en una cita
final class StockProductos {

    // La condición cantidad > 0 evita que dos citas a la vez se lleven la última unidad
    static final String SQL_DESCONTAR =
            "UPDATE productos SET cantidad = cantidad - 1 "
                    + "WHERE id_producto = ? AND activo AND cantidad > 0";

    static final String SQL_NOMBRE = "SELECT nombre FROM productos WHERE id_producto = ?";

    private StockProductos() {
    }

    // Resta una unidad dentro de la transacción de la conexión; si no hay existencias, lanza el aviso
    static void descontarUnidad(Connection conexion, int idProducto) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_DESCONTAR)) {
            ps.setInt(1, idProducto);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        String mensaje = "Uno de los productos elegidos no tiene existencias.";
        try (PreparedStatement ps = conexion.prepareStatement(SQL_NOMBRE)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mensaje = "El producto \"" + rs.getString(1) + "\" no tiene existencias.";
                }
            }
        }
        throw new IllegalArgumentException(mensaje);
    }
}