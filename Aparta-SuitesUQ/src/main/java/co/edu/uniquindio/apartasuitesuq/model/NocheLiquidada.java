package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;

/**
 * Una línea del desglose de la cotización: lo que se cobra por una noche concreta.
 * <p>
 * subtotal = tarifa por persona de la temporada × personas que pagan esa noche
 * (los menores de 6 años ocupan cupo pero no pagan).
 */
public class NocheLiquidada {

    private final LocalDate noche;
    private final Temporada temporada;
    private final double tarifaPorPersona;
    private final int personasQuePagan;
    private final int personasTotales;

    public NocheLiquidada(LocalDate noche, Temporada temporada, double tarifaPorPersona,
                          int personasQuePagan, int personasTotales) {
        this.noche = noche;
        this.temporada = temporada;
        this.tarifaPorPersona = tarifaPorPersona;
        this.personasQuePagan = personasQuePagan;
        this.personasTotales = personasTotales;
    }

    /** @return valor de la noche: tarifa por persona × personas que pagan */
    public double getSubtotal() {
        return tarifaPorPersona * personasQuePagan;
    }

    /** @return cuántas personas de la noche no pagan por ser menores de 6 años */
    public int getPersonasSinCobro() {
        return personasTotales - personasQuePagan;
    }

    public LocalDate getNoche() {
        return noche;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public double getTarifaPorPersona() {
        return tarifaPorPersona;
    }

    public int getPersonasQuePagan() {
        return personasQuePagan;
    }

    public int getPersonasTotales() {
        return personasTotales;
    }

    @Override
    public String toString() {
        String texto = noche + " | " + temporada.getDescripcion() + " | $" + String.format("%,.0f", tarifaPorPersona)
                + " x " + personasQuePagan + " = $" + String.format("%,.0f", getSubtotal());
        if (getPersonasSinCobro() > 0) {
            texto += " (" + getPersonasSinCobro() + " menor(es) sin cobro)";
        }
        return texto;
    }
}
