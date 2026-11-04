package co.edu.uniquindio.apartasuitesuq.model;

public class Apartamento implements Comparable<Apartamento> {
    private String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private double tarifaBaseNoche;

    public Apartamento(String codigo, String nombre, String bloque, int piso,
                       int numeroDormitorios, int capacidadMaxima, double tarifaBaseNoche) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.piso = piso;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.tarifaBaseNoche = tarifaBaseNoche;
    }

    @Override
    public int compareTo(Apartamento otro) {
        return this.codigo.compareTo(otro.codigo);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBloque() {
        return bloque;
    }

    public int getPiso() {
        return piso;
    }

    public int getNumeroDormitorios() {
        return numeroDormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public double getTarifaBaseNoche() {
        return tarifaBaseNoche;
    }
}