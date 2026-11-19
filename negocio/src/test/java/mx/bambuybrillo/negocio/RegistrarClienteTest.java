package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Cliente;
import mx.bambuybrillo.persistencia.ClienteRepositorio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarClienteTest {

    // Repositorio falso: cuenta los guardados en vez de usar la base de datos
    static class RepositorioFalso implements ClienteRepositorio {
        int guardados = 0;

        @Override
        public void guardar(Cliente cliente) {
            guardados++;
        }

        @Override
        public void actualizar(Cliente cliente) {
        }
    }

    @Test
    void nombreVacioLanzaExcepcionYNoGuarda() {
        RepositorioFalso repositorio = new RepositorioFalso();
        RegistrarCliente registrar = new RegistrarCliente(repositorio);
        Cliente cliente = new Cliente(0, "", 30, "5512345678");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> registrar.registrar(cliente));

        assertEquals("Ingresa el nombre del cliente.", e.getMessage());
        assertEquals(0, repositorio.guardados);
    }
}