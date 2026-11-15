package co.edu.uniquindio.apartasuitesuq.model.Estructuras;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ListaCircularTest {

    private ListaCircular<String> lista;

    @BeforeEach
    void preparar() {
        lista = new ListaCircular<>();
    }

    private ListaCircular<String> con(String... datos) {
        for (String d : datos) lista.agregarFinal(d);
        return lista;
    }

    private String contenido() {
        StringBuilder sb = new StringBuilder("[");
        for (String s : lista) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(s);
        }
        return sb.append("]").toString();
    }

    // ------------------------------------------------------------ estado inicial

    @Test
    void listaNuevaEstaVacia() {
        assertTrue(lista.estaVacia());
        assertEquals("[]", contenido());
    }

    @Test
    void operacionesEnListaVaciaLanzanExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> lista.datoActual());
        assertThrows(EstructuraVaciaException.class, () -> lista.avanzar());
        assertThrows(EstructuraVaciaException.class, () -> lista.buscar("a"));
        assertThrows(EstructuraVaciaException.class, () -> lista.eliminarDato("a"));
    }

    // -------------------------------------------------------------- agregar

    @Test
    void agregarFinalConservaElOrden() {
        con("a", "b", "c");
        assertEquals("[a, b, c]", contenido());
        assertFalse(lista.estaVacia());
    }

    // ------------------------------------------------------ turno (circular)

    @Test
    void elTurnoActualEmpiezaEnElPrimero() {
        con("a", "b", "c");
        assertEquals("a", lista.datoActual());
    }

    @Test
    void avanzarRotaYDaLaVueltaAlPrincipio() {
        con("a", "b", "c");
        assertEquals("b", lista.avanzar());
        assertEquals("c", lista.avanzar());
        assertEquals("a", lista.avanzar());   // vuelve al inicio
        assertEquals("b", lista.avanzar());
    }

    @Test
    void conUnSoloElementoAvanzarSeQuedaEnEl() {
        con("a");
        assertEquals("a", lista.avanzar());
        assertEquals("a", lista.datoActual());
    }

    @Test
    void agregarNoMueveElTurnoActual() {
        con("a", "b");
        lista.avanzar();                       // actual = b
        lista.agregarFinal("c");
        assertEquals("b", lista.datoActual());
    }

    // --------------------------------------------------------------- buscar

    @Test
    void buscarPresentesYAusentes() {
        con("a", "b", "c");
        assertTrue(lista.buscar("a"));
        assertTrue(lista.buscar("c"));
        assertFalse(lista.buscar("z"));
    }

    // ------------------------------------------------------------- eliminar

    @Test
    void eliminarEnElMedio() {
        con("a", "b", "c");
        assertTrue(lista.eliminarDato("b"));
        assertEquals("[a, c]", contenido());
    }

    @Test
    void eliminarElPrimero() {
        con("a", "b", "c");
        assertTrue(lista.eliminarDato("a"));
        assertEquals("[b, c]", contenido());
    }

    @Test
    void eliminarElUltimo() {
        con("a", "b", "c");
        assertTrue(lista.eliminarDato("c"));
        assertEquals("[a, b]", contenido());
    }

    @Test
    void eliminarElUnicoDejaLaListaVaciaYReutilizable() {
        con("a");
        assertTrue(lista.eliminarDato("a"));
        assertTrue(lista.estaVacia());
        lista.agregarFinal("b");
        assertEquals("b", lista.datoActual());
    }

    @Test
    void eliminarUnInexistenteDevuelveFalse() {
        con("a", "b");
        assertFalse(lista.eliminarDato("z"));
        assertEquals("[a, b]", contenido());
    }

    @Test
    void elAnilloSigueCerradoTrasEliminar() {
        // si el enlace quedó mal, avanzar se sale del anillo o se cuelga
        con("a", "b", "c");
        lista.eliminarDato("c");
        assertEquals("b", lista.avanzar());
        assertEquals("a", lista.avanzar());
        assertEquals("b", lista.avanzar());
    }

    @Test
    void eliminarElTurnoActualPasaAlSiguiente() {
        con("a", "b", "c");
        lista.eliminarDato("a");               // a era el turno actual
        assertEquals("b", lista.datoActual());
    }

    @Test
    void eliminarElActualEnElUltimoDaLaVuelta() {
        con("a", "b", "c");
        lista.avanzar();
        lista.avanzar();                       // actual = c
        lista.eliminarDato("c");
        assertEquals("a", lista.datoActual());
    }

    @Test
    void eliminarOtroNoCambiaElTurnoActual() {
        con("a", "b", "c");
        lista.eliminarDato("b");
        assertEquals("a", lista.datoActual());
    }

    @Test
    void eliminarVariasHastaVaciar() {
        con("a", "b", "c");
        lista.eliminarDato("a");
        lista.eliminarDato("b");
        lista.eliminarDato("c");
        assertTrue(lista.estaVacia());
        assertEquals("[]", contenido());
    }

    // -------------------------------------------------------------- mostrar

    @Test
    void mostrarVaciaYConElementos() {
        assertEquals("[ Null ]", lista.mostrar());
        con("a", "b");
        assertEquals("[ a b ]", lista.mostrar());
    }

    // ------------------------------------------------------------- iterador

    @Test
    void iteradorDaUnaSolaVueltaAunqueSeaCircular() {
        con("a", "b", "c");
        assertEquals("[a, b, c]", contenido());
        assertEquals("[a, b, c]", contenido());   // no consume ni se cuelga
    }

    @Test
    void iteradorDeListaVaciaNoTieneSiguiente() {
        assertFalse(lista.iterator().hasNext());
    }

    @Test
    void nextAlTerminarLanzaExcepcion() {
        con("a");
        Iterator<String> it = lista.iterator();
        it.next();
        assertThrows(EstructuraVaciaException.class, it::next);
    }
}
