package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Servicios.ColaPreparacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CargadorDatosTest {

    /** "Hoy" fijo para que las fechas relativas del escenario den siempre lo mismo. */
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
    void cargaTodasLasSecciones() {
        assertEquals(8, contar(sga.getApartamentos()));
        assertEquals(7, contar(sga.getHuespedes()));
        assertEquals(4, contar(sga.getEmpleados()));
        assertEquals(9, contar(sga.getReservas()));
        assertEquals(8, contar(sga.getCalendario().getPeriodos()));
        assertEquals(12, sga.getMapa().getGrafo().tamano());
        assertEquals("REC", sga.getMapa().getRecepcion());
        assertEquals(3, sga.getSolicitudesExternasPendientes());
        assertNotNull(sga.autenticar("admin", "admin123"));
        assertEquals("R1", sga.turnoActual().getId());
    }

    @Test
    void lasFechasRelativasSeResuelvenConElRelojDelSistema() {
        Reserva res3 = sga.buscarReserva("RES-3");
        assertEquals(LocalDate.of(2026, 11, 12), res3.getFechaInicio());
        assertEquals(LocalDate.of(2026, 11, 15), res3.getFechaFin());
    }

    @Test
    void liquidaLasReservasCargadasNochePorNoche() {
        // RES-9: 12 al 17 de diciembre, cruza Media → Alta; Sara cumple 6 años el 14, Martín es bebé
        Reserva res9 = sga.buscarReserva("RES-9");
        assertEquals(4, res9.contarOcupantesRegistrados());
        double esperado = 2 * 110000 + 2 * 110000 + 3 * 110000 + 3 * 140000 + 3 * 140000;
        assertEquals(esperado, res9.getCostoTotal(), 0.001);
        assertEquals(res9.getValorAnticipo(), res9.getFolio().totalPago(), 0.001);
    }

    @Test
    void aplicaLosEstadosDeLosApartamentos() {
        assertEquals(EstadoApartamento.OCUPADO, sga.buscarApartamento("A102").getEstadoOperativo(),
                "tiene una reserva en curso");
        Apartamento a202 = sga.buscarApartamento("A202");
        assertTrue(a202.estaFueraDeServicio());
        assertEquals("Fuga de agua en el baño principal", sga.motivoFueraDeServicio(a202));
        assertTrue(sga.buscarApartamento("A101").estaPreparado());
    }

    @Test
    void laColaDePreparacionAtiendePrimeroLasEntradasDeHoy() {
        ColaPreparacion.Item primero = sga.iniciarSiguientePreparacion();
        assertEquals("B101", primero.getApartamento().getCodigo(), "llegan a las 11:00");
        assertEquals(1, primero.getPrioridad());
        assertEquals("A103", sga.iniciarSiguientePreparacion().getApartamento().getCodigo(), "llegan a las 14:00");
        ColaPreparacion.Item ultimo = sga.iniciarSiguientePreparacion();
        assertEquals("B201", ultimo.getApartamento().getCodigo());
        assertEquals(2, ultimo.getPrioridad());
    }

    @Test
    void venceLaReservaPendienteDeHaceDosDias() {
        ListaDoble<Reserva> canceladas = sga.procesarVencimientos();
        assertEquals(1, contar(canceladas));
        assertEquals("RES-5", canceladas.obtener(0).getCodigoReserva());
        assertEquals(EstadoReserva.PENDIENTE, sga.buscarReserva("RES-6").getEstado());
    }

    @Test
    void lasReservasNuevasNoRepitenCodigosCargados() {
        SGA real = new SGA();
        assertDoesNotThrow(real::cargarEscenarioDeEjemplo);
        Reserva nueva = real.crearReserva(real.buscarHuesped("1094000001"), "A103",
                real.hoy().plusDays(40), real.hoy().plusDays(41), null, 1, Canal.DIRECTA);
        assertEquals("RES-10", nueva.getCodigoReserva());
    }

    @Test
    void unErrorIndicaLaLineaYNoModificaElSistema() {
        String archivo = """
                # comentario
                [APARTAMENTOS]
                Z1;Prueba;Z;1;1;cuatro;100;100;100
                """;
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> sga.cargarEscenario(new StringReader(archivo)));
        assertTrue(e.getMessage().startsWith("Línea 3"), e.getMessage());
        assertEquals(8, contar(sga.getApartamentos()), "el escenario anterior sigue intacto");
    }

    @Test
    void rechazaReservasActivasSolapadas() {
        String archivo = """
                [APARTAMENTOS]
                A1;Uno;A;1;1;2;100;100;100
                [HUESPEDES]
                1;Ana;1990-01-01;300;a@x.co
                [RESERVAS]
                R1;1;A1;HOY+1;HOY+4;DIRECTA;CONFIRMADA;1;HOY
                R2;1;A1;HOY+3;HOY+5;DIRECTA;CONFIRMADA;1;HOY
                """;
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> sga.cargarEscenario(new StringReader(archivo)));
        assertTrue(e.getMessage().startsWith("Línea 7"), e.getMessage());
        assertInstanceOf(ReservaSolapadaException.class, e.getCause());
    }

    @Test
    void seccionesDesconocidasSonUnError() {
        assertThrows(IllegalArgumentException.class,
                () -> sga.cargarEscenario(new StringReader("[FACTURAS]\nx;y\n")));
    }
}
