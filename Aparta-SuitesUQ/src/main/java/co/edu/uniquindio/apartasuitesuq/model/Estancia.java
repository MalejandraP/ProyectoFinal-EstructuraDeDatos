package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Estancia expresada con una fecha de entrada y una de salida: el intervalo [entrada, salida).
 * La noche de salida no se ocupa ni se cobra, así que del 10 al 12 de marzo son dos noches
 * (la del 10 y la del 11).
 * <p>
 * Es iterable: {@code for (LocalDate noche : estancia)} recorre una a una las noches a cobrar.
 * Así se arma la cotización con desglose noche por noche.
 */
public class Estancia implements Iterable<LocalDate> {

    private final LocalDate entrada;
    private final LocalDate salida;

    /**
     * @param entrada fecha de llegada (primera noche)
     * @param salida  fecha de salida (esa noche no cuenta)
     * @throws FechasInvalidasException si alguna fecha es nula o la salida no es posterior a la entrada
     */
    public Estancia(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new FechasInvalidasException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!salida.isAfter(entrada)) {
            throw new FechasInvalidasException(
                    "La fecha de salida debe ser posterior a la de entrada (mínimo una noche).");
        }
        this.entrada = entrada;
        this.salida = salida;
    }

    /** @return número de noches a cobrar (salida - entrada) */
    public int getNumeroNoches() {
        return (int) ChronoUnit.DAYS.between(entrada, salida);
    }

    /** @return true si la noche indicada pertenece a la estancia (la de salida no) */
    public boolean contiene(LocalDate noche) {
        return noche != null && !noche.isBefore(entrada) && noche.isBefore(salida);
    }

    /** @return true si las dos estancias comparten al menos una noche */
    public boolean seSolapaCon(Estancia otra) {
        return this.entrada.isBefore(otra.salida) && otra.entrada.isBefore(this.salida);
    }

    public LocalDate getEntrada() {
        return entrada;
    }

    public LocalDate getSalida() {
        return salida;
    }

    /** @return iterador que entrega las noches desde la entrada hasta la víspera de la salida */
    @Override
    public Iterator<LocalDate> iterator() {
        return new IteradorNoches();
    }

    /**
     * Iterador propio de la estancia. Empieza en la fecha de entrada y avanza un día
     * por cada llamada a next(), deteniéndose antes de la fecha de salida.
     */
    private class IteradorNoches implements Iterator<LocalDate> {

        private LocalDate actual = entrada;

        @Override
        public boolean hasNext() {
            return actual.isBefore(salida);
        }

        @Override
        public LocalDate next() {
            if (!hasNext()) {
                throw new FechasInvalidasException("No hay más noches en la estancia.");
            }
            LocalDate noche = actual;
            actual = actual.plusDays(1);
            return noche;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Estancia)) {
            return false;
        }
        Estancia otra = (Estancia) o;
        return entrada.equals(otra.entrada) && salida.equals(otra.salida);
    }

    @Override
    public int hashCode() {
        return 31 * entrada.hashCode() + salida.hashCode();
    }

    @Override
    public String toString() {
        int noches = getNumeroNoches();
        return entrada + " → " + salida + " (" + noches + (noches == 1 ? " noche)" : " noches)");
    }
}
