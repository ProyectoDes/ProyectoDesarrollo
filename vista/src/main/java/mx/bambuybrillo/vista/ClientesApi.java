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

import mx.bambuybrillo.entidad.Cliente;
import mx.bambuybrillo.negocio.ConsultarClientes;
import mx.bambuybrillo.negocio.EliminarCliente;
import mx.bambuybrillo.negocio.RegistrarCliente;
import mx.bambuybrillo.negocio.ValidacionException;


public class ClientesApi {

    private static final String RUTA_REGISTRO = "/api/clientes/registro";
    private static final String RUTA_LISTA = "/api/clientes/lista";
    private static final String RUTA_BAJA = "/api/clientes/baja/";

    private final RegistrarCliente registrarCliente;
    private final ConsultarClientes consultarClientes;
    private final EliminarCliente eliminarCliente;

    public ClientesApi(RegistrarCliente registrarCliente, ConsultarClientes consultarClientes,
                       EliminarCliente eliminarCliente) {
        this.registrarCliente = registrarCliente;
        this.consultarClientes = consultarClientes;
        this.eliminarCliente = eliminarCliente;
    }

    public void registrarRutas(HttpServer servidor) {
        servidor.createContext(RUTA_REGISTRO, this::atenderRegistro);
        servidor.createContext(RUTA_LISTA, this::atenderLista);
        servidor.createContext(RUTA_BAJA, this::atenderBaja);
    }

    // DELETE /api/clientes/baja/5 -> borra al cliente con sus citas e historia clínica
    private void atenderBaja(HttpExchange ex) throws IOException {
        try {
            if (!"DELETE".equals(ex.getRequestMethod())) {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
                return;
            }
            int id = aEntero(ex.getRequestURI().getPath().substring(RUTA_BAJA.length()));
            String mensaje = eliminarCliente.eliminar(id);
            responder(ex, 200, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    // Lista de clientes con nombre, edad y telefono para la pantalla de Clientes
    private void atenderLista(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equals(ex.getRequestMethod())) {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
                return;
            }
            List<Cliente> clientes = consultarClientes.listar();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < clientes.size(); i++) {
                Cliente c = clientes.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{\"id\":").append(c.getIdCliente())
                        .append(",\"nombre\":\"").append(escapar(c.getNombreCliente())).append("\"")
                        .append(",\"edad\":").append(c.getEdadCliente())
                        .append(",\"telefono\":\"").append(escapar(c.getTelefonoCliente())).append("\"}");
            }
            json.append("]");
            responder(ex, 200, json.toString());
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    private void atenderRegistro(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
                return;
            }
            Map<String, String> datos = leerFormulario(ex);

            Cliente cliente = new Cliente();
            cliente.setNombreCliente(datos.getOrDefault("nombre", "").trim());
            cliente.setEdadCliente(aEntero(datos.get("edad")));
            cliente.setTelefonoCliente(datos.getOrDefault("telefono", "").trim());

            String mensaje = registrarCliente.registrar(cliente);
            responder(ex, 201, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
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
        if (e instanceof ValidacionException) {
            ValidacionException v = (ValidacionException) e;
            responder(ex, 400, "{\"error\":\"" + escapar(v.getMessage())
                    + "\",\"campo\":\"" + escapar(v.getCampo()) + "\"}");
        } else if (e instanceof IllegalArgumentException) {
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
