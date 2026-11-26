package mx.bambuybrillo.negocio;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mx.bambuybrillo.entidad.Cliente;
import mx.bambuybrillo.persistencia.ClienteRepositorio;

public class ConsultarClientes {

    private final ClienteRepositorio repositorio;

    public ConsultarClientes(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Regresa los clientes ordenados por nombre, sin importar mayusculas ni acentos
    public List<Cliente> listar() {
        List<Cliente> clientes = repositorio.listar();
        if (clientes == null) {
            return new ArrayList<>();
        }
        Collator orden = Collator.getInstance(Locale.forLanguageTag("es-MX"));
        orden.setStrength(Collator.PRIMARY);
        List<Cliente> ordenados = new ArrayList<>(clientes);
        ordenados.sort((a, b) -> orden.compare(
                a.getNombreCliente() == null ? "" : a.getNombreCliente(),
                b.getNombreCliente() == null ? "" : b.getNombreCliente()));
        return ordenados;
    }
}
