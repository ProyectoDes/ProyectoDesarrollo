package mx.bambuybrillo.negocio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import mx.bambuybrillo.persistencia.ProductoRepositorio;

// La baja es lógica: el producto deja de salir en el inventario,
// pero la historia clínica de las citas que ya pasaron lo sigue mostrando
public class BajaProducto {

    public static final String MENSAJE_CITA_PENDIENTE =
            "No se puede dar de baja: el producto está en una cita que todavía no pasa.";

    private static final ZoneId ZONA = ZoneId.of("America/Tijuana");

    private final ProductoRepositorio repositorio;
    private final Clock reloj;

    public BajaProducto(ProductoRepositorio repositorio) {
        this(repositorio, Clock.system(ZONA));
    }

    // Permite fijar la hora en las pruebas
    public BajaProducto(ProductoRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public String eliminar(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("El producto a eliminar no existe.");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (repositorio.estaEnCitaPendiente(idProducto, ahora.toLocalDate(), ahora.toLocalTime())) {
            throw new IllegalArgumentException(MENSAJE_CITA_PENDIENTE);
        }
        if (!repositorio.eliminar(idProducto)) {
            throw new IllegalArgumentException("El producto a eliminar no existe.");
        }
        return "Producto eliminado correctamente.";
    }
}
