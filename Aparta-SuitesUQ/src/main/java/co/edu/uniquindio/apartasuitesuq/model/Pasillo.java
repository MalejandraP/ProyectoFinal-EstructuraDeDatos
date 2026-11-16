package co.edu.uniquindio.apartasuitesuq.model;

/** Pasillo del mapa del edificio: arista entre dos nodos del grafo, con su longitud y si está cerrado. */
public class Pasillo {

    private final String nodoA;
    private final String nodoB;
    private final double metros;
    private final boolean cerrado;

    public Pasillo(String nodoA, String nodoB, double metros, boolean cerrado) {
        this.nodoA = nodoA;
        this.nodoB = nodoB;
        this.metros = metros;
        this.cerrado = cerrado;
    }

    public String getNodoA() {
        return nodoA;
    }

    public String getNodoB() {
        return nodoB;
    }

    public double getMetros() {
        return metros;
    }

    public boolean isCerrado() {
        return cerrado;
    }

    @Override
    public String toString() {
        return nodoA + " — " + nodoB + " (" + Math.round(metros) + " m" + (cerrado ? ", cerrado)" : ")");
    }
}
