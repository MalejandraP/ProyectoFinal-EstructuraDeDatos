package co.edu.uniquindio.apartasuitesuq.model;


/**
 * Empleado de recepción. Registra reservas directas, procesa el canal externo,
 * maneja folios, hace check-in/check-out y rota en los turnos (lista circular).
 * <p>
 * Hereda de {@link Empleado} el id, el nombre, la contraseña y el orden natural por id.
 */
public class Recepcionista extends Empleado {

    /**
     * Crea un recepcionista cuya contraseña inicial es su id.
     * Se conserva el orden (nombre, id) del constructor original.
     */
    public Recepcionista(String nombre, String id) {
        this(nombre, id, null);
    }

    public Recepcionista(String nombre, String id, String contrasena) {
        super(id, nombre, contrasena);
    }

    @Override
    public Rol getRol() {
        return Rol.RECEPCIONISTA;
    }
}
