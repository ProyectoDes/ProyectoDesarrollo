package mx.bambuybrillo.negocio;

import java.util.ArrayList;
import java.util.List;

import mx.bambuybrillo.entidad.Cliente;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarClientesTest {

    static class RepositorioFalso extends RegistrarClienteTest.RepositorioFalso {
        List<Cliente> clientes = new ArrayList<>();

        @Override
        public List<Cliente> listar() {
            return clientes;
        }
    }

    @Test
    void listaOrdenadaPorNombre() {
        RepositorioFalso repositorio = new RepositorioFalso();
        repositorio.clientes.add(new Cliente(1, "ramón Juárez", 30, "5512345678"));
        repositorio.clientes.add(new Cliente(2, "Ana Prueba", 25, "5598765432"));
        repositorio.clientes.add(new Cliente(3, "Émilio Serrato", 40, "5511112222"));

        List<Cliente> lista = new ConsultarClientes(repositorio).listar();

        assertEquals(3, lista.size());
        assertEquals("Ana Prueba", lista.get(0).getNombreCliente());
        assertEquals("Émilio Serrato", lista.get(1).getNombreCliente());
        assertEquals("ramón Juárez", lista.get(2).getNombreCliente());
    }

    @Test
    void sinClientesRegresaListaVacia() {
        RepositorioFalso repositorio = new RepositorioFalso();
        repositorio.clientes = null;

        assertTrue(new ConsultarClientes(repositorio).listar().isEmpty());
    }
}
