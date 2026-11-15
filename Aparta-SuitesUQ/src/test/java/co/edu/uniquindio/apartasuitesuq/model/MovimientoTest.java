package co.edu.uniquindio.apartasuitesuq.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoTest {

    @Test
    void reversoDeUnCargoEsUnPago() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 50000, "Desayuno");
        Movimiento reverso = cargo.crearReverso();
        assertEquals(TipoMovimiento.PAGO, reverso.getTipo());
    }

    @Test
    void reversoDeUnPagoEsUnCargo() {
        Movimiento pago = new Movimiento(TipoMovimiento.PAGO, 96000, "Anticipo");
        Movimiento reverso = pago.crearReverso();
        assertEquals(TipoMovimiento.CARGO, reverso.getTipo());
    }

    @Test
    void reversoConservaElMonto() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 123456.5, "x");
        assertEquals(123456.5, cargo.crearReverso().getMonto(), 0.001);
    }

    @Test
    void reversoQuedaMarcadoComoReverso() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "x");
        assertTrue(cargo.crearReverso().isEsReverso());
    }

    @Test
    void movimientoOriginalNoEsReverso() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "x");
        assertFalse(cargo.isEsReverso());
    }

    @Test
    void descripcionDelReversoLlevaElPrefijo() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "Desayuno");
        assertEquals("Reverso: Desayuno", cargo.crearReverso().getDescripcion());
    }

    @Test
    void crearReversoNoModificaElOriginal() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "Desayuno");
        cargo.crearReverso();
        assertEquals(TipoMovimiento.CARGO, cargo.getTipo());
        assertEquals(1000, cargo.getMonto(), 0.001);
        assertEquals("Desayuno", cargo.getDescripcion());
        assertFalse(cargo.isEsReverso());
    }

    @Test
    void reversoEsUnObjetoDistinto() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "x");
        assertNotSame(cargo, cargo.crearReverso());
    }

    @Test
    void reversoTieneFechaPropiaNoAnteriorALaDelOriginal() {
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "x");
        Movimiento reverso = cargo.crearReverso();
        assertFalse(reverso.getFechayHora().isBefore(cargo.getFechayHora()));
    }

    @Test
    void reversoDelReversoVuelveAlTipoOriginal() {
        // el sistema no lo hace (el reverso no se apila), pero el método debe ser coherente
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, "x");
        Movimiento doble = cargo.crearReverso().crearReverso();
        assertEquals(TipoMovimiento.CARGO, doble.getTipo());
        assertEquals(1000, doble.getMonto(), 0.001);
        assertEquals("Reverso: Reverso: x", doble.getDescripcion());
    }

    @Test
    void reversoConDescripcionNulaNoFalla() {
        // "Reverso: null": documenta el comportamiento actual
        Movimiento cargo = new Movimiento(TipoMovimiento.CARGO, 1000, null);
        assertEquals("Reverso: null", cargo.crearReverso().getDescripcion());
    }
}