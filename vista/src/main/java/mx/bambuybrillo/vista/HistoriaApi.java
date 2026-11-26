package mx.bambuybrillo.vista;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.entidad.RegistroHistoria;
import mx.bambuybrillo.negocio.AgregarObservaciones;
import mx.bambuybrillo.negocio.BajaProducto;
import mx.bambuybrillo.negocio.ConsultarHistoria;
import mx.bambuybrillo.negocio.ConsultarProductos;
import mx.bambuybrillo.negocio.ModificarProducto;
import mx.bambuybrillo.negocio.RegistrarProducto;
import mx.bambuybrillo.negocio.ValidacionException;

// Rutas de la historia clínica y de los productos del inventario
public class HistoriaApi {

    private static final String RUTA_HISTORIA = "/api/historia";
    private static final String RUTA_REGISTRO = "/api/historia/cita";
    private static final String RUTA_PRODUCTOS = "/api/productos";

    private final ConsultarHistoria consultarHistoria;
    private final AgregarObservaciones agregarObservaciones;
    private final ConsultarProductos consultarProductos;
    private final ModificarProducto modificarProducto;
    private final BajaProducto bajaProducto;
    private final RegistrarProducto registrarProducto;

    public HistoriaApi(ConsultarHistoria consultarHistoria, AgregarObservaciones agregarObservaciones,
                       ConsultarProductos consultarProductos, ModificarProducto modificarProducto,
                       BajaProducto bajaProducto, RegistrarProducto registrarProducto) {
        this.consultarHistoria = consultarHistoria;
        this.agregarObservaciones = agregarObservaciones;
        this.consultarProductos = consultarProductos;
        this.modificarProducto = modificarProducto;
        this.bajaProducto = bajaProducto;
        this.registrarProducto = registrarProducto;
    }

    public void registrarRutas(HttpServer servidor) {
        servidor.createContext(RUTA_HISTORIA, this::atenderHistoria);
        servidor.createContext(RUTA_REGISTRO, this::atenderRegistro);
        servidor.createContext(RUTA_PRODUCTOS, this::atenderProductos);
    }

