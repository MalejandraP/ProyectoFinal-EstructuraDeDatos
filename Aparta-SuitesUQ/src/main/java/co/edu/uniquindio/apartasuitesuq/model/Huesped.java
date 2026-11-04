package co.edu.uniquindio.apartasuitesuq.model;

public class Huesped implements Comparable<Huesped>  {

    private String cedula;
    private String nombre;
    private String telefono;
    private String correo;

    public Huesped(String cedula, String nombre, String telefono, String correo) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new IllegalArgumentException("La cédula del huesped no puede estar vacía.");
        }
        this.cedula = cedula.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono del cliente no puede estar vacío.");
        }
        this.telefono = telefono.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo del cliente no puede estar vacío.");
        }
        String correoLimpio = correo.trim();

        if (!correoLimpio.contains("@") || !correoLimpio.contains(".")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
        this.correo = correoLimpio;
    }

    @Override
    public int compareTo(Huesped auxiliar) {
        if (auxiliar == null || auxiliar.getCedula() == null) {
            return 1;
        }
        return this.cedula.compareToIgnoreCase(auxiliar.getCedula());
    }

    @Override
    public String toString() {
        return "Huesped{" +
                "cedula='" + cedula + '\'' +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", correo='" + correo + '\'' +
                '}';
    }
}
