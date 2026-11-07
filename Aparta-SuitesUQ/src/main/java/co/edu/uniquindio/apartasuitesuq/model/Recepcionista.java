package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Clase de empleado Recepcionista con implementación Comparable y atributos nombre y id
 */
public class Recepcionista implements Comparable<Recepcionista> {
    private String nombre, id;

    public Recepcionista(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(Recepcionista o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "Recepcionista{" +
                "nombre='" + nombre + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
