package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Ciclo de vida de una reserva:
 * Pendiente → Confirmada → En curso → Finalizada,
 * pudiendo terminar antes como Cancelada o No-show.
 */
public enum EstadoReserva {

    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ENCURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada"),
    NOSHOW("No-show");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Una reserva activa ocupa sus noches: dos reservas activas del mismo
     * apartamento no pueden compartir ninguna noche.
     * @return true si el estado es PENDIENTE, CONFIRMADA o ENCURSO
     */
    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == ENCURSO;
    }

    /**
     * Un estado final ya no admite transiciones.
     * @return true si el estado es FINALIZADA, CANCELADA o NOSHOW
     */
    public boolean esFinal() {
        return this == FINALIZADA || this == CANCELADA || this == NOSHOW;
    }
}
