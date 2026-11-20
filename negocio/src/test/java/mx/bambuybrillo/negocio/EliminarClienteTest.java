package mx.bambuybrillo.negocio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EliminarClienteTest {

    @Test
    void idInvalidoNoBorraNada() {
        RegistrarClienteTest.RepositorioFalso repositorio = new RegistrarClienteTest.RepositorioFalso();
        EliminarCliente eliminar = new EliminarCliente(repositorio);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> eliminar.eliminar(0));

        assertEquals("El cliente a dar de baja no existe.", e.getMessage());
        assertEquals(0, repositorio.eliminado);
    }

    @Test
    void clienteValidoSeDaDeBaja() {
        RegistrarClienteTest.RepositorioFalso repositorio = new RegistrarClienteTest.RepositorioFalso();
        EliminarCliente eliminar = new EliminarCliente(repositorio);

        assertEquals("Cliente dado de baja correctamente.", eliminar.eliminar(7));
        assertEquals(7, repositorio.eliminado);
    }
}
