package citas.mx;

public class Cita {
    private String id;
    private String cliente;
    private String fecha;       // Formato "YYYY-MM-DD" o "DD/MM/YYYY"
    private String horaInicio;  // Formato "HH:mm"
    private String horaFin;     // Formato "HH:mm"
    private String servicio;


    public Cita(String id, String cliente, String fecha, String horaInicio, String horaFin, String servicio) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.servicio = servicio;
    }


    public String getCliente() { return cliente; }
    public String getFecha() { return fecha; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
}
