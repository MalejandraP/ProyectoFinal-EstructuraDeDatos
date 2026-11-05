package co.edu.uniquindio.apartasuitesuq.model;

import java.util.ArrayList;
import java.util.List;

public class Complejo {
    private String nombre;
    private List<Apartamento> listaApartamentos;
    private List<Huesped> listaHuespedes;
    private List<Reserva> listaReservas;

    public Complejo(String nombre) {
        this.nombre = nombre;
        this.listaApartamentos = new ArrayList<>();
        this.listaHuespedes = new ArrayList<>();
        this.listaReservas = new ArrayList<>();
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Apartamento> getListaApartamentos() {
        return listaApartamentos;
    }

    public List<Huesped> getListaHuespedes() {
        return listaHuespedes;
    }

    public List<Reserva> getListaReservas() {
        return listaReservas;
    }
}
