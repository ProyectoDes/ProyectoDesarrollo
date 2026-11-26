package mx.bambuybrillo.negocio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BajaProductoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Tijuana");
    // "Ahora" fijo para las pruebas: 25 de noviembre de 2026 a las 15:00
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 25, 15, 0);

    // Cita en la que se usó un producto
    record CitaUsada(int idProducto, LocalDate fecha, LocalTime horaFin) { }

    // Usa el mismo repositorio falso de ModificarProductoTest y agrega las citas
    static class RepositorioFalso extends ModificarProductoTest.RepositorioFalso {
        final List<CitaUsada> citas = new ArrayList<>();
        final List<Integer> dadosDeBaja = new ArrayList<>();

        @Override
        public boolean eliminar(int idProducto) {
            boolean quitado = productos.removeIf(p -> p.getIdProducto() == idProducto);
            if (quitado) {
                dadosDeBaja.add(idProducto);
            }
            return quitado;
        }

        @Override
        public boolean estaEnCitaPendiente(int idProducto, LocalDate hoy, LocalTime ahora) {
            return citas.stream().anyMatch(c -> c.idProducto() == idProducto
                    && (c.fecha().isAfter(hoy) || (c.fecha().equals(hoy) && c.horaFin().isAfter(ahora))));
        }
    }

    private RepositorioFalso repositorio;
    private BajaProducto baja;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        repositorio.productos.add(new Producto(1, "Toallas", null, 10));
        repositorio.productos.add(new Producto(2, "Sérum hialurónico", null, 10));
        baja = new BajaProducto(repositorio, Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA));
    }

    private void verificarRechazo(int id, String mensaje) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> baja.eliminar(id));
        assertEquals(mensaje, e.getMessage());
        assertEquals(2, repositorio.listar().size());
    }

    @Test
    void productoSinCitasSeDaDeBaja() {
        assertEquals("Producto eliminado correctamente.", baja.eliminar(1));
        assertEquals(1, repositorio.listar().size());
        assertEquals(List.of(1), repositorio.dadosDeBaja);
    }

    @Test
    void productoUsadoEnCitaDeHaceTresDiasSeDaDeBaja() {
        repositorio.citas.add(new CitaUsada(1, AHORA.toLocalDate().minusDays(3), LocalTime.of(11, 0)));
        assertEquals("Producto eliminado correctamente.", baja.eliminar(1));
    }

    @Test
    void productoUsadoEnCitaDeHoyQueYaTerminoSeDaDeBaja() {
        repositorio.citas.add(new CitaUsada(1, AHORA.toLocalDate(), LocalTime.of(14, 30)));
        assertEquals("Producto eliminado correctamente.", baja.eliminar(1));
    }

    @Test
    void productoEnCitaDeHoyQueNoHaTerminadoNoSeDaDeBaja() {
        repositorio.citas.add(new CitaUsada(1, AHORA.toLocalDate(), LocalTime.of(16, 0)));
        verificarRechazo(1, BajaProducto.MENSAJE_CITA_PENDIENTE);
    }

    @Test
    void productoEnCitaFuturaNoSeDaDeBaja() {
        repositorio.citas.add(new CitaUsada(1, AHORA.toLocalDate().plusDays(2), LocalTime.of(10, 0)));
        verificarRechazo(1, BajaProducto.MENSAJE_CITA_PENDIENTE);
    }

    @Test
    void idCeroSeRechaza() {
        verificarRechazo(0, "El producto a eliminar no existe.");
    }

    @Test
    void productoInexistenteSeRechaza() {
        verificarRechazo(99, "El producto a eliminar no existe.");
    }

    @Test
    void idNegativoSeRechaza() {
        verificarRechazo(-5, "El producto a eliminar no existe.");
    }

    @Test
    void productoDadoDeBajaYaNoSeEncuentra() {
        baja.eliminar(1);
        assertTrue(repositorio.buscarPorId(1).isEmpty());
        assertTrue(repositorio.buscarPorId(2).isPresent());
    }
}
