package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FolioTest {

    private Folio folio;

    @BeforeEach
    void preparar() {
        folio = new Folio(null);
    }

    private int numeroMovimientos() {
        int n = 0;
        for (Movimiento m : folio.getMovimientos()) n++;
        return n;
    }

    // ------------------------------------------------------------ estado inicial

    @Test
    void folioNuevoEstaEnCero() {
        assertEquals(0, folio.calcularSaldo(), 0.001);
        assertEquals(0, folio.totalPago(), 0.001);
        assertEquals(0, numeroMovimientos());
        assertNull(folio.ultimaOperacion());
    }

    // ------------------------------------------------------------ cargos y pagos

    @Test
    void cargoSubeElSaldo() {
        folio.registrarCargo(100000, "Alojamiento");
        assertEquals(100000, folio.calcularSaldo(), 0.001);
        assertEquals(0, folio.totalPago(), 0.001);
    }

    @Test
    void pagoBajaElSaldoYSumaEnTotalPago() {
        folio.registrarCargo(100000, "Alojamiento");
        folio.registrarPago(30000, "Anticipo");
        assertEquals(70000, folio.calcularSaldo(), 0.001);
        assertEquals(30000, folio.totalPago(), 0.001);
    }

    @Test
    void variosPagosSeAcumulan() {
        folio.registrarCargo(100000, "Alojamiento");
        folio.registrarPago(30000, "Anticipo");
        folio.registrarPago(20000, "Abono");
        assertEquals(50000, folio.calcularSaldo(), 0.001);
        assertEquals(50000, folio.totalPago(), 0.001);
    }

    @Test
    void pagoMayorQueElCargoDejaSaldoNegativo() {
        folio.registrarCargo(10000, "Consumo");
        folio.registrarPago(15000, "Pago");
        assertEquals(-5000, folio.calcularSaldo(), 0.001);
    }

    @Test
    void montoCeroONegativoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> folio.registrarCargo(0, "x"));
        assertThrows(IllegalArgumentException.class, () -> folio.registrarPago(-5, "x"));
        assertEquals(0, numeroMovimientos());
    }

    @Test
    void losMovimientosSeListanEnOrdenDeRegistro() {
        folio.registrarCargo(100, "primero");
        folio.registrarPago(50, "segundo");
        StringBuilder sb = new StringBuilder();
        for (Movimiento m : folio.getMovimientos()) sb.append(m.getDescripcion()).append("|");
        assertEquals("primero|segundo|", sb.toString());
    }

    // ------------------------------------------------------------ deshacer

    @Test
    void deshacerUnCargoLoCompensaConUnPago() {
        folio.registrarCargo(50000, "Desayuno");
        folio.deshacer();
        assertEquals(0, folio.calcularSaldo(), 0.001);
        assertEquals(2, numeroMovimientos());          // nada se borra
        assertEquals(0, folio.totalPago(), 0.001);     // el reverso no cuenta como pago real
    }

    @Test
    void deshacerUnPagoLoCompensaConUnCargo() {
        folio.registrarCargo(100000, "Alojamiento");
        folio.registrarPago(30000, "Anticipo");
        folio.deshacer();
        assertEquals(100000, folio.calcularSaldo(), 0.001);
        assertEquals(0, folio.totalPago(), 0.001);     // el reverso de un pago resta
        assertEquals(3, numeroMovimientos());
    }

    @Test
    void deshacerVariasVecesRevierteEnOrdenInverso() {
        folio.registrarCargo(100000, "Alojamiento");
        folio.registrarPago(30000, "Anticipo");
        folio.deshacer();                               // revierte el pago
        assertEquals("Alojamiento", folio.ultimaOperacion().getDescripcion());
        folio.deshacer();                               // revierte el cargo
        assertEquals(0, folio.calcularSaldo(), 0.001);
        assertNull(folio.ultimaOperacion());
        assertEquals(4, numeroMovimientos());
    }

    @Test
    void deshacerSinOperacionesLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> folio.deshacer());
    }

    @Test
    void noSePuedeDeshacerMasVecesQueOperaciones() {
        folio.registrarCargo(1000, "x");
        folio.deshacer();
        assertThrows(EstructuraVaciaException.class, () -> folio.deshacer());
    }

    @Test
    void elReversoNoSeApilaYNoCreaBucle() {
        folio.registrarCargo(1000, "x");
        folio.deshacer();
        assertNull(folio.ultimaOperacion());
    }

    @Test
    void ultimaOperacionEsLaMasReciente() {
        folio.registrarCargo(100, "a");
        folio.registrarPago(50, "b");
        assertEquals("b", folio.ultimaOperacion().getDescripcion());
    }

    @Test
    void ultimaOperacionNoLaQuitaDeLaPila() {
        folio.registrarCargo(100, "a");
        folio.ultimaOperacion();
        folio.ultimaOperacion();
        assertNotNull(folio.ultimaOperacion());
    }

    // ----------------------------------------------- escenario de cancelación

    @Test
    void cancelacionCierraEnCero() {
        // lo que hace ServicioReservas.cancelar: cargo, anticipo, devolución y anulación
        folio.registrarCargo(320000, "Alojamiento");
        folio.registrarPago(96000, "Anticipo");
        folio.registrarCargo(96000, "Devolución por cancelación");
        double saldo = folio.calcularSaldo();
        folio.registrarPago(saldo, "Anulación de cargos");
        assertEquals(0, folio.calcularSaldo(), 0.001);
    }
}
