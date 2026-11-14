package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Administrador del alojamiento. Analiza el grafo del edificio, gestiona empleados,
 * apartamentos y huéspedes, define temporadas y tarifas y genera los reportes.
 */
public class Administrador extends Empleado {

    /** Crea un administrador cuya contraseña inicial es su id. */
    public Administrador(String nombre, String id) {
        this(nombre, id, null);
    }

    public Administrador(String nombre, String id, String contrasena) {
        super(id, nombre, contrasena);
    }

    @Override
    public Rol getRol() {
        return Rol.ADMINISTRADOR;
    }
}
