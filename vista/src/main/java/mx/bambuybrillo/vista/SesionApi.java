package mx.bambuybrillo.vista;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import mx.bambuybrillo.negocio.CredencialesIncorrectasException;
import mx.bambuybrillo.negocio.IniciarSesion;
import mx.bambuybrillo.negocio.ValidacionException;
import mx.bambuybrillo.negocio.ValidarSesion;

// POST /api/sesion -> inicia sesión; GET -> revisa si hay sesión; DELETE -> cierra sesión
public class SesionApi {

    static final String RUTA = "/api/sesion";

    private final IniciarSesion iniciarSesion;
    private final ValidarSesion validarSesion;

    public SesionApi(IniciarSesion iniciarSesion, ValidarSesion validarSesion) {
        this.iniciarSesion = iniciarSesion;
        this.validarSesion = validarSesion;
    }

    public void registrarRutas(HttpServer servidor) {
        servidor.createContext(RUTA, this::atender);
    }

    private void atender(HttpExchange ex) throws IOException {
        try {
            switch (ex.getRequestMethod()) {
                case "POST" -> {
                    Map<String, String> datos = leerFormulario(ex);
                    String token = iniciarSesion.iniciar(datos.get("usuario"), datos.get("contrasena"));
                    CookieSesion.guardar(ex, token);
                    responder(ex, 200, "{\"mensaje\":\"Sesión iniciada.\"}");
                }
                case "GET" -> {
                    boolean activa = validarSesion.esValida(CookieSesion.leer(ex));
                    responder(ex, activa ? 200 : 401, "{\"activa\":" + activa + "}");
                }
                case "DELETE" -> {
                    validarSesion.cerrar(CookieSesion.leer(ex));
                    CookieSesion.borrar(ex);
                    responder(ex, 200, "{\"mensaje\":\"Sesión cerrada.\"}");
                }
                default -> responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
            }
        } catch (ValidacionException e) {
            responder(ex, 400, "{\"error\":\"" + escapar(e.getMessage())
                    + "\",\"campo\":\"" + escapar(e.getCampo()) + "\"}");
        } catch (CredencialesIncorrectasException e) {
            responder(ex, 401, "{\"error\":\"" + escapar(e.getMessage()) + "\"}");
        } catch (RuntimeException e) {
            e.printStackTrace();
            responder(ex, 500, "{\"error\":\"No se pudo completar la operacion.\"}");
        }
    }

    private Map<String, String> leerFormulario(HttpExchange ex) throws IOException {
        String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> datos = new HashMap<>();
        for (String par : cuerpo.split("&")) {
            if (par.isEmpty()) {
                continue;
            }
            String[] partes = par.split("=", 2);
            datos.put(URLDecoder.decode(partes[0], StandardCharsets.UTF_8),
                    partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "");
        }
        return datos;
    }

    static void responder(HttpExchange ex, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream salida = ex.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
