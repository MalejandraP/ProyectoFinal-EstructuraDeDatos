package co.edu.uniquindio.apartasuitesuq.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class EstanciaTest {

    private static final LocalDate ENTRADA = LocalDate.of(2026, 12, 30);
    private static final LocalDate SALIDA = LocalDate.of(2027, 1, 2);

    @Test
    void recorreCadaNocheSinIncluirLaDeSalida() {
        Estancia estancia = new Estancia(ENTRADA, SALIDA);
        LocalDate esperada = ENTRADA;
        int noches = 0;
        for (LocalDate noche : estancia) {
            assertEquals(esperada, noche);
            esperada = esperada.plusDays(1);
            noches++;
        }
        assertEquals(3, noches);
        assertEquals(3, estancia.getNumeroNoches());
        assertEquals(SALIDA, esperada, "el recorrido debe detenerse justo en la fecha de salida");
    }

    @Test
    void elIteradorLanzaExcepcionAlAgotarse() {
        Iterator<LocalDate> it = new Estancia(ENTRADA, ENTRADA.plusDays(1)).iterator();
        assertTrue(it.hasNext());
        assertEquals(ENTRADA, it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void cadaIteradorEsIndependiente() {
        Estancia estancia = new Estancia(ENTRADA, SALIDA);
        Iterator<LocalDate> primero = estancia.iterator();
        primero.next();
        primero.next();
        assertEquals(ENTRADA, estancia.iterator().next());
    }

    @Test
    void rechazaFechasInvalidas() {
        assertThrows(FechasInvalidasException.class, () -> new Estancia(ENTRADA, ENTRADA));
        assertThrows(FechasInvalidasException.class, () -> new Estancia(SALIDA, ENTRADA));
        assertThrows(FechasInvalidasException.class, () -> new Estancia(null, SALIDA));
    }

    @Test
    void contieneYSolapamientoUsanIntervaloSemiabierto() {
        Estancia estancia = new Estancia(ENTRADA, SALIDA);
        assertTrue(estancia.contiene(ENTRADA));
        assertFalse(estancia.contiene(SALIDA));

        Estancia siguiente = new Estancia(SALIDA, SALIDA.plusDays(2));
        assertFalse(estancia.seSolapaCon(siguiente), "salir y entrar el mismo día no es solapamiento");
        assertTrue(estancia.seSolapaCon(new Estancia(SALIDA.minusDays(1), SALIDA.plusDays(1))));
    }
}
