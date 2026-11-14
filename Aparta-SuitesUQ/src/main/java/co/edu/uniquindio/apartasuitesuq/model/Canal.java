package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Canales por los que llega una reserva. Todos compiten por los mismos apartamentos.
 */
public enum Canal {

    /** El huésped reserva desde la aplicación; queda Pendiente hasta pagar el anticipo del 30 %. */
    PORTAL("Portal"),
    /** La recepción la registra por teléfono o de forma presencial. */
    DIRECTA("Directa"),
    /** Plataforma de terceros (simulada); sus solicitudes llegan a una cola FIFO. */
    EXTERNO("Externo");

    private final String descripcion;

    Canal(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
