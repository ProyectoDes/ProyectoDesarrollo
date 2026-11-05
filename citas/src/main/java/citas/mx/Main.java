package citas.mx;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<Cita> citasExistentes = new ArrayList<>();
        citasExistentes.add(new Cita("1", "Ana Martínez", "2026-11-02", "10:00", "11:00", "Limpieza facial"));
        citasExistentes.add(new Cita("2", "Lucía Torres", "2026-11-02", "12:00", "12:45", "Manicure"));


        String nuevaFecha = "2026-11-02";
        String nuevaHoraInicio = "10:30";
        String nuevaHoraFin = "11:30";

        System.out.println("--- PROBANDO VALIDACIÓN DE DISPONIBILIDAD ---");
        ValidadorCitas.ResultadoVerificacion resultado = ValidadorCitas.verificarDisponibilidad(
                nuevaFecha, nuevaHoraInicio, nuevaHoraFin, citasExistentes
        );


        System.out.println("Estado: " + resultado.getEstado().toUpperCase());
        System.out.println("Mensaje: " + resultado.getMensaje());
    }
}