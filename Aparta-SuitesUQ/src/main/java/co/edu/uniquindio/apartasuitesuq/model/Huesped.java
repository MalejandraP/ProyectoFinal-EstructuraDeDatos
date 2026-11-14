package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Conjunto;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;

import java.time.LocalDate;
import java.time.Period;

/**
 * Perfil de un huésped: nombre, documento, fecha de nacimiento, teléfono, correo y,
 * de manera opcional, una fotografía (ruta del archivo).
 * <p>
 * Orden natural (Comparable): por documento. Es lo que permite guardarlo en el
 * árbol binario de búsqueda de huéspedes.
 */
public class Huesped implements Comparable<Huesped> {

    private String cedula;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String rutaFoto;
    private final Conjunto<Apartamento> favoritos = new Conjunto<>();
    private final ListaDoble<Reserva> historial = new ListaDoble<>();

    /**
     * Constructor sin fecha de nacimiento ni foto.
     * Se conserva por compatibilidad con el código existente.
     */
    public Huesped(String cedula, String nombre, String telefono, String correo) {
        this(cedula, nombre, null, telefono, correo, null);
    }

    /** Constructor con fecha de nacimiento y sin foto (lo usan las pantallas del equipo). */
    public Huesped(String cedula, String nombre, String telefono, String correo, LocalDate fechaNacimiento) {
        this(cedula, nombre, fechaNacimiento, telefono, correo, null);
    }

    /**
     * Crea el perfil completo del huésped.
     *
     * @param cedula          documento de identidad (clave del árbol)
     * @param fechaNacimiento fecha de nacimiento; puede ser null solo por compatibilidad
     * @param rutaFoto        ruta de la fotografía; opcional (null o vacío = sin foto)
     */
    public Huesped(String cedula, String nombre, LocalDate fechaNacimiento,
                   String telefono, String correo, String rutaFoto) {
        setCedula(cedula);
        setNombre(nombre);
        setFechaNacimiento(fechaNacimiento);
        setTelefono(telefono);
        setCorreo(correo);
        setRutaFoto(rutaFoto);
    }

    /**
     * Convierte al huésped en ocupante para registrarlo en una reserva como titular.
     * @throws IllegalStateException si el huésped no tiene fecha de nacimiento
     */
    public Ocupante comoOcupante() {
        if (fechaNacimiento == null) {
            throw new IllegalStateException(
                    "El huésped " + nombre + " no tiene fecha de nacimiento registrada.");
        }
        return new Ocupante(nombre, fechaNacimiento);
    }

    /** @return edad actual, o -1 si no tiene fecha de nacimiento */
    public int getEdad() {
        if (fechaNacimiento == null) {
            return -1;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public boolean tieneFoto() {
        return rutaFoto != null;
    }

    // -------------------------------------------------- getters y setters

    public String getCedula() {
        return cedula;
    }

    /** Alias de {@link #getCedula()} con el nombre que usa el enunciado. */
    public String getDocumento() {
        return cedula;
    }

    public void setCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del huésped no puede estar vacío.");
        }
        this.cedula = cedula.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del huésped no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
            throw new FechasInvalidasException("La fecha de nacimiento no puede estar en el futuro.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono del huésped no puede estar vacío.");
        }
        this.telefono = telefono.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo del huésped no puede estar vacío.");
        }
        String correoLimpio = correo.trim();
        if (!correoLimpio.contains("@") || !correoLimpio.contains(".")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
        this.correo = correoLimpio;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    /** La foto es opcional: null o vacío significa que no tiene. */
    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = (rutaFoto == null || rutaFoto.trim().isEmpty()) ? null : rutaFoto.trim();
    }

    // ------------------------------------------------- favoritos e historial

    /** @return true si se agregó; false si ya era favorito (el Conjunto no admite repetidos) */
    public boolean agregarFavorito(Apartamento apartamento) {
        return favoritos.agregar(apartamento);
    }

    public void quitarFavorito(Apartamento apartamento) {
        favoritos.eliminar(apartamento);
    }

    public boolean esFavorito(Apartamento apartamento) {
        return favoritos.buscar(apartamento);
    }

    public Conjunto<Apartamento> getFavoritos() {
        return favoritos;
    }

    /** Agrega una estancia al historial del huésped. */
    public void agregarAlHistorial(Reserva reserva) {
        if (reserva == null || reserva.getEstado() != EstadoReserva.FINALIZADA){
            return;
        }
        for(Reserva r : historial){
            if(r.equals(reserva)){
                return;
            }
        }
        historial.agregarFinal(reserva);
    }

    public ListaDoble<Reserva> getHistorial() {
        return historial;
    }

    // ------------------------------------------- Comparable, equals, hashCode

    /** Orden natural por documento. */
    @Override
    public int compareTo(Huesped otro) {
        if (otro == null || otro.getCedula() == null) {
            return 1;
        }
        return this.cedula.compareToIgnoreCase(otro.getCedula());
    }

    /** Dos huéspedes son iguales si tienen el mismo documento (coherente con compareTo). */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Huesped)) {
            return false;
        }
        return cedula.equalsIgnoreCase(((Huesped) o).cedula);
    }

    @Override
    public int hashCode() {
        return cedula.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + cedula + ")";
    }
}
