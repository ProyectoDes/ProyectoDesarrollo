package mx.desarrollo.proyecto;

public class RegistrarCliente {

    public static final int LONGITUD_MAX_NOMBRE = 80;
    public static final int LONGITUD_TELEFONO = 10;
    public static final int EDAD_MIN = 1;
    public static final int EDAD_MAX = 120;

    private final ClienteRepositorio repositorio;

    public RegistrarCliente(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String registrar(Cliente cliente) {
        validar(cliente);
        repositorio.guardar(cliente);
        return "Cliente registrado correctamente.";
    }

    public String modificar(Cliente cliente) {
        if (cliente != null && cliente.getIdCliente() <= 0) {
            throw new IllegalArgumentException("El cliente a modificar no existe.");
        }
        validar(cliente);
        repositorio.actualizar(cliente);
        return "Cliente actualizado correctamente.";
    }

    public void validar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Los datos del cliente son obligatorios.");
        }

        String nombre = cliente.getNombreCliente();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa el nombre del cliente.");
        }
        if (nombre.trim().length() > LONGITUD_MAX_NOMBRE) {
            throw new IllegalArgumentException(
                    "El nombre no puede pasar de " + LONGITUD_MAX_NOMBRE + " caracteres.");
        }

        int edad = cliente.getEdadCliente();
        if (edad < EDAD_MIN || edad > EDAD_MAX) {
            throw new IllegalArgumentException(
                    "Ingresa una edad entre " + EDAD_MIN + " y " + EDAD_MAX + ".");
        }

        String telefono = cliente.getTelefonoCliente();
        if (telefono == null || !telefono.matches("\\d{" + LONGITUD_TELEFONO + "}")) {
            String mensaje = "Ingresa un teléfono de " + LONGITUD_TELEFONO + " dígitos.";
            if (telefono != null && telefono.matches("\\d+")
                    && telefono.length() < LONGITUD_TELEFONO) {
                mensaje += " Faltan " + (LONGITUD_TELEFONO - telefono.length()) + ".";
            }
            throw new IllegalArgumentException(mensaje);
        }
    }
}
