package co.edu.uniquindio.apartasuitesuq.controller;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;

import java.time.LocalDate;


public class DatosPrueba {

    private static Huesped huesped;
    private static ListaDoble<Reserva> reservas = new ListaDoble<>();

    public static void cargar() {
        huesped = new Huesped("1126094546","Camila Torres", "+57 300 555 0142",
                "camila.torres@correo.com", LocalDate.of(1996, 3, 14));
        Apartamento a101 = new Apartamento("a101","Los Cerezos","Bloque 1", 3,2,3,400000);
        Apartamento a102 = new Apartamento("a102","Los Alpes","Bloque 2", 2,4,6,600000);

        reservas.agregarFinal(new Reserva("R-0142", huesped, a101,
                LocalDate.of(2026, 12, 18), LocalDate.of(2026, 12, 21),
                EstadoReserva.CONFIRMADA, 1080000, Canal.PORTAL));
        reservas.agregarFinal(new Reserva("R-0151", huesped, a102,
                LocalDate.of(2027, 1, 4), LocalDate.of(2027, 1, 6),
                EstadoReserva.PENDIENTE, 1200000, Canal.PORTAL));
    }

    public static Huesped getHuesped() { return huesped; }
    public static ListaDoble<Reserva> getReservas() { return reservas; }
}
