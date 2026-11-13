package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CalendarioTemporadasTest {

    private CalendarioTemporadas calendario;

    @BeforeEach
    void preparar() {
        calendario = new CalendarioTemporadas();
        // se agregan desordenados a propósito
        calendario.agregarPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));
        calendario.agregarPeriodo(Temporada.MEDIA, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 31));
        calendario.agregarPeriodo(Temporada.MEDIA, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 14));
    }

    @Test
    void fechasSinPeriodoSonTemporadaBaja() {
        assertEquals(Temporada.BAJA, calendario.temporadaDe(LocalDate.of(2026, 3, 1)));
        assertEquals(Temporada.BAJA, calendario.temporadaDe(LocalDate.of(2027, 1, 16)));
        assertEquals(Temporada.BAJA, new CalendarioTemporadas().temporadaDe(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void losLimitesDelPeriodoSonInclusivos() {
        assertEquals(Temporada.MEDIA, calendario.temporadaDe(LocalDate.of(2026, 12, 1)));
        assertEquals(Temporada.MEDIA, calendario.temporadaDe(LocalDate.of(2026, 12, 14)));
        assertEquals(Temporada.ALTA, calendario.temporadaDe(LocalDate.of(2026, 12, 15)));
        assertEquals(Temporada.ALTA, calendario.temporadaDe(LocalDate.of(2027, 1, 15)));
    }

    @Test
    void mantieneLosPeriodosOrdenadosPorInicio() {
        LocalDate anterior = null;
        int cantidad = 0;
        for (PeriodoTemporada periodo : calendario.getPeriodos()) {
            if (anterior != null) {
                assertTrue(periodo.getInicio().isAfter(anterior));
            }
            anterior = periodo.getInicio();
            cantidad++;
        }
        assertEquals(3, cantidad);
    }

    @Test
    void rechazaPeriodosSolapados() {
        assertThrows(FechasInvalidasException.class, () -> calendario.agregarPeriodo(
                Temporada.MEDIA, LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 20)));
        assertThrows(FechasInvalidasException.class, () -> new PeriodoTemporada(
                Temporada.ALTA, LocalDate.of(2027, 2, 10), LocalDate.of(2027, 2, 1)));
    }

    @Test
    void eliminarYLimpiarDevuelvenLasFechasABaja() {
        PeriodoTemporada alta = new PeriodoTemporada(Temporada.ALTA,
                LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));
        assertTrue(calendario.eliminarPeriodo(alta));
        assertFalse(calendario.eliminarPeriodo(alta));
        assertEquals(Temporada.BAJA, calendario.temporadaDe(LocalDate.of(2026, 12, 25)));

        calendario.limpiar();
        assertTrue(calendario.estaVacio());
    }

    @Test
    void tarifaPorPersonaUsaLaTarifaDeLaTemporada() {
        Apartamento ap = new Apartamento("A101", "Suite Cafetal", "A", 1, 2, 4, 80000);
        ap.setTarifa(Temporada.ALTA, 150000);
        assertEquals(150000, calendario.tarifaPorPersona(ap, LocalDate.of(2026, 12, 24)));
        assertEquals(80000, calendario.tarifaPorPersona(ap, LocalDate.of(2026, 9, 1)));
    }
}
