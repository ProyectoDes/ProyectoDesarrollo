package mx.bambuybrillo.negocio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import mx.bambuybrillo.entidad.Usuario;
import mx.bambuybrillo.persistencia.SesionRepositorio;
import mx.bambuybrillo.persistencia.UsuarioRepositorio;

public class IniciarSesion {

    public static final Duration DURACION = Duration.ofDays(30);
    // Mismo mensaje si falla el usuario o la contraseña, para no revelar cuál fue
    public static final String MENSAJE_INCORRECTO = "Usuario o contraseña incorrectos.";

    // Hash de relleno para tardar lo mismo aunque el usuario no exista
    private static final String HASH_DE_RELLENO = Contrasenas.cifrar("relleno-sin-uso");

    private final UsuarioRepositorio usuarios;
    private final SesionRepositorio sesiones;
    private final Clock reloj;

    public IniciarSesion(UsuarioRepositorio usuarios, SesionRepositorio sesiones) {
        this(usuarios, sesiones, Clock.systemUTC());
    }

    public IniciarSesion(UsuarioRepositorio usuarios, SesionRepositorio sesiones, Clock reloj) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.reloj = reloj;
    }

    // Regresa el token que va en la cookie si las credenciales son correctas
    public String iniciar(String nombreUsuario, String contrasena) {
        String nombre = nombreUsuario == null ? "" : nombreUsuario.trim();
        if (nombre.isEmpty()) {
            throw new ValidacionException("usuario", "Escribe tu usuario.");
        }
        if (contrasena == null || contrasena.isEmpty()) {
            throw new ValidacionException("contrasena", "Escribe tu contraseña.");
        }
        Optional<Usuario> usuario = usuarios.buscarPorNombre(nombre);
        String guardado = usuario.map(Usuario::getContrasenaHash).orElse(HASH_DE_RELLENO);
        boolean correcta = Contrasenas.coincide(contrasena, guardado);
        if (usuario.isEmpty() || !correcta) {
            throw new CredencialesIncorrectasException(MENSAJE_INCORRECTO);
        }

        Instant ahora = reloj.instant();
        sesiones.eliminarVencidas(ahora);
        String token = Contrasenas.nuevoToken();
        sesiones.guardar(Contrasenas.sha256(token), usuario.get().getIdUsuario(), ahora.plus(DURACION));
        return token;
    }
}
