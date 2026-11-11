package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ColaPreparacionTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 9, 11, 0);

    private Apartamento ap(String codigo) {
        return new Apartamento(codigo, "S", "A", 1, 1, 2, 100000);
    }

    @Test
    void conEntradaHoyVaPrimeroYLuegoPorHoraMasTemprana() {
        Apartamento sinEntrada = ap("A100");
        Apartamento tarde = ap("A200");
        Apartamento temprano = ap("A300");
        ColaPreparacion cola = new ColaPreparacion(a -> {
            if (a == tarde) return AHORA.withHour(18);
            if (a == temprano) return AHORA.withHour(14);
            return null;
        });
        cola.encolar(sinEntrada, AHORA);
        cola.encolar(tarde, AHORA);
        cola.encolar(temprano, AHORA);
        assertSame(temprano, cola.siguiente().getApartamento());
        assertSame(tarde, cola.siguiente().getApartamento());
        assertSame(sinEntrada, cola.siguiente().getApartamento());
        assertTrue(cola.estaVacia());
    }

    @Test
    void sinEntradaSeAtiendeEnOrdenDeLlegada() {
        ColaPreparacion cola = new ColaPreparacion(a -> null);
        Apartamento primero = ap("A900");
        Apartamento segundo = ap("A100");
        cola.encolar(primero, AHORA);
        cola.encolar(segundo, AHORA.plusMinutes(10));
        assertSame(primero, cola.siguiente().getApartamento());
    }

    @Test
    void empateTotalSeDesempataPorCodigo() {
        ColaPreparacion cola = new ColaPreparacion(a -> null);
        cola.encolar(ap("B2"), AHORA);
        cola.encolar(ap("A1"), AHORA);
        assertEquals("A1", cola.siguiente().getApartamento().getCodigo());
        assertEquals(1, cola.tamano());
    }

}