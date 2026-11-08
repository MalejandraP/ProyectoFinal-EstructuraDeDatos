package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Persona que se aloja en una reserva (el titular también es un ocupante).
 * <p>
 * El alojamiento cobra por persona y por noche, pero los menores de
 * {@value #EDAD_MINIMA_COBRO} años ocupan cupo sin pagar. La edad se evalúa en cada noche,
 * así que un niño que cumple 6 años durante la estancia empieza a pagar desde ese día.
 */
public class Ocupante {

    /** Edad a partir de la cual un ocupante paga la noche. */
    public static final int EDAD_MINIMA_COBRO = 6;

    private String nombre;
    private LocalDate fechaNacimiento;

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        setNombre(nombre);
        setFechaNacimiento(fechaNacimiento);
    }

    /**
     * @param fecha día en que se calcula la edad
     * @return años cumplidos en esa fecha
     */
    public int edadEn(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula.");
        }
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /**
     * @param noche noche de la estancia
     * @return true si en esa noche tiene 6 años o más y por tanto paga
     */
    public boolean pagaEn(LocalDate noche) {
        return edadEn(noche) >= EDAD_MINIMA_COBRO;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del ocupante no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento del ocupante es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new FechasInvalidasException("La fecha de nacimiento no puede estar en el futuro.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public String toString() {
        return nombre + " (" + fechaNacimiento + ")";
    }
}
