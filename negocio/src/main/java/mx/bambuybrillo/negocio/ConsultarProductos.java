package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Producto;
import mx.bambuybrillo.persistencia.ProductoRepositorio;

public class ConsultarProductos {

    private final ProductoRepositorio repositorio;

    public ConsultarProductos(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Producto> listar() {
        List<Producto> productos = repositorio.listar();
        return productos == null ? new ArrayList<>() : productos;
    }
}
