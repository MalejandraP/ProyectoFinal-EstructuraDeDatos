package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;

import java.time.LocalDate;

public class Reserva implements Comparable<Reserva> {

    private String codigoReserva;
    private Huesped huesped;
    private Apartamento apartamento;
    private Estancia estancia;
    private EstadoReserva estado;
    private double costoTotal;
    private Canal canal;
    private ListaDoble<Huesped> huespedes;

    // Constructor principal con Estancia
    public Reserva(String codigoReserva, Huesped huesped,
                   Apartamento apartamento, Estancia estancia,
                   EstadoReserva estado, double costoTotal, Canal canal) {
        this.codigoReserva = codigoReserva;
        this.huesped = huesped;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.estado = estado;
        this.costoTotal = costoTotal;
        this.canal = canal;
        this.huespedes = new ListaDoble<>();
        if (huesped != null) {
            this.huespedes.agregarFinal(huesped);
        }
    }

    // Constructor sobrecargado por compatibilidad con fechas separadas
    public Reserva(String codigoReserva, Huesped huesped,
                   Apartamento apartamento, LocalDate fechaInicio,
                   LocalDate fechaFin, EstadoReserva estado, double costoTotal, Canal canal) {
        this(codigoReserva, huesped, apartamento, new Estancia(fechaInicio, fechaFin), estado, costoTotal, canal);
    }

    public int obtenerNumeroNoches() {
        return (estancia != null) ? estancia.getNumeroNoches() : 0;
    }

    public double calcularCostoTotal() {
        if (this.apartamento != null && this.estancia != null) {
            return this.apartamento.calcularCostoEstadia(obtenerNumeroNoches());
        }
        return 0.0;
    }

    public void cancelarReserva() {
        if (EstadoReserva.FINALIZADA.equals(estado)) {
            throw new IllegalStateException("No se puede cancelar una reserva que ya fue finalizada.");
        }
        this.estado = EstadoReserva.CANCELADA;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        if (codigoReserva == null || codigoReserva.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la reserva no puede estar vacío.");
        }
        this.codigoReserva = codigoReserva.trim();
    }

    public Huesped getHuesped() {
        return huesped;
    }

    public void setCliente(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("La reserva debe tener un huesped asociado.");
        }
        this.huesped = huesped;
    }

    public ListaDoble<Huesped> getHuespedes() {
        return huespedes;
    }

    public void agregarHuesped(Huesped acompaniante) {
        if (acompaniante != null) {
            this.huespedes.agregarFinal(acompaniante);
        }
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public void setApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("La reserva debe tener un apartamento asociado.");
        }
        this.apartamento = apartamento;
        if (this.estancia != null) {
            this.costoTotal = calcularCostoTotal();
        }
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public void setEstancia(Estancia estancia) {
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia no puede ser nula.");
        }
        this.estancia = estancia;
        if (this.apartamento != null) {
            this.costoTotal = calcularCostoTotal();
        }
    }

    public LocalDate getFechaInicio() {
        return (estancia != null) ? estancia.getEntrada() : null;
    }

    public LocalDate getFechaFin() {
        return (estancia != null) ? estancia.getSalida() : null;
    }

    public void setFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        this.estancia = new Estancia(fechaInicio, fechaFin);
        if (this.apartamento != null) {
            this.costoTotal = calcularCostoTotal();
        }
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la reserva no puede estar vacío.");
        }
        this.estado = estado;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public void cambiarEstado(EstadoReserva nuevo) {
        if (nuevo == null) {
            throw new TransicionEstadoInvalidaException("El estado no puede ser nulo.");
        }
        if (!transicionValida(this.estado, nuevo)) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede pasar de " + this.estado + " a " + nuevo);
        }
        this.estado = nuevo;
    }

    private boolean transicionValida(EstadoReserva actual, EstadoReserva nuevo) {
        switch (actual) {
            case PENDIENTE:  return nuevo == EstadoReserva.CONFIRMADA
                    || nuevo == EstadoReserva.CANCELADA;
            case CONFIRMADA: return nuevo == EstadoReserva.ENCURSO
                    || nuevo == EstadoReserva.CANCELADA
                    || nuevo == EstadoReserva.NOSHOW;
            case ENCURSO:    return nuevo == EstadoReserva.FINALIZADA;
            default:         return false;
        }
    }

    public boolean estaActiva() {
        return estado == EstadoReserva.PENDIENTE
                || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.ENCURSO;
    }

    @Override
    public int compareTo(Reserva otra) {
        if (otra == null) {
            return 1;
        }
        if (this.getFechaInicio() != null && otra.getFechaInicio() != null) {
            int cmpFecha = this.getFechaInicio().compareTo(otra.getFechaInicio());
            if (cmpFecha != 0) {
                return cmpFecha;
            }
        }
        if (this.codigoReserva == null) {
            return otra.codigoReserva == null ? 0 : -1;
        }
        return otra.codigoReserva == null ? 1 : this.codigoReserva.compareToIgnoreCase(otra.codigoReserva);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "codigoReserva='" + codigoReserva + '\'' +
                ", huesped=" + huesped +
                ", apartamento=" + apartamento +
                ", estancia=" + estancia +
                ", estado=" + estado +
                ", costoTotal=" + costoTotal +
                '}';
    }
}