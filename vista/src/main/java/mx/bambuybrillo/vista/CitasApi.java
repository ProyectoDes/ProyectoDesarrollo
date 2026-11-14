package mx.bambuybrillo.vista;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.entidad.CitaConCliente;
import mx.bambuybrillo.entidad.ClienteOpcion;
import mx.bambuybrillo.negocio.ConsultarCitas;
import mx.bambuybrillo.negocio.EliminarCita;
import mx.bambuybrillo.negocio.RegistrarCita;

public class CitasApi {

    private static final String RUTA_CITAS = "/api/citas";
    private static final String RUTA_CLIENTES = "/api/clientes";

    private final RegistrarCita registrarCita;
    private final ConsultarCitas consultarCitas;
    private final EliminarCita eliminarCita;

    public CitasApi(RegistrarCita registrarCita, ConsultarCitas consultarCitas,
                    EliminarCita eliminarCita) {
        this.registrarCita = registrarCita;
        this.consultarCitas = consultarCitas;
        this.eliminarCita = eliminarCita;
    }

    public void registrarRutas(HttpServer servidor) {
        servidor.createContext(RUTA_CLIENTES, this::atenderClientes);
        servidor.createContext(RUTA_CITAS, this::atenderCitas);
    }

    private void atenderClientes(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equals(ex.getRequestMethod())) {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
                return;
            }
            List<ClienteOpcion> clientes = consultarCitas.listarClientes();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < clientes.size(); i++) {
                ClienteOpcion c = clientes.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{\"id\":").append(c.idCliente())
                        .append(",\"nombre\":\"").append(escapar(c.nombre())).append("\"}");
            }
            json.append("]");
            responder(ex, 200, json.toString());
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    private void atenderCitas(HttpExchange ex) throws IOException {
        try {
            String metodo = ex.getRequestMethod();
            String ruta = ex.getRequestURI().getPath();
            if ("GET".equals(metodo) && RUTA_CITAS.equals(ruta)) {
                listar(ex);
            } else if ("POST".equals(metodo) && RUTA_CITAS.equals(ruta)) {
                crear(ex);
            } else if ("DELETE".equals(metodo) && ruta.startsWith(RUTA_CITAS + "/")) {
                borrar(ex, ruta);
            } else {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
            }
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    private void listar(HttpExchange ex) throws IOException {
        List<CitaConCliente> citas = consultarCitas.listar();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < citas.size(); i++) {
            CitaConCliente c = citas.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"id\":").append(c.getIdCita())
                    .append(",\"idCliente\":").append(c.getIdCliente())
                    .append(",\"cliente\":\"").append(escapar(c.getNombreCliente())).append("\"")
                    .append(",\"fecha\":\"").append(escapar(c.getFecha())).append("\"")
                    .append(",\"horaInicio\":\"").append(escapar(c.getHoraInicio())).append("\"")
                    .append(",\"horaFin\":\"").append(escapar(c.getHoraFin())).append("\"")
                    .append(",\"servicio\":\"").append(escapar(c.getServicio())).append("\"}");
        }
        json.append("]");
        responder(ex, 200, json.toString());
    }

    private void crear(HttpExchange ex) throws IOException {
        Map<String, String> datos = leerFormulario(ex);

        String servicio = datos.getOrDefault("servicio", "").trim();
        String personalizado = datos.getOrDefault("servicio_personalizado", "").trim();
        if (!personalizado.isEmpty()) {
            servicio = personalizado;
        }

        Cita cita = new Cita();
        cita.setIdCliente(aEntero(datos.get("id_cliente")));
        cita.setFecha(datos.getOrDefault("fecha", "").trim());
        cita.setHoraInicio(datos.getOrDefault("hora_inicio", "").trim());
        cita.setHoraFin(datos.getOrDefault("hora_fin", "").trim());
        cita.setServicio(servicio);

        String mensaje = registrarCita.registrar(cita);
        responder(ex, 201, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
    }

    private void borrar(HttpExchange ex, String ruta) throws IOException {
        int id;
        try {
            id = Integer.parseInt(ruta.substring((RUTA_CITAS + "/").length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cita a eliminar no existe.");
        }
        String mensaje = eliminarCita.eliminar(id);
        responder(ex, 200, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
    }

    private Map<String, String> leerFormulario(HttpExchange ex) throws IOException {
        String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> datos = new HashMap<>();
        if (cuerpo.isEmpty()) {
            return datos;
        }
        for (String par : cuerpo.split("&")) {
            String[] partes = par.split("=", 2);
            String clave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length > 1
                    ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            datos.put(clave, valor);
        }
        return datos;
    }

    private int aEntero(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void manejarError(HttpExchange ex, RuntimeException e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            responder(ex, 400, "{\"error\":\"" + escapar(e.getMessage()) + "\"}");
        } else {
            e.printStackTrace();
            responder(ex, 500, "{\"error\":\"No se pudo completar la operacion.\"}");
        }
    }

    private void responder(HttpExchange ex, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream salida = ex.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}