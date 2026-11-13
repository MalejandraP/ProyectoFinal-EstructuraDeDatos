package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Estadísticas sobre el escenario de ejemplo con "hoy" = 12 de noviembre de 2026.
 * Noches vendidas en noviembre: RES-2 (A102, 3), RES-3 (A103, 3) y RES-4 (B101, 2).
 */
class EstadisticasTest {

    private static final Clock RELOJ = Clock.fixed(
            ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, ZoneId.of("America/Bogota")).toInstant(),
            ZoneId.of("America/Bogota"));
    private static final Estancia NOVIEMBRE = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 1));

    private SGA sga;
    private Estadisticas estadisticas;

    @BeforeEach
    void preparar() throws IOException {
        sga = new SGA(RELOJ);
        sga.cargarEscenarioDeEjemplo();
        estadisticas = sga.getEstadisticas();
    }

    private static Indicador fila(ListaDoble<Indicador> filas, String etiqueta) {
        for (Indicador fila : filas) {
            if (fila.getEtiqueta().equals(etiqueta)) {
                return fila;
            }
        }
        fail("No existe la fila " + etiqueta);
        return null;
    }

    @Test
    void ocupacionPorApartamentoEnOrdenDeCodigo() {
        ListaDoble<Indicador> filas = estadisticas.ocupacionPorApartamento(NOVIEMBRE);
        assertEquals("A101", filas.obtener(0).getEtiqueta(), "el catálogo se recorre en inorden");
        assertEquals(10.0, fila(filas, "A102").getValor(), 0.001, "3 de 30 noches");
        assertEquals(0.0, fila(filas, "B102").getValor(), 0.001, "RES-5 está pendiente: no cuenta");
        assertEquals(8 * 100.0 / (30 * 8), estadisticas.ocupacionGeneral(NOVIEMBRE), 0.001);
    }

    @Test
    void ocupacionPorTemporada() {
        Estancia diciembre = new Estancia(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 1));
        ListaDoble<Indicador> filas = estadisticas.ocupacionPorTemporada(diciembre);
        // RES-9 (A101): 12, 13 y 14 en Media (14 noches x 8 apts); 15 y 16 en Alta (17 noches x 8 apts)
        assertEquals(3 * 100.0 / (14 * 8), fila(filas, "Media").getValor(), 0.001);
        assertEquals(2 * 100.0 / (17 * 8), fila(filas, "Alta").getValor(), 0.001);
        assertEquals(0.0, fila(filas, "Baja").getValor(), 0.001);
    }

    @Test
    void ingresosPorMesRepartenLasNochesLiquidadas() {
        Estancia periodo = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 1));
        ListaDoble<Indicador> meses = estadisticas.ingresosPorMes(periodo);
        // RES-2: 3 noches x 3 pagantes x 85.000 | RES-3: 3 x 2 x 70.000 | RES-4: 2 x 5 x 80.000
        assertEquals("2026-11", meses.obtener(0).getEtiqueta());
        assertEquals(765000 + 420000 + 800000, meses.obtener(0).getValor(), 0.001);
        assertEquals(1610000, meses.obtener(1).getValor(), 0.001, "RES-9 cruza Media y Alta");
        assertEquals(765000 + 420000 + 800000 + 1610000, estadisticas.ingresosTotales(periodo), 0.001);
    }

    @Test
    void reservasPorCanalIncluyenCanceladasYNoShow() {
        Estancia periodo = new Estancia(LocalDate.of(2026, 10, 1), LocalDate.of(2027, 1, 1));
        int[][] conteo = estadisticas.reservasPorCanalYEstado(periodo);
        assertEquals(1, conteo[Canal.PORTAL.ordinal()][EstadoReserva.CANCELADA.ordinal()]);
        assertEquals(1, conteo[Canal.EXTERNO.ordinal()][EstadoReserva.NOSHOW.ordinal()]);

        ListaDoble<Indicador> porCanal = estadisticas.reservasPorCanal(periodo);
        assertEquals(5, fila(porCanal, Canal.PORTAL.getDescripcion()).getValor(), 0.001);
        assertEquals(3, fila(porCanal, Canal.DIRECTA.getDescripcion()).getValor(), 0.001);
        assertEquals(1, fila(porCanal, Canal.EXTERNO.getDescripcion()).getValor(), 0.001);
    }

    @Test
    void conflictosDelCanalExterno() throws IOException {
        SGA real = new SGA(); // la creación de reservas valida contra la fecha real
        real.cargarEscenarioDeEjemplo();
        while (real.getSolicitudesExternasPendientes() > 0) {
            real.procesarSiguienteSolicitud();
        }
        assertEquals(1, real.getEstadisticas().contarConflictosCanal(), "EXT-101 choca con RES-6");
        assertEquals(0, estadisticas.tiempoPromedioPreparacionMin(), 0.001);
    }
}
