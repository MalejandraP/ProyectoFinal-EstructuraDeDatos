package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Estados operativos posibles de un apartamento.
 * Cada constante guarda una descripción legible para mostrar en la GUI.
 */
public enum EstadoApartamento {

    PREPARADO("Preparado"),
    OCUPADO("Ocupado"),
    PENDIENTEDEPREPARACION("Pendiente de preparación"),
    ENPREPARACION("En preparación"),
    FUERADESERVICIO("Fuera de servicio");

    private final String descripcion;

    EstadoApartamento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Solo un apartamento Preparado puede recibir un nuevo grupo (check-in).
     * @return true si el estado es PREPARADO
     */
    public boolean puedeRecibirHuespedes() {
        return this == PREPARADO;
    }
}
