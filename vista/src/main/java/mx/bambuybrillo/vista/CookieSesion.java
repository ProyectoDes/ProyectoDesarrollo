package mx.bambuybrillo.vista;

import com.sun.net.httpserver.HttpExchange;

import mx.bambuybrillo.negocio.IniciarSesion;

// Lee y escribe la cookie donde viaja el token de sesión
final class CookieSesion {

    static final String NOMBRE = "byb_sesion";

    private CookieSesion() {
    }

    static String leer(HttpExchange ex) {
        for (String encabezado : ex.getRequestHeaders().getOrDefault("Cookie", java.util.List.of())) {
            for (String parte : encabezado.split(";")) {
                String[] par = parte.trim().split("=", 2);
                if (par.length == 2 && NOMBRE.equals(par[0])) {
                    return par[1];
                }
            }
        }
        return null;
    }

    // HttpOnly: JavaScript no puede leerla. SameSite=Lax: no viaja en peticiones de otros sitios.
    static void guardar(HttpExchange ex, String token) {
        long segundos = IniciarSesion.DURACION.getSeconds();
        ex.getResponseHeaders().add("Set-Cookie", NOMBRE + "=" + token
                + "; Path=/; Max-Age=" + segundos + "; HttpOnly; SameSite=Lax" + seguro(ex));
    }

    static void borrar(HttpExchange ex) {
        ex.getResponseHeaders().add("Set-Cookie", NOMBRE
                + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax" + seguro(ex));
    }

    // En Render la app va por https (detrás de un proxy); en local es http
    private static String seguro(HttpExchange ex) {
        String protocolo = ex.getRequestHeaders().getFirst("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(protocolo) ? "; Secure" : "";
    }
}
