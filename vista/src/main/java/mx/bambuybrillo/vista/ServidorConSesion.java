package mx.bambuybrillo.vista;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// Envuelve el HttpServer para que TODAS las rutas que se registren pasen por el filtro de sesión,
// sin tener que tocar cada API
public class ServidorConSesion extends HttpServer {

    private final HttpServer servidor;
    private final Filter filtro;

    public ServidorConSesion(HttpServer servidor, Filter filtro) {
        this.servidor = servidor;
        this.filtro = filtro;
    }

    @Override
    public HttpContext createContext(String ruta, HttpHandler manejador) {
        HttpContext contexto = servidor.createContext(ruta, manejador);
        contexto.getFilters().add(filtro);
        return contexto;
    }

    @Override
    public HttpContext createContext(String ruta) {
        HttpContext contexto = servidor.createContext(ruta);
        contexto.getFilters().add(filtro);
        return contexto;
    }

    @Override
    public void bind(InetSocketAddress direccion, int pendientes) throws IOException {
        servidor.bind(direccion, pendientes);
    }

    @Override
    public void start() {
        servidor.start();
    }

    @Override
    public void setExecutor(Executor ejecutor) {
        servidor.setExecutor(ejecutor);
    }

    @Override
    public Executor getExecutor() {
        return servidor.getExecutor();
    }

    @Override
    public void stop(int espera) {
        servidor.stop(espera);
    }

    @Override
    public void removeContext(String ruta) {
        servidor.removeContext(ruta);
    }

    @Override
    public void removeContext(HttpContext contexto) {
        servidor.removeContext(contexto);
    }

    @Override
    public InetSocketAddress getAddress() {
        return servidor.getAddress();
    }
}
