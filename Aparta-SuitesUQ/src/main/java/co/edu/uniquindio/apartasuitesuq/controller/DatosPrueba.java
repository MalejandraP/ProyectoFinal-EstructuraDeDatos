package co.edu.uniquindio.apartasuitesuq.controller;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioReservas;

import java.time.LocalDate;


public class DatosPrueba {

    private static Huesped huesped;
    private static ListaDoble<Reserva> reservas = new ListaDoble<>();
    private static ArbolBinarioBusqueda<Apartamento> apartamentos = new ArbolBinarioBusqueda<>();
    public static ArbolBinarioBusqueda<Apartamento> getApartamentos() { return apartamentos; }
    private static final ServicioReservas servicioReservas = new ServicioReservas();
    public static ServicioReservas getServicioReservas() { return servicioReservas; }


    public static void cargar() {

        huesped = new Huesped("1126094546","Camila Torres", "+57 300 555 0142",
                "camila.torres@correo.com", LocalDate.of(1996, 3, 14));
        Apartamento a101 = new Apartamento("a101","Los Cerezos","Bloque 1", 3,2,3,400000);
        Apartamento a102 = new Apartamento("a102","Los Alpes","Bloque 2", 2,4,6,600000);

        LocalDate hoy = LocalDate.now();

        Reserva r1 = servicioReservas.crearReserva(huesped, a101,
                hoy.plusDays(30), hoy.plusDays(33), 3, Canal.DIRECTA);   // queda CONFIRMADA
        Reserva r2 = servicioReservas.crearReserva(huesped, a102,
                hoy.plusDays(60), hoy.plusDays(62), 2, Canal.PORTAL);    // queda PENDIENTE

        r1.getFolio().registrarCargo(r1.getCostoTotal(), "Estancia");
        r1.getFolio().registrarPago(r1.getCostoTotal() * 0.30, "Anticipo 30 %");

        apartamentos.insertar(a101);
        apartamentos.insertar(a102);
        huesped.agregarFavorito(a101);
        huesped.agregarAlHistorial(new Estancia(LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22)));
        huesped.agregarAlHistorial(new Estancia(LocalDate.of(2026, 6, 3),  LocalDate.of(2026, 6, 5)));
        huesped.agregarAlHistorial(new Estancia(LocalDate.of(2026, 8, 14), LocalDate.of(2026, 8, 16)));
    }

    public static Huesped getHuesped() { return huesped; }
    public static ListaDoble<Reserva> getReservas() { return reservas; }
}
