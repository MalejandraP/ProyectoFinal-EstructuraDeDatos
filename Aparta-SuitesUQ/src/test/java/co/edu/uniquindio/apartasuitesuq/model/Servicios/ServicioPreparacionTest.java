package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Grafo;
import co.edu.uniquindio.apartasuitesuq.model.TransicionEstadoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioPreparacionTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 9, 10, 0);
    private ServicioPreparacion servicio;
    private Apartamento a101;
    private Apartamento a102;

    @BeforeEach
    void preparar() {
        Grafo<String> mapa = new Grafo<>();
        mapa.agregarNodo("REC");
        mapa.agregarNodo("A101");
        mapa.agregarNodo("A102");
        mapa.agregarPasillo("REC", "A101", 5);
        mapa.agregarPasillo("REC", "A102", 8);
        servicio = new ServicioPreparacion(mapa, "REC", new ColaPreparacion(ap -> null));
        a101 = new Apartamento("A101", "Suite 1", "A", 1, 2, 4, 100000);
        a102 = new Apartamento("A102", "Suite 2", "A", 1, 1, 2, 80000);
    }

    @Test
    void checkInSoloSiEstaPreparado() {
        servicio.checkIn(a101);
        assertEquals(EstadoApartamento.OCUPADO, a101.getEstado());
        assertThrows(TransicionEstadoInvalidaException.class, () -> servicio.checkIn(a101));
    }

    @Test
    void checkOutEncolaYSePrepara() {
        servicio.checkIn(a101);
        servicio.checkOut(a101, T0);
        assertEquals(EstadoApartamento.PENDIENTEDEPREPARACION, a101.getEstado());
        ColaPreparacion.Item it = servicio.iniciarSiguiente();
        assertEquals(EstadoApartamento.ENPREPARACION, a101.getEstado());
        servicio.terminarPreparacion(it, T0.plusMinutes(40));
        assertEquals(EstadoApartamento.PREPARADO, a101.getEstado());
        assertEquals(40.0, servicio.tiempoMedioPreparacionMin(), 0.001);
    }

    @Test
    void fueraDeServicioGuardaElMotivo() {
        assertNull(servicio.motivoFueraDeServicio(a102));
        servicio.ponerFueraDeServicio(a102, "  Fuga de agua  ", T0);
        assertEquals(EstadoApartamento.FUERADESERVICIO, a102.getEstado());
        assertEquals("Fuga de agua", servicio.motivoFueraDeServicio(a102));
    }

    @Test
    void fueraDeServicioExigeMotivoYNoPermiteOcupado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.ponerFueraDeServicio(a102, " "));
        servicio.checkIn(a101);
        assertThrows(TransicionEstadoInvalidaException.class, () -> servicio.ponerFueraDeServicio(a101, "x"));
    }

    @Test
    void volverAServicioCierraElMotivoYMantieneHistorial() {
        servicio.ponerFueraDeServicio(a102, "Pintura", T0);
        servicio.volverAServicio(a102, T0.plusHours(5));
        assertNull(servicio.motivoFueraDeServicio(a102));
        assertEquals(EstadoApartamento.PENDIENTEDEPREPARACION, a102.getEstado());
        int n = 0;
        for (RegistroFueraDeServicio.Registro r : servicio.historialFueraDeServicio()) {
            n++;
            assertEquals(T0.plusHours(5), r.getHasta());
        }
        assertEquals(1, n);
    }

}