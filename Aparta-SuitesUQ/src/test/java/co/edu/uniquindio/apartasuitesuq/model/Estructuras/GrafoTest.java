package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrafoTest {

    //   A --1-- B --1-- C
    //   |_______5_______|        D queda aislado
    private Grafo<String> g;

    @BeforeEach
    void preparar() {
        g = new Grafo<>();
        for (String s : new String[]{"A", "B", "C", "D"}) g.agregarNodo(s);
        g.agregarPasillo("A", "B", 1);
        g.agregarPasillo("B", "C", 1);
        g.agregarPasillo("A", "C", 5);
    }

    @Test
    void dijkstraPrefiereElCaminoMasCorto() {
        Recorrido<String> r = g.dijkstra("A", "C");
        assertEquals(2.0, r.getDistancia(), 0.001);
        assertEquals("A", r.obtener(0));
        assertEquals("B", r.obtener(1));
        assertEquals("C", r.obtener(2));
    }

    @Test
    void pasilloCerradoCambiaLaRutaYAbrirlaLaRestaura() {
        g.cerrarPasillo("A", "B");
        assertTrue(g.pasilloCerrado("A", "B"));
        assertEquals(5.0, g.dijkstra("A", "C").getDistancia(), 0.001);
        g.abrirPasillo("A", "B");
        assertEquals(2.0, g.dijkstra("A", "C").getDistancia(), 0.001);
    }

    @Test
    void sinCaminoDevuelveNull() {
        assertNull(g.dijkstra("A", "D"));
    }

    @Test
    void bfsYDfsVisitanLosMismosNodosAlcanzables() {
        assertEquals(3, g.bfs("A").tamano());
        assertEquals(3, g.dfs("A").tamano());
        assertEquals("A", g.bfs("A").obtener(0));
        assertEquals("A", g.dfs("A").obtener(0));
    }

    @Test
    void inalcanzablesListaElNodoAislado() {
        Recorrido<String> r = g.inalcanzables("A");
        assertEquals(1, r.tamano());
        assertEquals("D", r.obtener(0));
    }

    @Test
    void rutaEnOrdenSumaLosTramos() {
        ListaDestinos d = new ListaDestinos("B", "C");
        Recorrido<String> r = g.rutaEnOrden("A", d);
        assertEquals(2.0, r.getDistancia(), 0.001);
        assertEquals(3, r.tamano());
    }

    @Test
    void contieneYTamano() {
        assertEquals(4, g.tamano());
        assertTrue(g.contiene("D"));
        assertFalse(g.contiene("Z"));
    }


    private static class ListaDestinos implements Iterable<String> {
        private final String[] datos;
        ListaDestinos(String... datos) { this.datos = datos; }
        public java.util.Iterator<String> iterator() {
            return new java.util.Iterator<String>() {
                int i = 0;
                public boolean hasNext() { return i < datos.length; }
                public String next() { return datos[i++]; }
            };
        }
    }

}