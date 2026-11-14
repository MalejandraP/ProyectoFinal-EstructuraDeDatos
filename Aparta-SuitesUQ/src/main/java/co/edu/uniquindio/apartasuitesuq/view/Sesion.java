package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Empleado;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Rol;

/** Usuario que inició sesión y el rol con el que navega la aplicación. */
public final class Sesion {

    private final Rol rol;
    private final Empleado empleado;
    private final Huesped huesped;

    private Sesion(Rol rol, Empleado empleado, Huesped huesped) {
        this.rol = rol;
        this.empleado = empleado;
        this.huesped = huesped;
    }

    public static Sesion deEmpleado(Empleado empleado) {
        return new Sesion(empleado.getRol(), empleado, null);
    }

    public static Sesion deHuesped(Huesped huesped) {
        return new Sesion(Rol.HUESPED, null, huesped);
    }

    public Rol getRol() {
        return rol;
    }

    /** @return el empleado, o null si es un huésped */
    public Empleado getEmpleado() {
        return empleado;
    }

    /** @return el huésped, o null si es un empleado */
    public Huesped getHuesped() {
        return huesped;
    }

    public String getNombre() {
        return empleado != null ? empleado.getNombre() : huesped.getNombre();
    }
}
