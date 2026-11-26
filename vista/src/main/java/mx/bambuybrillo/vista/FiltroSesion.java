package mx.bambuybrillo.vista;

import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import mx.bambuybrillo.negocio.ValidarSesion;

// Sin sesión iniciada: las pantallas mandan al inicio de sesión y la API responde 401
public class FiltroSesion extends Filter {

    static final String PANTALLA_INICIO_SESION = "/inicio-sesion.html";

    private final ValidarSesion validarSesion;

    public FiltroSesion(ValidarSesion validarSesion) {
        this.validarSesion = validarSesion;
    }

    // Lo único que se puede abrir sin sesión
    static boolean esPublica(String ruta) {
        return ruta.equals(PANTALLA_INICIO_SESION)
                || ruta.equals("/js/inicio-sesion.js")
                || ruta.startsWith("/css/")
                || ruta.equals(SesionApi.RUTA)
                || ruta.equals("/favicon.ico");
    }

    @Override
    public void doFilter(HttpExchange ex, Chain cadena) throws IOException {
        String ruta = ex.getRequestURI().getPath();
        if (esPublica(ruta) || validarSesion.esValida(CookieSesion.leer(ex))) {
            cadena.doFilter(ex);
            return;
        }
        if (ruta.startsWith("/api/")) {
            SesionApi.responder(ex, 401, "{\"error\":\"Inicia sesión para continuar.\"}");
        } else {
            ex.getResponseHeaders().set("Location", PANTALLA_INICIO_SESION);
            ex.sendResponseHeaders(302, -1);
            ex.close();
        }
    }

    @Override
    public String description() {
        return "Pide iniciar sesión antes de usar el sistema";
    }
}
