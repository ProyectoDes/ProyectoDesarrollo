package mx.bambuybrillo.entidad;

// Usuario que puede entrar al sistema (por ahora solo Celia, la administradora)
public class Usuario {
    private int idUsuario;
    private String nombreUsuario;
    // Contraseña cifrada con PBKDF2; nunca se guarda la contraseña tal cual
    private String contrasenaHash;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombreUsuario, String contrasenaHash) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = contrasenaHash;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }
}
