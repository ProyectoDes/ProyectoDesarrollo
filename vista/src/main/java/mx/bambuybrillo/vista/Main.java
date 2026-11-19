package mx.bambuybrillo.vista;

import mx.bambuybrillo.entidad.Cita;
import mx.bambuybrillo.persistencia.CitaRepositorio;
import mx.bambuybrillo.negocio.GestorCitas;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        List<Cita> citasExistentes = new ArrayList<>();
        CitaRepositorio repositorioMemoria = new CitaRepositorio() {
            @Override
            public void guardar(Cita cita) {
                System.out.println("  [BD] ¡Cita registrada y guardada con éxito para: " + cita.getCliente() + "!");
            }
        };

        GestorCitas gestor = new GestorCitas(repositorioMemoria, citasExistentes);
        Scanner scanner = new Scanner(System.in);
        int idAutoincrementable = 1;
        boolean continuar = true;

        System.out.println("==================================================");
        System.out.println("   SISTEMA DE REGISTRO MANUAL (AGENDA VACÍA)     ");
        System.out.println("==================================================");

        while (continuar) {
            System.out.println("\n--- Ingresa los datos de la nueva cita ---");
            System.out.print("Nombre del cliente: ");
            String cliente = scanner.nextLine();
            System.out.print("Fecha (Formato YYYY-MM-DD): ");
            String fecha = scanner.nextLine();
            System.out.print("Hora de inicio (Formato HH:mm): ");
            String horaInicio = scanner.nextLine();
            System.out.print("Hora de fin (Formato HH:mm): ");
            String horaFin = scanner.nextLine();
            System.out.print("Servicio a realizar: ");
            String servicio = scanner.nextLine();

            Cita nuevaCita = new Cita(idAutoincrementable, 100 + idAutoincrementable, cliente, fecha, horaInicio, horaFin, servicio);
            System.out.println("\nValidando disponibilidad...");

            boolean registrado = gestor.registrarCita(nuevaCita);
            if (registrado) {
                idAutoincrementable++;
            }

            System.out.print("\n¿Deseas registrar otra cita? (s/n): ");
            String respuesta = scanner.nextLine();
            if (respuesta.equalsIgnoreCase("n")) {
                continuar = false;
            }
        }
        scanner.close();
        System.out.println("\nSesión finalizada. Total de citas activas: " + citasExistentes.size());
    }
}
