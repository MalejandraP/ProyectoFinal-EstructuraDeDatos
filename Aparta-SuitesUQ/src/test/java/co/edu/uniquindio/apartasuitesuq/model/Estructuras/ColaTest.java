package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ColaTest {

    private Cola<String> cola;

    @BeforeEach
    void preparar() {
        cola = new Cola<>();
    }

    private String contenido(Cola<String> c) {
        StringBuilder sb = new StringBuilder("[");
        for (String s : c) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(s);
        }
        return sb.append("]").toString();
    }

    // ------------------------------------------------------------ estado inicial

    @Test
    void colaNuevaEstaVacia() {
        assertTrue(cola.estaVacia());
        assertEquals("[]", contenido(cola));
    }

    // ---------------------------------------------------------------- FIFO

    @Test
    void desencolarRespetaElOrdenDeLlegada() {
        cola.encolar("a");
        cola.encolar("b");
        cola.encolar("c");
        assertEquals("a", cola.desencolar());
        assertEquals("b", cola.desencolar());
        assertEquals("c", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void encolarDejaElElementoAlFinal() {
        cola.encolar("a");
        cola.encolar("b");
        assertEquals("[a, b]", contenido(cola));
        assertFalse(cola.estaVacia());
    }

    @Test
    void desencolarEnColaVaciaLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> cola.desencolar());
    }

    @Test
    void despuesDeVaciarSePuedeReutilizar() {
        // detecta un 'fin' que no se puso en null al vaciar
        cola.encolar("a");
        cola.desencolar();
        assertTrue(cola.estaVacia());
        cola.encolar("b");
        cola.encolar("c");
        assertEquals("[b, c]", contenido(cola));
        assertEquals("b", cola.desencolar());
    }

    @Test
    void intercalarEncolarYDesencolar() {
        cola.encolar("a");
        cola.encolar("b");
        assertEquals("a", cola.desencolar());
        cola.encolar("c");
        assertEquals("b", cola.desencolar());
        assertEquals("c", cola.desencolar());
    }

    // --------------------------------------------------------- buscar/localizar

    @Test
    void buscarEncuentraPresentesYNoAusentes() {
        cola.encolar("a");
        cola.encolar("b");
        assertTrue(cola.buscar("b"));
        assertFalse(cola.buscar("z"));
    }

    @Test
    void buscarEnColaVaciaLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> cola.buscar("a"));
    }

    @Test
    void localizarDevuelvePosicionDesdeElFrente() {
        cola.encolar("a");
        cola.encolar("b");
        cola.encolar("c");
        assertEquals(0, cola.localizar("a"));
        assertEquals(2, cola.localizar("c"));
    }

    @Test
    void localizarInexistenteODeColaVaciaDevuelveMenosUno() {
        assertEquals(-1, cola.localizar("a"));
        cola.encolar("a");
        assertEquals(-1, cola.localizar("z"));
    }

    @Test
    void localizarCambiaTrasDesencolar() {
        cola.encolar("a");
        cola.encolar("b");
        cola.desencolar();
        assertEquals(0, cola.localizar("b"));
    }

    // ------------------------------------------------------------- mostrar

    @Test
    void mostrarColaVaciaYConElementos() {
        assertEquals("[ Null ]", cola.mostrar());
        cola.encolar("a");
        cola.encolar("b");
        assertEquals("[ a b ]", cola.mostrar());
    }

    // ------------------------------------------------------------- iterador

    @Test
    void iteradorRecorreEnOrdenYNoConsumeLaCola() {
        cola.encolar("a");
        cola.encolar("b");
        assertEquals("[a, b]", contenido(cola));
        assertEquals("[a, b]", contenido(cola)); // sigue igual
        assertEquals("a", cola.desencolar());
    }

    @Test
    void iteradorDeColaVaciaNoTieneSiguiente() {
        assertFalse(cola.iterator().hasNext());
    }

    @Test
    void nextSinElementosLanzaExcepcion() {
        Iterator<String> it = cola.iterator();
        assertThrows(EstructuraVaciaException.class, it::next);
    }

    @Test
    void dosIteradoresSonIndependientes() {
        cola.encolar("a");
        cola.encolar("b");
        Iterator<String> uno = cola.iterator();
        Iterator<String> dos = cola.iterator();
        uno.next();
        assertEquals("a", dos.next());
    }

    // ------------------------------------------------------------ genéricos

    @Test
    void funcionaConObjetosDelDominio() {
        Cola<Integer> numeros = new Cola<>();
        numeros.encolar(10);
        numeros.encolar(20);
        assertEquals(10, numeros.desencolar());
        assertEquals(20, numeros.desencolar());
    }
}