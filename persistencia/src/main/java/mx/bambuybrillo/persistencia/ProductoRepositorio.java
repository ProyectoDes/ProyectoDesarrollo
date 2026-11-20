package mx.bambuybrillo.persistencia;

import java.util.List;

import mx.bambuybrillo.entidad.Producto;

public interface ProductoRepositorio {
    List<Producto> listar();
}
