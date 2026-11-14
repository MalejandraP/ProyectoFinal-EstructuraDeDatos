package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Roles del sistema. La ventana principal muestra un menú distinto según el rol
 * de quien inicia sesión.
 */
public enum Rol {

    HUESPED("Huésped"),
    RECEPCIONISTA("Recepcionista"),
    ADMINISTRADOR("Administrador");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
