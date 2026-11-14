package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrdenApartamentosTest {

    private static final LocalDate HOY = LocalDate.now();
    private SGA sga;

    @BeforeEach
    void preparar() {
        sga = new SGA();
        sga.registrarApartamento(new Apartamento("A101", "Uno", "A", 1, 2, 4, 90000));
        sga.registrarApartamento(new Apartamento("A102", "Dos", "A", 1, 1, 2, 70000));
        sga.registrarApartamento(new Apartamento("B101", "Tres", "B", 1, 3, 6, 80000));
        Huesped ana = new Huesped("1", "Ana", LocalDate.of(1990, 1, 1), "300", "a@x.co", null);
        sga.crearReserva(ana, "A102", HOY.plusDays(1), HOY.plusDays(2), null, 1, Canal.DIRECTA);
        sga.crearReserva(ana, "A102", HOY.plusDays(5), HOY.plusDays(6), null, 1, Canal.DIRECTA);
        sga.crearReserva(ana, "B101", HOY.plusDays(5), HOY.plusDays(6), null, 1, Canal.DIRECTA);
    }

    private String codigos(ListaDoble<Apartamento> lista) {
        StringBuilder sb = new StringBuilder();
        for (Apartamento a : lista) {
            sb.append(a.getCodigo()).append(' ');
        }
        return sb.toString().trim();
    }

    private ListaDoble<Apartamento> disponibles(java.util.Comparator<Apartamento> orden) {
        return sga.buscarDisponibles(HOY.plusDays(20), HOY.plusDays(21), 1, orden);
    }

    @Test
    void ordenaPorCadaCriterio() {
        assertEquals("A101 A102 B101", codigos(disponibles(null)), "orden natural: código");
        assertEquals("A102 B101 A101", codigos(disponibles(OrdenApartamentos.porPrecio())));
        assertEquals("B101 A101 A102", codigos(disponibles(OrdenApartamentos.porCapacidad())));
        assertEquals("B101 A101 A102", codigos(disponibles(OrdenApartamentos.porDormitorios())));
        assertEquals("A102 B101 A101", codigos(disponibles(OrdenApartamentos.porNumeroDeReservas(sga))));
    }
}
