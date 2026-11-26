package mx.bambuybrillo.persistencia;

import java.time.Instant;

public interface SesionRepositorio {
    // Guarda una sesión nueva (solo el hash del token)
    void guardar(String tokenHash, int idUsuario, Instant expira);

    // true si la sesión existe y todavía no expira
    boolean estaVigente(String tokenHash, Instant ahora);

    // Cierra la sesión
    void eliminar(String tokenHash);

    // Limpia las sesiones que ya expiraron
    void eliminarVencidas(Instant ahora);
}
