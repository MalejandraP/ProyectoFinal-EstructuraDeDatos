package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;

import java.time.LocalDate;

/**
 * Rango de fechas que pertenece a una temporada, por ejemplo
 * "Alta del 15 de diciembre al 15 de enero". Ambos extremos son inclusivos:
 * así lo define el administrador en el calendario.
 * <p>
 * Orden natural (Comparable): por fecha de inicio.
 */
public class PeriodoTemporada implements Comparable<PeriodoTemporada> {

    private final Temporada temporada;
    private final LocalDate inicio;
    private final LocalDate fin;

    /**
     * @param temporada temporada que aplica en el rango
     * @param inicio    primer día del periodo (inclusive)
     * @param fin       último día del periodo (inclusive)
     * @throws FechasInvalidasException si faltan fechas o el fin es anterior al inicio
     */
    public PeriodoTemporada(Temporada temporada, LocalDate inicio, LocalDate fin) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada del periodo es obligatoria.");
        }
        if (inicio == null || fin == null) {
            throw new FechasInvalidasException("Las fechas del periodo son obligatorias.");
        }
        if (fin.isBefore(inicio)) {
            throw new FechasInvalidasException("El fin del periodo no puede ser anterior a su inicio.");
        }
        this.temporada = temporada;
        this.inicio = inicio;
        this.fin = fin;
    }

    /** @return true si la fecha cae dentro del periodo (extremos incluidos) */
    public boolean contiene(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    /** @return true si los dos periodos comparten al menos un día */
    public boolean seSolapaCon(PeriodoTemporada otro) {
        return !this.inicio.isAfter(otro.fin) && !otro.inicio.isAfter(this.fin);
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    @Override
    public int compareTo(PeriodoTemporada otro) {
        return this.inicio.compareTo(otro.inicio);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PeriodoTemporada)) {
            return false;
        }
        PeriodoTemporada otro = (PeriodoTemporada) o;
        return temporada == otro.temporada && inicio.equals(otro.inicio) && fin.equals(otro.fin);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * temporada.hashCode() + inicio.hashCode()) + fin.hashCode();
    }

    @Override
    public String toString() {
        return temporada.getDescripcion() + ": " + inicio + " a " + fin;
    }
}
