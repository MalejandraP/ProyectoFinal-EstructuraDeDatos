package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class PilaTest {

    private Pila<String> pila;

    @BeforeEach
    void preparar() {
        pila = new Pila<>();
    }

    /** Recorre con for-each (de la cima al fondo) y arma "[a, b, c]". */
    private String contenido(Pila<String> p) {
        StringBuilder sb = new StringBuilder("[");
        for (String s : p) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(s);
        }
        return sb.append("]").toString();
    }

    // ------------------------------------------------------------ estado inicial

    @Test
    void pilaNuevaEstaVacia() {
        assertTrue(pila.estaVacia());
        assertEquals("[]", contenido(pila));
    }

    // ---------------------------------------------------------------- LIFO

    @Test
    void desapilarRespetaElOrdenInverso() {
        pila.apilar("a");
        pila.apilar("b");
        pila.apilar("c");
        assertEquals("c", pila.desapilar());
        assertEquals("b", pila.desapilar());
        assertEquals("a", pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    void apilarDejaElElementoEnLaCima() {
        pila.apilar("a");
        pila.apilar("b");
        assertEquals("b", pila.cima());
        assertEquals("[b, a]", contenido(pila));
        assertFalse(pila.estaVacia());
    }

    @Test
    void desapilarEnPilaVaciaLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> pila.desapilar());
    }

    @Test
    void despuesDeVaciarSePuedeReutilizar() {
        pila.apilar("a");
        pila.desapilar();
        assertTrue(pila.estaVacia());
        pila.apilar("b");
        assertEquals("b", pila.cima());
    }

    @Test
    void intercalarApilarYDesapilar() {
        pila.apilar("a");
        pila.apilar("b");
        assertEquals("b", pila.desapilar());
        pila.apilar("c");
        assertEquals("c", pila.desapilar());
        assertEquals("a", pila.desapilar());
    }

    // ---------------------------------------------------------------- cima

    @Test
    void cimaNoQuitaElElemento() {
        pila.apilar("a");
        assertEquals("a", pila.cima());
        assertEquals("a", pila.cima());
        assertFalse(pila.estaVacia());
    }

    @Test
    void cimaEnPilaVaciaLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> pila.cima());
    }

    // --------------------------------------------------------- buscar/localizar

    @Test
    void buscarEncuentraPresentesYNoAusentes() {
        pila.apilar("a");
        pila.apilar("b");
        assertTrue(pila.buscar("a"));
        assertFalse(pila.buscar("z"));
    }

    @Test
    void buscarEnPilaVaciaLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> pila.buscar("a"));
    }

    @Test
    void localizarEnPilaVaciaDevuelveMenosUno() {
        assertEquals(-1, pila.localizar("a"));
    }

    @Test
    void localizarDevuelvePosicionDesdeLaCima() {
        pila.apilar("a");
        pila.apilar("b");
        pila.apilar("c");
        assertEquals(0, pila.localizar("c"));
        assertEquals(2, pila.localizar("a"));
        assertEquals(-1, pila.localizar("z"));
    }

    // ------------------------------------------------------------- mostrar

    @Test
    void mostrarPilaVaciaYConElementos() {
        assertEquals("[ Null ]", pila.mostrar());
        pila.apilar("a");
        pila.apilar("b");
        assertEquals("[ b a ]", pila.mostrar());
    }

    // ------------------------------------------------------------- iterador

    @Test
    void iteradorRecorreDeLaCimaAlFondoSinConsumir() {
        pila.apilar("a");
        pila.apilar("b");
        assertEquals("[b, a]", contenido(pila));
        assertEquals("[b, a]", contenido(pila));
        assertEquals("b", pila.desapilar());
    }

    @Test
    void iteradorDePilaVaciaNoTieneSiguiente() {
        assertFalse(pila.iterator().hasNext());
    }

    @Test
    void nextSinElementosLanzaExcepcion() {
        Iterator<String> it = pila.iterator();
        assertThrows(EstructuraVaciaException.class, it::next);
    }

    @Test
    void dosIteradoresSonIndependientes() {
        pila.apilar("a");
        pila.apilar("b");
        Iterator<String> uno = pila.iterator();
        Iterator<String> dos = pila.iterator();
        uno.next();
        assertEquals("b", dos.next());
    }

    // ------------------------------------------------------------ genéricos

    @Test
    void funcionaConOtrosTipos() {
        Pila<Integer> numeros = new Pila<>();
        numeros.apilar(10);
        numeros.apilar(20);
        assertEquals(20, numeros.desapilar());
        assertEquals(10, numeros.desapilar());
    }
}
