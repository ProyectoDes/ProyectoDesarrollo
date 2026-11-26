package mx.bambuybrillo.negocio;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import mx.bambuybrillo.entidad.Usuario;
import mx.bambuybrillo.persistencia.SesionRepositorio;
import mx.bambuybrillo.persistencia.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IniciarSesionTest {

    static final Instant AHORA = Instant.parse("2026-11-25T20:00:00Z");

    static class UsuariosFalsos implements UsuarioRepositorio {
        final Map<String, Usuario> usuarios = new HashMap<>();

        @Override
        public Optional<Usuario> buscarPorNombre(String nombreUsuario) {
            return Optional.ofNullable(usuarios.get(nombreUsuario.trim().toLowerCase()));
        }
    }

    // Guarda en memoria el hash del token y cuándo expira
    static class SesionesFalsas implements SesionRepositorio {
        final Map<String, Instant> sesiones = new HashMap<>();

        @Override
        public void guardar(String tokenHash, int idUsuario, Instant expira) {
            sesiones.put(tokenHash, expira);
        }

        @Override
        public boolean estaVigente(String tokenHash, Instant ahora) {
            Instant expira = sesiones.get(tokenHash);
            return expira != null && expira.isAfter(ahora);
        }

        @Override
        public void eliminar(String tokenHash) {
            sesiones.remove(tokenHash);
        }

        @Override
        public void eliminarVencidas(Instant ahora) {
            sesiones.values().removeIf(expira -> !expira.isAfter(ahora));
        }
    }

    private SesionesFalsas sesiones;
    private IniciarSesion iniciar;

    @BeforeEach
    void preparar() {
        UsuariosFalsos usuarios = new UsuariosFalsos();
        usuarios.usuarios.put("celia", new Usuario(1, "celia", Contrasenas.cifrar("Bambu2026!")));
        sesiones = new SesionesFalsas();
        iniciar = new IniciarSesion(usuarios, sesiones, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void credencialesCorrectasAbrenUnaSesionDe30Dias() {
        String token = iniciar.iniciar("celia", "Bambu2026!");
        assertNotNull(token);
        String hash = Contrasenas.sha256(token);
        assertEquals(AHORA.plus(IniciarSesion.DURACION), sesiones.sesiones.get(hash));
        // Se guarda el hash, no el token
        assertFalse(sesiones.sesiones.containsKey(token));
    }

    @Test
    void elUsuarioNoDistingueMayusculasNiEspacios() {
        assertNotNull(iniciar.iniciar("  CELIA ", "Bambu2026!"));
    }

    @Test
    void contrasenaIncorrectaSeRechaza() {
        CredencialesIncorrectasException e = assertThrows(CredencialesIncorrectasException.class,
                () -> iniciar.iniciar("celia", "otra"));
        assertEquals(IniciarSesion.MENSAJE_INCORRECTO, e.getMessage());
        assertTrue(sesiones.sesiones.isEmpty());
    }

    @Test
    void usuarioInexistenteDaElMismoMensaje() {
        CredencialesIncorrectasException e = assertThrows(CredencialesIncorrectasException.class,
                () -> iniciar.iniciar("alguien", "Bambu2026!"));
        assertEquals(IniciarSesion.MENSAJE_INCORRECTO, e.getMessage());
    }

    @Test
    void usuarioVacioSeRechaza() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> iniciar.iniciar("  ", "x"));
        assertEquals("usuario", e.getCampo());
    }

    @Test
    void contrasenaVaciaSeRechaza() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> iniciar.iniciar("celia", ""));
        assertEquals("contrasena", e.getCampo());
    }

    @Test
    void alIniciarSeLimpianLasSesionesVencidas() {
        sesiones.sesiones.put("vieja", AHORA.minusSeconds(1));
        iniciar.iniciar("celia", "Bambu2026!");
        assertFalse(sesiones.sesiones.containsKey("vieja"));
    }
}
