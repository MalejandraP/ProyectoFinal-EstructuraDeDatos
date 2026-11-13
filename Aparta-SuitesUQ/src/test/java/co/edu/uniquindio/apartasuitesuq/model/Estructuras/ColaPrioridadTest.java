package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class ColaPrioridadTest {

    @Test
    void desencolaDeMenorAMayor() {
        ColaPrioridad<Integer> c = new ColaPrioridad<>(Comparator.naturalOrder());
        int[] datos = {5, 3, 9, 1, 7, 3, 8, 2, 6, 4, 0, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        for (int d : datos) c.encolar(d);
        assertEquals(datos.length, c.tamano());
        int anterior = Integer.MIN_VALUE;
        while (!c.estaVacia()) {
            int x = c.desencolar();
            assertTrue(x >= anterior, "orden roto: " + anterior + " antes de " + x);
            anterior = x;
        }
    }

    @Test
    void comparadorInvertidoDaMaximoPrimero() {
        ColaPrioridad<Integer> c = new ColaPrioridad<>(Comparator.reverseOrder());
        c.encolar(2); c.encolar(9); c.encolar(4);
        assertEquals(9, c.ver().intValue());
        assertEquals(9, c.desencolar().intValue());
        assertEquals(4, c.desencolar().intValue());
    }

    @Test
    void vaciaLanzaExcepcionPropia() {
        ColaPrioridad<String> c = new ColaPrioridad<>(Comparator.naturalOrder());
        assertThrows(EstructuraVaciaException.class, c::desencolar);
        assertThrows(EstructuraVaciaException.class, c::ver);
    }

    @Test
    void iteradorNoConsumeLaCola() {
        ColaPrioridad<Integer> c = new ColaPrioridad<>(Comparator.naturalOrder());
        c.encolar(3); c.encolar(1); c.encolar(2);
        StringBuilder sb = new StringBuilder();
        for (int x : c) sb.append(x);
        assertEquals("123", sb.toString());
        assertEquals(3, c.tamano());
    }

}