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

    public SolicitudExterna(String idExterno, Apartamento apartamento, LocalDate entrada, LocalDate salida, int numPersonas, Huesped titular) {
        this.idExterno = idExterno;
        this.apartamento = apartamento;
        this.entrada = entrada;
        this.salida = salida;
        this.numPersonas = numPersonas;
        this.titular = titular;
    }

    public void marcarAceptada() {
        this.estado = EstadoSolicitud.ACEPTADA;

    }
    public void marcarRechazada(String motivoRechazo) {
        this.estado = EstadoSolicitud.RECHAZADA;
        this.motivoRechazo = motivoRechazo;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public Apartamento getCodigoApartamento() {
        return apartamento;
    }

    public void setApartamento(Apartamento apartamento) {
        this.apartamento = apartamento;
    }

    public LocalDate getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalDate entrada) {
        this.entrada = entrada;
    }

    public LocalDate getSalida() {
        return salida;
    }

    public void setSalida(LocalDate salida) {
        this.salida = salida;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public Huesped getTitular() {
        return titular;
    }

    public void setTitular(Huesped titular) {
        this.titular = titular;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}
