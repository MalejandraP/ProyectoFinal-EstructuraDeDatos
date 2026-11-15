package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ListaDobleTest {

    private ListaDoble<String> lista;

    @BeforeEach
    void preparar() {
        lista = new ListaDoble<>();
    }

    /** Recorre con for-each (usa el Iterator propio) y arma "[a, b, c]". */
    private String contenido(ListaDoble<String> l) {
        StringBuilder sb = new StringBuilder("[");
        for (String s : l) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(s);
        }
        return sb.append("]").toString();
    }

    // ---------------------------------------------------------- estado inicial

    @Test
    void listaNuevaEstaVacia() {
        assertTrue(lista.estaVacia());
        assertEquals(0, lista.tamanio());
        assertEquals("[]", contenido(lista));
    }

    // ------------------------------------------------------------- agregar

    @Test
    void agregarFinalConservaElOrden() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarFinal("c");
        assertEquals("[a, b, c]", contenido(lista));
        assertEquals(3, lista.tamanio());
        assertFalse(lista.estaVacia());
    }

    @Test
    void agregarInicioPoneElElementoAlPrincipio() {
        lista.agregarInicio("a");
        lista.agregarInicio("b");
        lista.agregarInicio("c");
        assertEquals("[c, b, a]", contenido(lista));
    }

    @Test
    void mezclarAgregarInicioYFinal() {
        lista.agregarFinal("b");
        lista.agregarInicio("a");
        lista.agregarFinal("c");
        assertEquals("[a, b, c]", contenido(lista));
    }

    // ------------------------------------------------------ eliminar extremos

    @Test
    void eliminarInicioQuitaElPrimero() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.eliminarInicio();
        assertEquals("[b]", contenido(lista));
        assertEquals(1, lista.tamanio());
    }

    @Test
    void eliminarFinalQuitaElUltimo() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.eliminarFinal();
        assertEquals("[a]", contenido(lista));
        assertEquals(1, lista.tamanio());
    }

    @Test
    void eliminarElUnicoElementoDejaLaListaVaciaYReutilizable() {
        lista.agregarFinal("a");
        lista.eliminarInicio();
        assertTrue(lista.estaVacia());
        lista.agregarFinal("b");
        assertEquals("[b]", contenido(lista));
    }

    @Test
    void eliminarInicioYFinalEnListaVaciaLanzanExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> lista.eliminarInicio());
        assertThrows(EstructuraVaciaException.class, () -> lista.eliminarFinal());
    }

    @Test
    void despuesDeEliminarFinalSePuedeAgregarFinal() {
        // detecta un 'fin.siguiente' mal actualizado
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.eliminarFinal();
        lista.agregarFinal("c");
        assertEquals("[a, c]", contenido(lista));
    }

    @Test
    void despuesDeEliminarInicioSePuedeAgregarInicio() {
        // detecta un 'inicio.anterior' mal actualizado
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.eliminarInicio();
        lista.agregarInicio("c");
        assertEquals("[c, b]", contenido(lista));
    }

    // ---------------------------------------------------- eliminar por valor

    @Test
    void eliminarPorValorEnElMedio() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarFinal("c");
        assertTrue(lista.eliminar("b"));
        assertEquals("[a, c]", contenido(lista));
        assertEquals(2, lista.tamanio());
    }

    @Test
    void eliminarPorValorEnLosExtremos() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarFinal("c");
        assertTrue(lista.eliminar("a"));
        assertTrue(lista.eliminar("c"));
        assertEquals("[b]", contenido(lista));
    }

    @Test
    void eliminarUnValorInexistenteDevuelveFalse() {
        lista.agregarFinal("a");
        assertFalse(lista.eliminar("z"));
        assertEquals(1, lista.tamanio());
    }

    @Test
    void eliminarEnListaVaciaDevuelveFalse() {
        assertFalse(lista.eliminar("a"));
    }

    @Test
    void eliminarSoloQuitaLaPrimeraOcurrencia() {
        lista.agregarFinal("a");
        lista.agregarFinal("x");
        lista.agregarFinal("a");
        lista.eliminar("a");
        assertEquals("[x, a]", contenido(lista));
    }

    @Test
    void enlacesAnterioresQuedanBienTrasEliminarEnElMedio() {
        // eliminarFinal usa fin.anterior: si eliminar() del medio dejó mal un enlace, esto falla
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarFinal("c");
        lista.eliminar("b");
        lista.eliminarFinal();
        assertEquals("[a]", contenido(lista));
        lista.eliminarFinal();
        assertTrue(lista.estaVacia());
    }

    // -------------------------------------------------------- obtener/buscar

    @Test
    void obtenerDevuelveElElementoDeLaPosicion() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        lista.agregarFinal("c");
        assertEquals("a", lista.obtener(0));
        assertEquals("b", lista.obtener(1));
        assertEquals("c", lista.obtener(2));
    }

    @Test
    void obtenerFueraDeRangoDevuelveNull() {
        lista.agregarFinal("a");
        assertNull(lista.obtener(1));
        assertNull(lista.obtener(-1));
    }

    @Test
    void obtenerEnListaVaciaLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> lista.obtener(0));
    }

    @Test
    void buscarEncuentraElementosPresentes() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        assertTrue(lista.buscar("a"));
        assertTrue(lista.buscar("b"));
        assertFalse(lista.buscar("z"));
    }

    @Test
    void buscarEnListaVaciaDevuelveFalse() {
        assertFalse(lista.buscar("a"));
    }

    // --------------------------------------------------------------- copiar

    @Test
    void copiarGeneraUnaListaIndependiente() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        ListaDoble<String> copia = lista.copiar();
        assertEquals("[a, b]", contenido(copia));

        copia.agregarFinal("c");
        lista.eliminarInicio();
        assertEquals("[b]", contenido(lista));
        assertEquals("[a, b, c]", contenido(copia));
    }

    @Test
    void copiarUnaListaVaciaDaUnaListaVacia() {
        assertTrue(lista.copiar().estaVacia());
    }

    // ------------------------------------------------------------- iterador

    @Test
    void iteradorRecorreTodosLosElementosEnOrden() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        Iterator<String> it = lista.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void iteradorDeListaVaciaNoTieneSiguiente() {
        assertFalse(lista.iterator().hasNext());
    }

    @Test
    void nextSinElementosLanzaExcepcion() {
        Iterator<String> it = lista.iterator();
        assertThrows(EstructuraVaciaException.class, it::next);
    }

    @Test
    void dosIteradoresSonIndependientes() {
        lista.agregarFinal("a");
        lista.agregarFinal("b");
        Iterator<String> uno = lista.iterator();
        Iterator<String> dos = lista.iterator();
        uno.next();
        assertEquals("a", dos.next());
    }

    // ------------------------------------------------------------- genéricos

    @Test
    void funcionaConCualquierTipo() {
        ListaDoble<Integer> numeros = new ListaDoble<>();
        numeros.agregarFinal(10);
        numeros.agregarFinal(20);
        int suma = 0;
        for (int n : numeros) suma += n;
        assertEquals(30, suma);
    }
}
