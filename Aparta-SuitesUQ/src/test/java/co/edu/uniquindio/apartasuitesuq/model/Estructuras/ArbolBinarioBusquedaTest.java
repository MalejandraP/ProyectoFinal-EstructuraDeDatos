package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la estructura propia {@link ArbolBinarioBusqueda}.
 */
class ArbolBinarioBusquedaTest {

    private ArbolBinarioBusqueda<Integer> arbol;

    @BeforeEach
    void setUp() {
        arbol = new ArbolBinarioBusqueda<>();
    }

    @Test
    void arbolNuevoEstaVacioYTieneAlturaCero() {
        assertTrue(arbol.estaVacio());
        assertEquals(0, arbol.getTamano());
        assertEquals(0, arbol.obtenerAltura());
        assertNull(arbol.buscar(10));
        assertFalse(arbol.eliminar(10));
    }

    @Test
    void insertarYBuscarIgnoraDuplicadosYNulos() {
        arbol.insertar(50);
        arbol.insertar(30);
        arbol.insertar(70);
        arbol.insertar(30); // duplicado
        arbol.insertar(null); // nulo

        assertFalse(arbol.estaVacio());
        assertEquals(3, arbol.getTamano());
        assertEquals(2, arbol.obtenerAltura());
        assertEquals(30, arbol.buscar(30));
        assertTrue(arbol.contiene(70));
        assertFalse(arbol.contiene(99));
    }

    @Test
    void recorridosInordenPreordenYPostordenSonCorrectos() {
        // Árbol:
        //       40
        //      /  \
        //    20    60
        //   /  \   / \
        //  10  30 50 70
        for (int valor : new int[]{40, 20, 60, 10, 30, 50, 70}) {
            arbol.insertar(valor);
        }

        List<Integer> inorden = new ArrayList<>();
        arbol.recorridoInorden(inorden::add);
        assertEquals(List.of(10, 20, 30, 40, 50, 60, 70), inorden);

        List<Integer> preorden = new ArrayList<>();
        arbol.recorridoPreorden(preorden::add);
        assertEquals(List.of(40, 20, 10, 30, 60, 50, 70), preorden);

        List<Integer> postorden = new ArrayList<>();
        arbol.recorridoPostorden(postorden::add);
        assertEquals(List.of(10, 30, 20, 50, 70, 60, 40), postorden);
    }

    @Test
    void iteradorForEachRecorreEnOrdenYLanzaExcepcionAlAgotarse() {
        arbol.insertar(25);
        arbol.insertar(15);
        arbol.insertar(35);
        arbol.insertar(10);

        List<Integer> recorridos = new ArrayList<>();
        for (Integer n : arbol) {
            recorridos.add(n);
        }
        assertEquals(List.of(10, 15, 25, 35), recorridos);

        Iterator<Integer> it = arbol.iterator();
        while (it.hasNext()) {
            it.next();
        }
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void eliminarHojaNodoConUnHijoYNodoConDosHijosMantieneOrdenBST() {
        for (int valor : new int[]{50, 30, 70, 20, 40, 60, 80, 35}) {
            arbol.insertar(valor);
        }
        assertEquals(8, arbol.getTamano());

        // 1. Eliminar hoja (20)
        assertTrue(arbol.eliminar(20));
        assertFalse(arbol.contiene(20));
        assertEquals(7, arbol.getTamano());

        // 2. Eliminar nodo con un solo hijo (40 -> tiene hijo izquierdo 35)
        assertTrue(arbol.eliminar(40));
        assertFalse(arbol.contiene(40));
        assertTrue(arbol.contiene(35));
        assertEquals(6, arbol.getTamano());

        // 3. Eliminar raíz con dos hijos (50)
        assertTrue(arbol.eliminar(50));
        assertFalse(arbol.contiene(50));
        assertEquals(5, arbol.getTamano());

        // 4. Eliminar inexistente devuelve false
        assertFalse(arbol.eliminar(999));

        List<Integer> restantes = new ArrayList<>();
        arbol.recorridoInorden(restantes::add);
        assertEquals(List.of(30, 35, 60, 70, 80), restantes);
    }

    @Test
    void existeConPodaEncuentraElementosDescartandoRamasInnecesarias() {
        for (int valor : new int[]{50, 20, 80, 10, 30, 70, 90}) {
            arbol.insertar(valor);
        }

        // Buscar valor 70 podando izquierda cuando actual > 70 y derecha cuando actual < 70
        boolean existe70 = arbol.existeConPoda(
                actual -> actual == 70,
                actual -> 70 < actual,
                actual -> 70 > actual
        );
        assertTrue(existe70);

        boolean existe65 = arbol.existeConPoda(
                actual -> actual == 65,
                actual -> 65 < actual,
                actual -> 65 > actual
        );
        assertFalse(existe65);
    }

    @Test
    void obtenerAlturaReflejaDegeneracionYBalanceo() {
        arbol.insertar(10);
        arbol.insertar(20);
        arbol.insertar(30);
        arbol.insertar(40);
        assertEquals(4, arbol.obtenerAltura());
    }
}