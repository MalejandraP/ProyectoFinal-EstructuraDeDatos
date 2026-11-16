package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Consultas de la fachada que usa el tablero de Inicio, sobre el escenario con hoy = 12/11/2026. */
class TableroInicioTest {

    private static final Clock RELOJ = Clock.fixed(
            ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, ZoneId.of("America/Bogota")).toInstant(),
            ZoneId.of("America/Bogota"));

    private SGA sga;

    @BeforeEach
    void preparar() throws IOException {
        sga = new SGA(RELOJ);
        sga.cargarEscenarioDeEjemplo();
    }

    private static int contar(Iterable<?> datos) {
        int n = 0;
        for (Object ignored : datos) {
            n++;
        }
        return n;
    }

    @Test
    void resumenDelTablero() {
        int[] conteo = sga.contarPorEstado();
        assertEquals(3, conteo[EstadoApartamento.PREPARADO.ordinal()], "A101, A201 y B102");
        assertEquals(1, conteo[EstadoApartamento.OCUPADO.ordinal()]);
        assertEquals(3, conteo[EstadoApartamento.PENDIENTEDEPREPARACION.ordinal()]);
        assertEquals(1, conteo[EstadoApartamento.FUERADESERVICIO.ordinal()]);
        assertEquals(100.0 / 8, sga.ocupacionActual(), 0.001);
        assertEquals(2, contar(sga.entradasDeHoy()), "RES-3 y RES-4 entran hoy");
    }

    @Test
    void informacionContextualPorApartamento() {
        Apartamento a102 = sga.buscarApartamento("A102");
        assertEquals("RES-2", sga.reservaEnCurso(a102).getCodigoReserva());
        assertEquals("RES-3", sga.entradaDeHoy(sga.buscarApartamento("A103")).getCodigoReserva());
        assertEquals(1, sga.prioridadPreparacion(sga.buscarApartamento("B101")));
        assertEquals(2, sga.prioridadPreparacion(sga.buscarApartamento("B201")));
        assertEquals(0, sga.prioridadPreparacion(sga.buscarApartamento("A101")));
        assertEquals("RES-9", sga.proximaReserva(sga.buscarApartamento("A101")).getCodigoReserva());
    }

    @Test
    void filtrosYBusquedas() {
        ListaDoble<Apartamento> bloqueB = sga.filtrarApartamentos(null, "B", null);
        assertEquals(3, contar(bloqueB));
        assertEquals("B201", sga.filtrarApartamentos(null, "B", 2).obtener(0).getCodigo());
        assertEquals(3, contar(sga.filtrarApartamentos(new EstadoApartamento[]{
                EstadoApartamento.PENDIENTEDEPREPARACION, EstadoApartamento.ENPREPARACION}, null, null)));
        assertEquals("A101", sga.apartamentosDeHuesped("1094000001").obtener(0).getCodigo(), "RES-9 de Ana");
        assertTrue(sga.apartamentosDeHuesped("no-existe").estaVacia());
        assertEquals(2, contar(sga.bloques()));
        assertEquals(2, contar(sga.pisos()));
    }

    @Test
    void checkInYCheckOutDesdeElTablero() {
        // A103 entra hoy pero sigue pendiente de preparación: no se puede hacer check-in
        assertThrows(TransicionEstadoInvalidaException.class, () -> sga.checkInDeHoy("A103"));
        assertThrows(TransicionEstadoInvalidaException.class, () -> sga.checkInDeHoy("A101"), "sin entrada hoy");

        Reserva salida = sga.checkOutApartamento("A102");
        assertEquals(EstadoReserva.FINALIZADA, salida.getEstado());
        assertEquals(EstadoApartamento.PENDIENTEDEPREPARACION, sga.buscarApartamento("A102").getEstadoOperativo());
        assertThrows(TransicionEstadoInvalidaException.class, () -> sga.checkOutApartamento("A102"));
    }

    @Test
    void rutaDesdeRecepcion() {
        assertEquals(22, sga.rutaHacia("A101").getDistancia(), 0.001, "REC→ESC_A 12 m + ESC_A→A101 10 m");
    }
}
