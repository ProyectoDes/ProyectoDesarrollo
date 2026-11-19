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
        Cliente cliente = new Cliente(0, "", 30, "5512345678", "cliente@correo.com");

        RegistrarCliente.ValidacionClienteException e = assertThrows(
                RegistrarCliente.ValidacionClienteException.class,
                () -> registrar.registrar(cliente)
        );

        assertEquals("nombre", e.getCampo());
        assertEquals("Ingresa el nombre del cliente.", e.getMessage());
        assertEquals(0, repositorio.guardados);
    }

    @Test
    void telefonoInvalidoBloqueaGuardado() {
        RepositorioFalso repositorio = new RepositorioFalso();
        RegistrarCliente registrar = new RegistrarCliente(repositorio);
        Cliente cliente = new Cliente(0, "Ana López", 25, "55123", "ana@correo.com");

        RegistrarCliente.ValidacionClienteException e = assertThrows(
                RegistrarCliente.ValidacionClienteException.class,
                () -> registrar.registrar(cliente)
        );

        assertEquals("telefono", e.getCampo());
        assertEquals(0, repositorio.guardados);
    }

    @Test
    void correoSinArrobaODominioBloqueaGuardado() {
        RepositorioFalso repositorio = new RepositorioFalso();
        RegistrarCliente registrar = new RegistrarCliente(repositorio);
        Cliente cliente = new Cliente(0, "Ana López", 25, "5512345678", "correo_invalido");

        RegistrarCliente.ValidacionClienteException e = assertThrows(
                RegistrarCliente.ValidacionClienteException.class,
                () -> registrar.registrar(cliente)
        );

        assertEquals("correo", e.getCampo());
        assertEquals(0, repositorio.guardados);
    }

    @Test
    void clienteValidoGuardaCorrectamente() {
        RepositorioFalso repositorio = new RepositorioFalso();
        RegistrarCliente registrar = new RegistrarCliente(repositorio);
        Cliente cliente = new Cliente(0, "Ana López", 25, "5512345678", "ana@correo.com");

        String resultado = registrar.registrar(cliente);

        assertEquals("Cliente registrado correctamente.", resultado);
        assertEquals(1, repositorio.guardados);
    }
}
