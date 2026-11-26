package mx.bambuybrillo.negocio;

import java.time.Clock;

import mx.bambuybrillo.persistencia.SesionRepositorio;

// Revisa si la cookie de sesión sigue siendo válida y permite cerrar sesión
public class ValidarSesion {

    private final SesionRepositorio sesiones;
    private final Clock reloj;

    public ValidarSesion(SesionRepositorio sesiones) {
        this(sesiones, Clock.systemUTC());
    }

    public ValidarSesion(SesionRepositorio sesiones, Clock reloj) {
        this.sesiones = sesiones;
        this.reloj = reloj;
    }

    public boolean esValida(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return sesiones.estaVigente(Contrasenas.sha256(token), reloj.instant());
    }

    public void cerrar(String token) {
        if (token != null && !token.isBlank()) {
            sesiones.eliminar(Contrasenas.sha256(token));
        }
    }
}
