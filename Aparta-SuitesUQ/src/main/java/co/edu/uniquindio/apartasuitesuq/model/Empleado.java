package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Empleado del alojamiento. Es la clase base de {@link Recepcionista} y {@link Administrador}.
 * <p>
 * Orden natural (Comparable): por identificación. Permite guardar a todos los empleados
 * en un árbol binario de búsqueda ({@code ArbolBinarioBusqueda<Empleado>}) para la
 * gestión jerárquica del administrador.
 */
public abstract class Empleado implements Comparable<Empleado> {

    private String id;
    private String nombre;
    private String contrasena;

    /**
     * @param id         identificación del empleado (clave del árbol)
     * @param nombre     nombre completo
     * @param contrasena contraseña para iniciar sesión; si es vacía se usa el id
     */
    protected Empleado(String id, String nombre, String contrasena) {
        setId(id);
        setNombre(nombre);
        setContrasena(contrasena);
    }

    /** @return el rol con el que este empleado navega la aplicación */
    public abstract Rol getRol();

    /**
     * @param intento contraseña digitada en el inicio de sesión
     * @return true si coincide
     */
    public boolean verificarContrasena(String intento) {
        return contrasena.equals(intento);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("La identificación del empleado no puede estar vacía.");
        }
        this.id = id.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empleado no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    /** Cambia la contraseña; si es vacía se usa la identificación. */
    public void setContrasena(String contrasena) {
        this.contrasena = (contrasena == null || contrasena.isEmpty()) ? this.id : contrasena;
    }

    /** Orden natural por identificación. */
    @Override
    public int compareTo(Empleado otro) {
        if (otro == null) {
            return 1;
        }
        return this.id.compareTo(otro.id);
    }

    /** Dos empleados son iguales si tienen la misma identificación (coherente con compareTo). */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Empleado)) {
            return false;
        }
        return id.equals(((Empleado) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + getRol().getDescripcion() + ", " + id + ")";
    }
}