    // GET /api/historia?cliente=5 -> registros de ese cliente
    private void atenderHistoria(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equals(ex.getRequestMethod())) {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
                return;
            }
            int idCliente = aEntero(leerConsulta(ex).get("cliente"));
            List<RegistroHistoria> registros = consultarHistoria.listarPorCliente(idCliente);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < registros.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append(aJson(registros.get(i)));
            }
            json.append("]");
            responder(ex, 200, json.toString());
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    // GET /api/historia/cita?id=8 -> un registro; POST agrega observaciones y productos
    private void atenderRegistro(HttpExchange ex) throws IOException {
        try {
            if ("GET".equals(ex.getRequestMethod())) {
                int idCita = aEntero(leerConsulta(ex).get("id"));
                responder(ex, 200, aJson(consultarHistoria.buscarPorCita(idCita)));
            } else if ("POST".equals(ex.getRequestMethod())) {
                Map<String, List<String>> datos = leerFormulario(ex);
                List<Integer> productos = new ArrayList<>();
                for (String valor : datos.getOrDefault("productos", new ArrayList<>())) {
                    productos.add(aEntero(valor));
                }
                String mensaje = agregarObservaciones.agregar(
                        aEntero(primero(datos, "id_cita")), primero(datos, "observaciones"), productos);
                responder(ex, 200, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
            } else {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
            }
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    // GET /api/productos -> productos del inventario
    // GET /api/productos?id=5 -> datos actuales de un producto
    // POST /api/productos -> registra un producto nuevo
    // PUT /api/productos/5 -> guarda los cambios de un producto
    // DELETE /api/productos/5 -> da de baja un producto
    private void atenderProductos(HttpExchange ex) throws IOException {
        try {
            String metodo = ex.getRequestMethod();
            if ("GET".equals(metodo)) {
                String id = leerConsulta(ex).get("id");
                if (id != null) {
                    responder(ex, 200, aJson(modificarProducto.buscar(aEntero(id))));
                } else {
                    listarProductos(ex);
                }
            } else if ("POST".equals(metodo)) {
                Map<String, List<String>> datos = leerFormulario(ex);
                String mensaje = registrarProducto.registrar(
                        primero(datos, "nombre"), primero(datos, "descripcion"), primero(datos, "cantidad"));
                responder(ex, 201, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
            } else if ("PUT".equals(metodo)) {
                actualizarProducto(ex);
            } else if ("DELETE".equals(metodo)) {
                String mensaje = bajaProducto.eliminar(idDeRuta(ex));
                responder(ex, 200, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
            } else {
                responder(ex, 405, "{\"error\":\"Metodo no permitido.\"}");
            }
        } catch (RuntimeException e) {
            manejarError(ex, e);
        }
    }

    private void listarProductos(HttpExchange ex) throws IOException {
        List<Producto> productos = consultarProductos.listar();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productos.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(aJson(productos.get(i)));
        }
        json.append("]");
        responder(ex, 200, json.toString());
    }

    // Lee el id que viene al final de la ruta: /api/productos/5 -> 5
    private int idDeRuta(HttpExchange ex) {
        String ruta = ex.getRequestURI().getPath();
        return ruta.length() > RUTA_PRODUCTOS.length() + 1
                ? aEntero(ruta.substring(RUTA_PRODUCTOS.length() + 1))
                : 0;
    }

    private void actualizarProducto(HttpExchange ex) throws IOException {
        int idProducto = idDeRuta(ex);
        Map<String, List<String>> datos = leerFormulario(ex);
        Producto producto = new Producto(
                idProducto,
                primero(datos, "nombre"),
                primero(datos, "descripcion"),
                leerCantidad(primero(datos, "cantidad")));
        String mensaje = modificarProducto.modificar(producto);
        responder(ex, 200, "{\"mensaje\":\"" + escapar(mensaje) + "\"}");
    }

    // La cantidad debe ser un número entero; si no, se avisa en el campo
    private int leerCantidad(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("cantidad", "La cantidad debe ser un número entero.");
        }
    }

    private String aJson(Producto p) {
        return "{\"id\":" + p.getIdProducto()
                + ",\"nombre\":\"" + escapar(p.getNombre()) + "\""
                + ",\"descripcion\":\"" + escapar(p.getDescripcion()) + "\""
                + ",\"cantidad\":" + p.getCantidad() + "}";
    }

    private String aJson(RegistroHistoria r) {
        StringBuilder productos = new StringBuilder("[");
        for (int i = 0; i < r.getProductos().size(); i++) {
            if (i > 0) {
                productos.append(",");
            }
            productos.append(aJson(r.getProductos().get(i)));
        }
        productos.append("]");
        return "{\"idCita\":" + r.getIdCita()
                + ",\"idCliente\":" + r.getIdCliente()
                + ",\"cliente\":\"" + escapar(r.getNombreCliente()) + "\""
                + ",\"fecha\":\"" + escapar(r.getFecha()) + "\""
                + ",\"horaInicio\":\"" + escapar(r.getHoraInicio()) + "\""
                + ",\"horaFin\":\"" + escapar(r.getHoraFin()) + "\""
                + ",\"servicio\":\"" + escapar(r.getServicio()) + "\""
                + ",\"observaciones\":\"" + escapar(r.getObservaciones()) + "\""
                + ",\"productos\":" + productos + "}";
    }

    private Map<String, String> leerConsulta(HttpExchange ex) {
        Map<String, String> datos = new HashMap<>();
        String consulta = ex.getRequestURI().getRawQuery();
        if (consulta == null || consulta.isEmpty()) {
            return datos;
        }
        for (String par : consulta.split("&")) {
            String[] partes = par.split("=", 2);
            datos.put(URLDecoder.decode(partes[0], StandardCharsets.UTF_8),
                    partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "");
        }
        return datos;
    }

    // Igual que en las otras API, pero guarda todos los valores de una misma clave
    // (para poder recibir varios productos a la vez)
    private Map<String, List<String>> leerFormulario(HttpExchange ex) throws IOException {
        String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, List<String>> datos = new HashMap<>();
        if (cuerpo.isEmpty()) {
            return datos;
        }
        for (String par : cuerpo.split("&")) {
            String[] partes = par.split("=", 2);
            String clave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            datos.computeIfAbsent(clave, k -> new ArrayList<>()).add(valor);
        }
        return datos;
    }

    private String primero(Map<String, List<String>> datos, String clave) {
        List<String> valores = datos.get(clave);
        return valores == null || valores.isEmpty() ? "" : valores.get(0);
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
