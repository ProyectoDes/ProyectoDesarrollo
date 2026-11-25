package mx.bambuybrillo.vista;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import mx.bambuybrillo.negocio.ConsultarCitas;
import mx.bambuybrillo.negocio.EliminarCita;
import mx.bambuybrillo.negocio.RegistrarCita;
import mx.bambuybrillo.negocio.ModificarCita;
import mx.bambuybrillo.persistencia.CitaRepositorioPostgres;
import mx.bambuybrillo.negocio.ConsultarClientes;
import mx.bambuybrillo.negocio.RegistrarCliente;
import mx.bambuybrillo.negocio.EliminarCliente;
import mx.bambuybrillo.persistencia.ClienteRepositorioPostgres;
import mx.bambuybrillo.negocio.AgregarObservaciones;
import mx.bambuybrillo.negocio.ConsultarHistoria;
import mx.bambuybrillo.negocio.ConsultarProductos;
import mx.bambuybrillo.negocio.ModificarProducto;
import mx.bambuybrillo.persistencia.HistoriaRepositorioPostgres;
import mx.bambuybrillo.persistencia.ProductoRepositorioPostgres;

public class Servidor {

    private static final int PUERTO = 8080;
    private static final String PAGINA_INICIAL = "inicio.html";

    public static void main(String[] args) throws IOException {
        String url = variable("BYB_DB_URL");
        String usuario = variable("BYB_DB_USUARIO");
        String clave = variable("BYB_DB_CLAVE");

        CitaRepositorioPostgres repositorio = new CitaRepositorioPostgres(url, usuario, clave);
        CitasApi api = new CitasApi(
                new RegistrarCita(repositorio),
                new ConsultarCitas(repositorio),
                new EliminarCita(repositorio),
                new ModificarCita(repositorio));

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        api.registrarRutas(servidor);

        ClienteRepositorioPostgres repositorioClientes =
                new ClienteRepositorioPostgres(url, usuario, clave);
        ClientesApi apiClientes = new ClientesApi(
                new RegistrarCliente(repositorioClientes),
                new ConsultarClientes(repositorioClientes),
                new EliminarCliente(repositorioClientes));
        apiClientes.registrarRutas(servidor);

        HistoriaRepositorioPostgres repositorioHistoria =
                new HistoriaRepositorioPostgres(url, usuario, clave);
        ProductoRepositorioPostgres repositorioProductos =
                new ProductoRepositorioPostgres(url, usuario, clave);
        HistoriaApi apiHistoria = new HistoriaApi(
                new ConsultarHistoria(repositorioHistoria),
                new AgregarObservaciones(repositorioHistoria),
                new ConsultarProductos(repositorioProductos),
                new ModificarProducto(repositorioProductos));
        apiHistoria.registrarRutas(servidor);

        Path carpetaVista = localizarCarpetaVista();
        servidor.createContext("/", exchange -> servirArchivo(exchange, carpetaVista));

        servidor.start();
        System.out.println("Servidor listo: http://localhost:" + PUERTO + "/" + PAGINA_INICIAL);
        System.out.println("Sirviendo páginas desde: " + carpetaVista);
    }

    private static String variable(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException("Falta la variable de entorno " + nombre);
        }
        return valor;
    }

    // Busca la carpeta que contiene las páginas HTML, según desde dónde se ejecute
    private static Path localizarCarpetaVista() {
        Path[] candidatas = {Paths.get("vista"), Paths.get("."), Paths.get("..", "vista")};
        for (Path candidata : candidatas) {
            Path carpeta = candidata.toAbsolutePath().normalize();
            if (Files.isRegularFile(carpeta.resolve(PAGINA_INICIAL))) {
                return carpeta;
            }
        }
        throw new IllegalStateException(
                "No se encontró la carpeta con las páginas (" + PAGINA_INICIAL + ").");
    }

    private static void servirArchivo(HttpExchange exchange, Path carpeta) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        if (ruta.equals("/")) {
            ruta = "/" + PAGINA_INICIAL;
        }
        Path archivo = carpeta.resolve(ruta.substring(1)).normalize();

        if (!archivo.startsWith(carpeta) || !Files.isRegularFile(archivo)) {
            byte[] mensaje = "No encontrado".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(404, mensaje.length);
            try (OutputStream salida = exchange.getResponseBody()) {
                salida.write(mensaje);
            }
            return;
        }

        byte[] contenido = Files.readAllBytes(archivo);
        exchange.getResponseHeaders().set("Content-Type", tipoDe(archivo.toString()));
        exchange.sendResponseHeaders(200, contenido.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(contenido);
        }
    }

    private static String tipoDe(String nombre) {
        String minusculas = nombre.toLowerCase();
        if (minusculas.endsWith(".html")) return "text/html; charset=utf-8";
        if (minusculas.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (minusculas.endsWith(".css")) return "text/css; charset=utf-8";
        if (minusculas.endsWith(".png")) return "image/png";
        if (minusculas.endsWith(".jpg") || minusculas.endsWith(".jpeg")) return "image/jpeg";
        if (minusculas.endsWith(".svg")) return "image/svg+xml";
        if (minusculas.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
