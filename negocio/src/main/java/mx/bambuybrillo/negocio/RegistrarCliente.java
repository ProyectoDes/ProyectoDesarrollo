package mx.bambuybrillo.negocio;

import mx.bambuybrillo.entidad.Cliente;
import mx.bambuybrillo.persistencia.ClienteRepositorio;

public class RegistrarCliente {

    public static final int LONGITUD_MAX_NOMBRE = 80;
    public static final int LONGITUD_TELEFONO = 10;
    public static final int EDAD_MIN = 1;
    public static final int EDAD_MAX = 120;
    private static final String REGEX_CORREO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final ClienteRepositorio repositorio;

    public RegistrarCliente(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public static class ValidacionClienteException extends IllegalArgumentException {
        private final String campo;

        public ValidacionClienteException(String campo, String mensaje) {
            super(mensaje);
            this.campo = campo;
        }

        public String getCampo() {
            return campo;
        }
    }

    public String registrar(Cliente cliente) {
        validar(cliente);
        repositorio.guardar(cliente);
        return "Cliente registrado correctamente.";
    }

    public String modificar(Cliente cliente) {
        if (cliente != null && cliente.getIdCliente() <= 0) {
            throw new ValidacionClienteException("idCliente", "El cliente a modificar no existe.");
        }
        validar(cliente);
        repositorio.actualizar(cliente);
        return "Cliente actualizado correctamente.";
    }

    public void validar(Cliente cliente) {
        if (cliente == null) {
            throw new ValidacionClienteException("cliente", "Los datos del cliente son obligatorios.");
        }

        String nombre = cliente.getNombreCliente();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionClienteException("nombre", "Ingresa el nombre del cliente.");
        }
        if (nombre.trim().length() > LONGITUD_MAX_NOMBRE) {
            throw new ValidacionClienteException(
                    "nombre",
                    "El nombre no puede pasar de " + LONGITUD_MAX_NOMBRE + " caracteres."
            );
        }

        int edad = cliente.getEdadCliente();
        if (edad < EDAD_MIN || edad > EDAD_MAX) {
            throw new ValidacionClienteException(
                    "edad",
                    "Ingresa una edad entre " + EDAD_MIN + " y " + EDAD_MAX + "."
            );
        }

        String telefono = cliente.getTelefonoCliente();
        if (telefono == null || !telefono.matches("\\d{" + LONGITUD_TELEFONO + "}")) {
            String mensaje = "Ingresa un teléfono de " + LONGITUD_TELEFONO + " dígitos.";
            if (telefono != null && telefono.matches("\\d+")
                    && telefono.length() < LONGITUD_TELEFONO) {
                mensaje += " Faltan " + (LONGITUD_TELEFONO - telefono.length()) + ".";
            }
            throw new ValidacionClienteException("telefono", mensaje);
        }

        String correo = cliente.getCorreoCliente();
        if (correo == null || correo.trim().isEmpty()) {
            throw new ValidacionClienteException("correo", "Ingresa el correo electrónico del cliente.");
        }
        if (!correo.trim().matches(REGEX_CORREO)) {
            throw new ValidacionClienteException(
                    "correo",
                    "Ingresa un correo válido con '@' y dominio (ejemplo: usuario@dominio.com)."
            );
        }
    }
}