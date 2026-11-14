package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;

import java.time.LocalDate;

/**
 * Calendario de temporadas que define el administrador.
 * <p>
 * Guarda los periodos Media y Alta en una {@link ListaDoble} ordenada por fecha de inicio
 * (estructura propia, sin java.util). Los periodos no pueden solaparse. Toda fecha que no
 * caiga en ningún periodo pertenece a la temporada base (BAJA).
 * <p>
 * Con el calendario y las tarifas de cada apartamento se sabe cuánto cuesta cada noche:
 * {@link #tarifaPorPersona(Apartamento, LocalDate)}.
 */
public class CalendarioTemporadas {

    private ListaDoble<PeriodoTemporada> periodos;

    public CalendarioTemporadas() {
        this.periodos = new ListaDoble<>();
    }

    /**
     * Agrega un periodo manteniendo la lista ordenada por fecha de inicio.
     * @throws FechasInvalidasException si se solapa con un periodo ya definido
     */
    public void agregarPeriodo(PeriodoTemporada nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("El periodo no puede ser nulo.");
        }
        for (PeriodoTemporada existente : periodos) {
            if (existente.seSolapaCon(nuevo)) {
                throw new FechasInvalidasException("El periodo " + nuevo
                        + " se solapa con el periodo ya definido " + existente + ".");
            }
        }
        ListaDoble<PeriodoTemporada> ordenada = new ListaDoble<>();
        boolean insertado = false;
        for (PeriodoTemporada existente : periodos) {
            if (!insertado && nuevo.compareTo(existente) < 0) {
                ordenada.agregarFinal(nuevo);
                insertado = true;
            }
            ordenada.agregarFinal(existente);
        }
        if (!insertado) {
            ordenada.agregarFinal(nuevo);
        }
        periodos = ordenada;
    }

    /** Atajo para agregar un periodo a partir de sus datos. */
    public void agregarPeriodo(Temporada temporada, LocalDate inicio, LocalDate fin) {
        agregarPeriodo(new PeriodoTemporada(temporada, inicio, fin));
    }

    /**
     * Elimina un periodo del calendario.
     * @return true si existía y se eliminó
     */
    public boolean eliminarPeriodo(PeriodoTemporada periodo) {
        ListaDoble<PeriodoTemporada> restantes = new ListaDoble<>();
        boolean eliminado = false;
        for (PeriodoTemporada existente : periodos) {
            if (!eliminado && existente.equals(periodo)) {
                eliminado = true;
            } else {
                restantes.agregarFinal(existente);
            }
        }
        periodos = restantes;
        return eliminado;
    }

    /** Borra todos los periodos: todo el año queda en temporada BAJA. */
    public void limpiar() {
        periodos = new ListaDoble<>();
    }

    /**
     * @param fecha noche a consultar
     * @return la temporada del periodo que contiene la fecha, o BAJA si ninguno la contiene
     */
    public Temporada temporadaDe(LocalDate fecha) {
        for (PeriodoTemporada periodo : periodos) {
            if (periodo.contiene(fecha)) {
                return periodo.getTemporada();
            }
            if (periodo.getInicio().isAfter(fecha)) {
                break; // la lista está ordenada: los siguientes empiezan aún más tarde
            }
        }
        return Temporada.base();
    }

    /**
     * @return tarifa por persona que cobra el apartamento en esa noche según su temporada
     */
    public double tarifaPorPersona(Apartamento apartamento, LocalDate noche) {
        return apartamento.getTarifa(temporadaDe(noche));
    }

    /** @return los periodos definidos, ordenados por fecha de inicio */
    public ListaDoble<PeriodoTemporada> getPeriodos() {
        return periodos;
    }

    public boolean estaVacio() {
        return periodos.estaVacia();
    }
}
