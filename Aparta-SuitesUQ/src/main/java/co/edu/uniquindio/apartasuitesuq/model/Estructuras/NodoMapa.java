package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

public class NodoMapa {

    private final String codigo;
    private final TipoNodo tipo;
    private final double x;
    private final double y;

    public NodoMapa(String codigo, TipoNodo tipo, double x, double y) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
    }

    public String getCodigo() { return codigo; }
    public TipoNodo getTipo() { return tipo; }
    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public String toString() { return tipo + " " + codigo; }
}
