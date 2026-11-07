package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reserva implements Comparable<Reserva>{

    private String codigoReserva;
    private Huesped huesped;
    private Apartamento apartamento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoReserva estado;
    private double costoTotal;
    private Canal canal;

    public Reserva(String codigoReserva, Huesped huesped,
                   Apartamento apartamento, LocalDate fechaInicio,
                   LocalDate fechaFin, EstadoReserva estado, double costoTotal, Canal canal) {
        this.codigoReserva = codigoReserva;
        this.huesped = huesped;
        this.apartamento = apartamento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.costoTotal = costoTotal;
        this.canal = canal;
    }

    public int obtenerNumeroNoches() {
        if (fechaInicio != null && fechaFin != null) {
            long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            return (int) noches;
        }
        return 0;
    }

    public double calcularCostoTotal() {
        if (this.apartamento != null && this.fechaInicio != null && this.fechaFin != null) {
            int noches = obtenerNumeroNoches();
            return this.apartamento.calcularCostoEstadia(noches);
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

    public Apartamento getApartamento() {
        return apartamento;
    }

    public void setApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("La reserva debe tener un apartamento asociado.");
        }
        this.apartamento = apartamento;
        // Calcular el costo del apartamento, si ya hay fechas establecidas
        if (this.fechaInicio != null && this.fechaFin != null) {
            this.costoTotal = calcularCostoTotal();
        }
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin no pueden ser nulas, deben exister.");
        }
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser estrictamente posterior a la fecha de inicio.");
        }
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;

        // Calcular de nuevo el costo al cambiar fechas si el apartamento ya está asignado
        if (this.apartamento != null) {
            this.costoTotal = calcularCostoTotal();
        }
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        if (estado == null ) {
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
            case ENCURSO:   return nuevo == EstadoReserva.FINALIZADA;
            default:         return false;
        }
    }
    public boolean estaActiva(){ //PROVISIONAL HASTA EL CUELLO
        return estado == EstadoReserva.PENDIENTE
                || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.ENCURSO;
    }
    @Override
    public int compareTo(Reserva auxiliar) {
        if (auxiliar == null || auxiliar.getCodigoReserva() == null) {
            return 1;
        }
        return this.codigoReserva.compareToIgnoreCase(auxiliar.getCodigoReserva());
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "codigoReserva='" + codigoReserva + '\'' +
                ", huesped=" + huesped +
                ", apartamento=" + apartamento +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", estado='" + estado + '\'' +
                ", costoTotal=" + costoTotal +
                '}';
    }
}
