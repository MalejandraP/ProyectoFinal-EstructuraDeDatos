package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

public class AnalisisGrafo {
    private final MapaEdificio mapa;

    public AnalisisGrafo(MapaEdificio mapa) {
        this.mapa = mapa;
    }


    public Recorrido<String> inalcanzablesDesdeRecepcion() {
        return mapa.getGrafo().inalcanzables(mapa.getRecepcion());
    }

    /** Solo los apartamentos que quedaron aislados. */
    public Recorrido<String> apartamentosAislados() {
        Recorrido<String> r = new Recorrido<>();
        for (String codigo : inalcanzablesDesdeRecepcion()) {
            if (mapa.buscar(codigo).getTipo() == TipoNodo.APARTAMENTO) {
                r.agregar(codigo);
            }
        }
        return r;
    }


    public int componentesConexas() {
        Grafo<String> g = mapa.getGrafo();
        int total = g.tamano();
        boolean[] visto = new boolean[total];
        String[] codigos = new String[total];
        int i = 0;
        for (NodoMapa n : mapa) {
            codigos[i++] = n.getCodigo();
        }
        int componentes = 0;
        for (int k = 0; k < total; k++) {
            if (!visto[k]) {
                componentes++;
                for (String c : g.bfs(codigos[k])) {
                    visto[indice(codigos, c)] = true;
                }
            }
        }
        return componentes;
    }

    private int indice(String[] codigos, String c) {
        for (int i = 0; i < codigos.length; i++) {
            if (codigos[i].equals(c)) return i;
        }
        return -1;
    }


    public Recorrido<String> rutaDesdeRecepcion(String destino) {
        return mapa.getGrafo().dijkstra(mapa.getRecepcion(), destino);
    }


    public double distanciaDesdeRecepcion(String destino) {
        Recorrido<String> r = rutaDesdeRecepcion(destino);
        return r == null ? -1 : r.getDistancia();
    }


    public int[] contarPasillos() {
        int[] c = new int[2];
        mapa.getGrafo().recorrerPasillos((a, b, peso, cerrado) -> {
            c[1]++;
            if (cerrado) c[0]++;
        });
        return c;
    }
}
