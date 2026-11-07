package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reserva implements Comparable<Reserva>{

    private String codigoReserva;
    private Huesped huespedTitular;
    private Apartamento apartamento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private double costoTotal;
    private ListaDoble<Huesped> huespedes;

    public Reserva(String codigoReserva, Huesped huespedTitular,
                   Apartamento apartamento, LocalDate fechaInicio,
                   LocalDate fechaFin, String estado, double costoTotal) {
        this.codigoReserva = codigoReserva;
        this.huespedTitular = huespedTitular;
        this.apartamento = apartamento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.costoTotal = costoTotal;
        this.huespedes = new ListaDoble<>();
        this.huespedes.agregarFinal(huespedTitular);
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
        if ("Finalizada".equalsIgnoreCase(this.estado)) {
            throw new IllegalStateException("No se puede cancelar una reserva que ya fue finalizada.");
        }
        this.estado = "Cancelada";
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
        return huespedTitular;
    }

    public void setCliente(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("La reserva debe tener un huesped asociado.");
        }
        this.huespedTitular = huesped;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado de la reserva no puede estar vacío.");
        }
        this.estado = estado.trim();
    }

    public double getCostoTotal() {
        return costoTotal;
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
                ", huesped=" + huespedTitular +
                ", apartamento=" + apartamento +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", estado='" + estado + '\'' +
                ", costoTotal=" + costoTotal +
                '}';
    }
}
