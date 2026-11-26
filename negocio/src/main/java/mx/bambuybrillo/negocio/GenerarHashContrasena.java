package mx.bambuybrillo.negocio;

import java.io.Console;
import java.util.Arrays;

// Herramienta para crear el usuario de Celia en Neon sin escribir la contraseña en el código.
// Pide la contraseña sin mostrarla y escribe el INSERT listo para pegar en el SQL Editor de Neon.
public final class GenerarHashContrasena {

    private GenerarHashContrasena() {
    }

    public static void main(String[] args) {
        Console consola = System.console();
        if (consola == null) {
            System.err.println("Ejecuta esta herramienta desde una terminal.");
            return;
        }
        String usuario = consola.readLine("Usuario: ").trim();
        char[] primera = consola.readPassword("Contraseña: ");
        char[] segunda = consola.readPassword("Repite la contraseña: ");
        if (usuario.isEmpty() || primera.length < 8) {
            System.err.println("El usuario es obligatorio y la contraseña debe tener al menos 8 caracteres.");
            return;
        }
        if (!Arrays.equals(primera, segunda)) {
            System.err.println("Las contraseñas no coinciden.");
            return;
        }
        String hash = Contrasenas.cifrar(new String(primera));
        Arrays.fill(primera, ' ');
        Arrays.fill(segunda, ' ');
        System.out.println();
        System.out.println("INSERT INTO usuarios (nombre_usuario, contrasena_hash) VALUES ('"
                + usuario.replace("'", "''") + "', '" + hash + "');");
    }
}
