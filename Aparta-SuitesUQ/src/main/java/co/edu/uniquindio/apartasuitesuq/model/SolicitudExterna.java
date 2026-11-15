package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;

public class SolicitudExterna {
    private String idExterno;
    private Apartamento apartamento;
    private LocalDate entrada, salida;
    private int numPersonas;
    private Huesped titular;
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
    private String motivoRechazo;
    private Reserva reservaCreada;

    public SolicitudExterna(String idExterno, Apartamento apartamento, LocalDate entrada, LocalDate salida, int numPersonas, Huesped titular) {
        this.idExterno = idExterno;
        this.apartamento = apartamento;
        this.entrada = entrada;
        this.salida = salida;
        this.numPersonas = numPersonas;
        this.titular = titular;
    }

    public void marcarAceptada(Reserva reserva) {
        this.estado = EstadoSolicitud.ACEPTADA;
        this.reservaCreada = reserva;
    }
    public void marcarRechazada(String motivoRechazo) {
        this.estado = EstadoSolicitud.RECHAZADA;
        this.motivoRechazo = motivoRechazo;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }


    public LocalDate getEntrada() {
        return entrada;
    }


    public LocalDate getSalida() {
        return salida;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public Huesped getTitular() {
        return titular;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }


    public String getMotivoRechazo() {
        return motivoRechazo;
    }
    public Reserva getReservaCreada() { return reservaCreada; }

}
