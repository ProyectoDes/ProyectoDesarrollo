package mx.bambuybrillo.entidad;

import java.util.ArrayList;
import java.util.List;

public class Cita {
    private int idCita;
    private int idCliente;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String servicio;
    // Productos del inventario que se usarán en la cita (sus id)
    private List<Integer> productos = new ArrayList<>();

    public Cita() {
    }

    public Cita(int idCita, int idCliente, String fecha,
                String horaInicio, String horaFin, String servicio) {
        this.idCita = idCita;
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.servicio = servicio;
    }

    public int getIdCita() { return idCita; }

    public void setIdCita(int idCita) { this.idCita = idCita; }

    public int getIdCliente() { return idCliente; }

    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getFecha() { return fecha; }

    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }

    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }

    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getServicio() { return servicio; }

    public void setServicio(String servicio) { this.servicio = servicio; }

    public List<Integer> getProductos() { return productos; }

    public void setProductos(List<Integer> productos) {
        this.productos = productos == null ? new ArrayList<>() : productos;
    }
}
