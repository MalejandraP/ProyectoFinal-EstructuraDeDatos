package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.ListaDoble;

public class MapaEdificio implements Iterable<NodoMapa> {

    private final Grafo<String> grafo = new Grafo<>();
    private final ListaDoble<NodoMapa> nodos = new ListaDoble<>();
    private String recepcion;

    public void agregarNodo(NodoMapa nodo) {
        if (buscar(nodo.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe el nodo " + nodo.getCodigo());
        }
        if (nodo.getTipo() == TipoNodo.RECEPCION) {
            if (recepcion != null) {
                throw new IllegalArgumentException("Solo puede haber una recepción");
            }
            recepcion = nodo.getCodigo();
        }
        grafo.agregarNodo(nodo.getCodigo());
        nodos.agregarFinal(nodo);
    }

    public void agregarPasillo(String a, String b, double metros) {
        if (buscar(a) == null || buscar(b) == null) {
            throw new IllegalArgumentException("Pasillo con nodo desconocido: " + a + " - " + b);
        }
        if (metros <= 0) {
            throw new IllegalArgumentException("La longitud del pasillo debe ser positiva");
        }
        grafo.agregarPasillo(a, b, metros);
    }

    public NodoMapa buscar(String codigo) {
        for (NodoMapa n : nodos) {
            if (n.getCodigo().equalsIgnoreCase(codigo)) {
                return n;
            }
        }
        return null;
    }

    public Grafo<String> getGrafo() {
        return grafo;
    }


    public String getRecepcion() {
        return recepcion;
    }

    @Override
    public java.util.Iterator<NodoMapa> iterator() {
        return nodos.iterator();
    }


}
