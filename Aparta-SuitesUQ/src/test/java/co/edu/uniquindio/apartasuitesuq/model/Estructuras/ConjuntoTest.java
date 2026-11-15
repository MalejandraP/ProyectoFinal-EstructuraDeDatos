package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ConjuntoTest {

    private Conjunto<String> conjunto;

    @BeforeEach
    void preparar() {
        conjunto = new Conjunto<>();
    }

    private String contenido(Conjunto<String> c) {
        StringBuilder sb = new StringBuilder("[");
        for (String s : c) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(s);
        }
        return sb.append("]").toString();
    }

    @Test
    void conjuntoNuevoEstaVacio() {
        assertTrue(conjunto.estaVacia());
        assertEquals(0, conjunto.tamanio());
    }

    @Test
    void agregarElementosDistintos() {
        assertTrue(conjunto.agregar("a"));
        assertTrue(conjunto.agregar("b"));
        assertEquals(2, conjunto.tamanio());
        assertEquals("[a, b]", contenido(conjunto));
    }

    @Test
    void noAceptaRepetidos() {
        assertTrue(conjunto.agregar("a"));
        assertFalse(conjunto.agregar("a"));
        assertEquals(1, conjunto.tamanio());
    }

    @Test
    void noAceptaNulos() {
        assertFalse(conjunto.agregar(null));
        assertTrue(conjunto.estaVacia());
    }

    @Test
    void buscarEnConjuntoVacioDevuelveFalse() {
        assertFalse(conjunto.buscar("a"));
    }

    @Test
    void buscarPresentesYAusentes() {
        conjunto.agregar("a");
        assertTrue(conjunto.buscar("a"));
        assertFalse(conjunto.buscar("z"));
    }

    @Test
    void eliminarPresenteYAusente() {
        conjunto.agregar("a");
        conjunto.agregar("b");
        assertTrue(conjunto.eliminar("a"));
        assertFalse(conjunto.eliminar("a"));
        assertEquals("[b]", contenido(conjunto));
    }

    @Test
    void eliminarEnConjuntoVacioDevuelveFalse() {
        assertFalse(conjunto.eliminar("a"));
    }

    @Test
    void sePuedeVolverAAgregarTrasEliminar() {
        conjunto.agregar("a");
        conjunto.eliminar("a");
        assertTrue(conjunto.agregar("a"));
        assertEquals(1, conjunto.tamanio());
    }

    @Test
    void iteradorRecorreEnOrdenDeInsercion() {
        conjunto.agregar("c");
        conjunto.agregar("a");
        conjunto.agregar("b");
        assertEquals("[c, a, b]", contenido(conjunto));
    }

    @Test
    void iteradorDeConjuntoVacioNoTieneSiguiente() {
        Iterator<String> it = conjunto.iterator();
        assertFalse(it.hasNext());
    }
}
