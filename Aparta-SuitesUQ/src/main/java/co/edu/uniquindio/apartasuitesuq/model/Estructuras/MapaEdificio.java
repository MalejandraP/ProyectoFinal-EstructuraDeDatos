package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

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

    public static MapaEdificio ejemplo() {
        MapaEdificio m = new MapaEdificio();
        m.agregarNodo(new NodoMapa("REC", TipoNodo.RECEPCION, 0.1, 0.5));
        m.agregarNodo(new NodoMapa("ESC_A", TipoNodo.ESCALERA, 0.35, 0.5));
        m.agregarNodo(new NodoMapa("ESC_B", TipoNodo.ESCALERA, 0.75, 0.5));
        m.agregarNodo(new NodoMapa("ASEO", TipoNodo.ZONA_ASEO, 0.1, 0.85));
        m.agregarNodo(new NodoMapa("A101", TipoNodo.APARTAMENTO, 0.3, 0.15));
        m.agregarNodo(new NodoMapa("A102", TipoNodo.APARTAMENTO, 0.45, 0.15));
        m.agregarNodo(new NodoMapa("A103", TipoNodo.APARTAMENTO, 0.38, 0.85));
        m.agregarNodo(new NodoMapa("B101", TipoNodo.APARTAMENTO, 0.7, 0.15));
        m.agregarNodo(new NodoMapa("B102", TipoNodo.APARTAMENTO, 0.85, 0.85));
        m.agregarPasillo("REC", "ESC_A", 12);
        m.agregarPasillo("REC", "ASEO", 8);
        m.agregarPasillo("ESC_A", "A101", 10);
        m.agregarPasillo("ESC_A", "A102", 14);
        m.agregarPasillo("ESC_A", "A103", 9);
        m.agregarPasillo("ESC_A", "ESC_B", 20);
        m.agregarPasillo("ESC_B", "B101", 11);
        m.agregarPasillo("ESC_B", "B102", 13);
        return m;
    }


}
