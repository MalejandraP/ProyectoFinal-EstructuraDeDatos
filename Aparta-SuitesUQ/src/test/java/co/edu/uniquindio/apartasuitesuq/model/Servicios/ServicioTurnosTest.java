package co.edu.uniquindio.apartasuitesuq.model.Servicios;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import co.edu.uniquindio.apartasuitesuq.model.Recepcionista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioTurnosTest {

    private ServicioTurnos servicio;
    private Recepcionista laura, andres, valentina;

    private Recepcionista rec(String id, String nombre) {
        return new Recepcionista(id, nombre, id.toLowerCase());
    }

    @BeforeEach
    void preparar() {
        servicio = new ServicioTurnos();
        laura = rec("R1", "Laura Pérez");
        andres = rec("R2", "Andrés Castaño");
        valentina = rec("R3", "Valentina Ospina");
    }

    @Test
    void sinRecepcionistasNoHayTurno() {
        assertFalse(servicio.hayRecepcionistas());
        assertThrows(EstructuraVaciaException.class, () -> servicio.turnoActual());
        assertThrows(EstructuraVaciaException.class, () -> servicio.pasarTurno());
    }

    @Test
    void elPrimeroRegistradoTieneElTurno() {
        assertTrue(servicio.registrar(laura));
        servicio.registrar(andres);
        assertTrue(servicio.hayRecepcionistas());
        assertSame(laura, servicio.turnoActual());
    }

    @Test
    void noSeRegistraDosVecesElMismo() {
        assertTrue(servicio.registrar(laura));
        assertFalse(servicio.registrar(laura));
        assertFalse(servicio.registrar(rec("R1", "Laura Pérez")));   // requiere equals por id
    }

    @Test
    void pasarTurnoRotaYDaLaVuelta() {
        servicio.registrar(laura);
        servicio.registrar(andres);
        servicio.registrar(valentina);
        assertSame(andres, servicio.pasarTurno());
        assertSame(valentina, servicio.pasarTurno());
        assertSame(laura, servicio.pasarTurno());
    }

    @Test
    void conUnoSoloPasarTurnoSeQuedaEnEl() {
        servicio.registrar(laura);
        assertSame(laura, servicio.pasarTurno());
    }

    @Test
    void retirarUnoDelMedioNoRompeLaRotacion() {
        servicio.registrar(laura);
        servicio.registrar(andres);
        servicio.registrar(valentina);
        assertTrue(servicio.retirar(andres));
        assertSame(valentina, servicio.pasarTurno());
        assertSame(laura, servicio.pasarTurno());
    }

    @Test
    void retirarElQueTieneElTurnoPasaAlSiguiente() {
        servicio.registrar(laura);
        servicio.registrar(andres);
        servicio.retirar(laura);
        assertSame(andres, servicio.turnoActual());
    }

    @Test
    void retirarInexistenteDevuelveFalse() {
        servicio.registrar(laura);
        assertFalse(servicio.retirar(andres));
        assertSame(laura, servicio.turnoActual());
    }

    @Test
    void retirarEnVacioLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> servicio.retirar(laura));
    }

    @Test
    void retirarATodosDejaElServicioVacioYReutilizable() {
        servicio.registrar(laura);
        servicio.retirar(laura);
        assertFalse(servicio.hayRecepcionistas());
        servicio.registrar(andres);
        assertSame(andres, servicio.turnoActual());
    }

    @Test
    void getTurnosExponeLaListaCircular() {
        servicio.registrar(laura);
        servicio.registrar(andres);
        int n = 0;
        for (Recepcionista r : servicio.getTurnos()) n++;
        assertEquals(2, n);
    }
}
