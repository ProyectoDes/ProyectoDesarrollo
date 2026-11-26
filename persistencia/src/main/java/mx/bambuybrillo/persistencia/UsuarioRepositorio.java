package mx.bambuybrillo.persistencia;

import java.util.Optional;

import mx.bambuybrillo.entidad.Usuario;

public interface UsuarioRepositorio {
    // Busca el usuario sin importar mayúsculas ni espacios en el nombre
    Optional<Usuario> buscarPorNombre(String nombreUsuario);
}
