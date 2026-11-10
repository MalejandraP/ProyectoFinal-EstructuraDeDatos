package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class Grafo <T> implements Iterable<T> {
    private static class Arista {
        final int destino;
        double peso;
        boolean cerrada;
        Arista sig;

        Arista(int destino, double peso, Arista sig) {
            this.destino = destino;
            this.peso = peso;
            this.sig = sig;
        }
    }

    @FunctionalInterface
    public interface VisitantePasillo<N> {
        void visitar(N a, N b, double peso, boolean cerrado);
    }

    private Object[] nodos = new Object[16];
    private Arista[] ady = new Arista[16];
    private int n;

    //

    public void agregarNodo(T nodo) {
        if (nodo == null) throw new IllegalArgumentException("Nodo null");
        if (indiceDe(nodo) >= 0) return;
        if (n == nodos.length) crecer();
        nodos[n] = nodo;
        ady[n] = null;
        n++;
    }

    public void agregarPasillo(T a, T b, double peso) {
        if (peso <= 0) throw new IllegalArgumentException("El peso debe ser positivo");
        int i = idx(a), j = idx(b);
        Arista existente = buscarArista(i, j);
        if (existente != null) {
            existente.peso = peso;
            buscarArista(j, i).peso = peso;
            return;
        }
        ady[i] = new Arista(j, peso, ady[i]);
        ady[j] = new Arista(i, peso, ady[j]);
    }

    public void cerrarPasillo(T a, T b) { cambiarEstado(a, b, true); }

    public void abrirPasillo(T a, T b) { cambiarEstado(a, b, false); }

    public boolean pasilloCerrado(T a, T b) {
        Arista e = buscarArista(idx(a), idx(b));
        if (e == null) throw new IllegalArgumentException("No existe ese pasillo");
        return e.cerrada;
    }

    public int tamano() { return n; }

    public boolean contiene(T nodo) { return indiceDe(nodo) >= 0; }

    //

    public Recorrido<T> dijkstra(T origen, T destino) {
        int s = idx(origen), t = idx(destino);
        double[] dist = new double[n];
        int[] prev = new int[n];
        calcular(s, dist, prev);
        if (dist[t] == Double.POSITIVE_INFINITY) return null;

        int cantidad = 0;
        for (int v = t; v != -1; v = prev[v]) cantidad++;
        int[] orden = new int[cantidad];
        int k = cantidad;
        for (int v = t; v != -1; v = prev[v]) orden[--k] = v;

        Recorrido<T> ruta = new Recorrido<>();
        for (int v : orden) ruta.agregar(nodo(v));
        ruta.setDistancia(dist[t]);
        return ruta;
    }


    public Recorrido<T> rutaEnOrden(T inicio, Iterable<T> destinos) {
        idx(inicio);
        Recorrido<T> ruta = new Recorrido<>();
        ruta.agregar(inicio);
        T actual = inicio;
        double total = 0;
        for (T destino : destinos) {
            Recorrido<T> tramo = dijkstra(actual, destino);
            if (tramo == null) continue;
            for (int i = 1; i < tramo.tamano(); i++) ruta.agregar(tramo.obtener(i));
            total += tramo.getDistancia();
            actual = destino;
        }
        ruta.setDistancia(total);
        return ruta;
    }

    private void calcular(int s, double[] dist, int[] prev) {
        for (int i = 0; i < n; i++) {
            dist[i] = Double.POSITIVE_INFINITY;
            prev[i] = -1;
        }
        dist[s] = 0;

        ColaPrioridad<double[]> cola = new ColaPrioridad<>((a, b) -> Double.compare(a[0], b[0]));
        cola.encolar(new double[]{0, s});
        while (!cola.estaVacia()) {
            double[] actual = cola.desencolar();
            int u = (int) actual[1];
            if (actual[0] > dist[u]) continue; // entrada vieja
            for (Arista e = ady[u]; e != null; e = e.sig) {
                if (e.cerrada) continue;
                double nueva = dist[u] + e.peso;
                if (nueva < dist[e.destino]) {
                    dist[e.destino] = nueva;
                    prev[e.destino] = u;
                    cola.encolar(new double[]{nueva, e.destino});
                }
            }
        }
    }

    //

    public Recorrido<T> bfs(T origen) { return bfs(origen, null); }

    public Recorrido<T> dfs(T origen) { return dfs(origen, null); }


    public Recorrido<T> bfs(T origen, Predicate<T> filtro) {
        int s = idx(origen);
        boolean[] visitado = new boolean[n];
        int[] cola = new int[n];
        int ini = 0, fin = 0;
        Recorrido<T> resultado = new Recorrido<>();
        visitado[s] = true;
        cola[fin++] = s;
        while (ini < fin) {
            int u = cola[ini++];
            resultado.agregar(nodo(u));
            for (Arista e = ady[u]; e != null; e = e.sig) {
                if (e.cerrada || visitado[e.destino]) continue;
                if (filtro != null && !filtro.test(nodo(e.destino))) continue;
                visitado[e.destino] = true;
                cola[fin++] = e.destino;
            }
        }
        return resultado;
    }

    public Recorrido<T> dfs(T origen, Predicate<T> filtro) {
        int s = idx(origen);
        boolean[] visitado = new boolean[n];
        Recorrido<T> resultado = new Recorrido<>();
        dfsRec(s, visitado, resultado, filtro);
        return resultado;
    }

    private void dfsRec(int u, boolean[] visitado, Recorrido<T> resultado, Predicate<T> filtro) {
        visitado[u] = true;
        resultado.agregar(nodo(u));
        for (Arista e = ady[u]; e != null; e = e.sig) {
            if (e.cerrada || visitado[e.destino]) continue;
            if (filtro != null && !filtro.test(nodo(e.destino))) continue;
            dfsRec(e.destino, visitado, resultado, filtro);
        }
    }


    public Recorrido<T> inalcanzables(T origen) {
        boolean[] alcanzado = new boolean[n];
        for (T x : bfs(origen)) alcanzado[indiceDe(x)] = true;
        Recorrido<T> resultado = new Recorrido<>();
        for (int i = 0; i < n; i++) {
            if (!alcanzado[i]) resultado.agregar(nodo(i));
        }
        return resultado;
    }

    //

    public void recorrerPasillos(VisitantePasillo<T> visitante) {
        for (int i = 0; i < n; i++) {
            for (Arista e = ady[i]; e != null; e = e.sig) {
                if (i < e.destino) visitante.visitar(nodo(i), nodo(e.destino), e.peso, e.cerrada);
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i = 0;

            @Override
            public boolean hasNext() { return i < n; }

            @Override
            public T next() {
                if (i >= n) throw new NoSuchElementException();
                return nodo(i++);
            }
        };
    }

    //

    private void crecer() {
        Object[] nuevosNodos = new Object[n * 2];
        Arista[] nuevaAdy = new Arista[n * 2];
        System.arraycopy(nodos, 0, nuevosNodos, 0, n);
        System.arraycopy(ady, 0, nuevaAdy, 0, n);
        nodos = nuevosNodos;
        ady = nuevaAdy;
    }

    private void cambiarEstado(T a, T b, boolean cerrada) {
        int i = idx(a), j = idx(b);
        Arista ab = buscarArista(i, j), ba = buscarArista(j, i);
        if (ab == null || ba == null) throw new IllegalArgumentException("No existe ese pasillo");
        ab.cerrada = cerrada;
        ba.cerrada = cerrada;
    }

    private Arista buscarArista(int i, int j) {
        for (Arista e = ady[i]; e != null; e = e.sig) {
            if (e.destino == j) return e;
        }
        return null;
    }

    private int indiceDe(T nodo) {
        for (int i = 0; i < n; i++) {
            if (nodos[i].equals(nodo)) return i;
        }
        return -1;
    }

    private int idx(T nodo) {
        int i = indiceDe(nodo);
        if (i < 0) throw new IllegalArgumentException("El nodo no existe en el grafo: " + nodo);
        return i;
    }

    @SuppressWarnings("unchecked")
    private T nodo(int i) { return (T) nodos[i]; }

}
