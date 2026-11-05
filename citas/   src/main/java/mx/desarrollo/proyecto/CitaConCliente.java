package mx.desarrollo.proyecto;

public class CitaConCliente {
    private int idCita;
    private int idCliente;
    private String nombreCliente;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String servicio;

    public CitaConCliente() {
    }

    public CitaConCliente(int idCita, int idCliente, String nombreCliente,
                          String fecha, String horaInicio, String horaFin,
                          String servicio) {
        this.idCita = idCita;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.servicio = servicio;
    }

    public int getIdCita() { return idCita; }

    public void setIdCita(int idCita) { this.idCita = idCita; }

    public int getIdCliente() { return idCliente; }

    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombreCliente() { return nombreCliente; }

    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getFecha() { return fecha; }

    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }

    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }

    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getServicio() { return servicio; }

    public void setServicio(String servicio) { this.servicio = servicio; }
}