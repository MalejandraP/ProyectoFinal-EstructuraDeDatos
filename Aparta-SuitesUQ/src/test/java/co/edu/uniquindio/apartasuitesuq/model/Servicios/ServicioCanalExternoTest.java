package co.edu.uniquindio.apartasuitesuq.model.Servicios;
import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ServicioCanalExternoTest {

    private ServicioReservas servicioReservas;
    private ServicioCanalExterno canal;
    private Huesped titular;
    private LocalDate hoy;

    // AJUSTA este constructor a tu clase Apartamento
    private Apartamento apto(String codigo, int capacidad) {
        return new Apartamento(codigo, "Las Heliconias", "BloqueA", 1, 1, capacidad, 130000);
    }

    private SolicitudExterna solicitud(String id, Apartamento a, int desde, int hasta, int personas) {
        return new SolicitudExterna(id, a, hoy.plusDays(desde), hoy.plusDays(hasta), personas, titular);
    }

    @BeforeEach
    void preparar() {
        servicioReservas = new ServicioReservas();
        canal = new ServicioCanalExterno(servicioReservas);
        titular = new Huesped("1094000001", "Ana Gómez", "3001234567",
                "ana@correo.co", LocalDate.of(1990, 5, 20));
        hoy = LocalDate.now();
    }

    // ------------------------------------------------------------ recibir

    @Test
    void recibirSolicitudNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> canal.recibirSolicitud(null));
    }

    @Test
    void recibirEncolaYMarcaPendientes() {
        assertFalse(canal.hayPendientes());
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), 5, 7, 2));
        assertTrue(canal.hayPendientes());
    }

    @Test
    void getPendientesRecorreEnOrdenDeLlegada() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 7, 2));
        canal.recibirSolicitud(solicitud("E2", a, 10, 12, 2));
        StringBuilder ids = new StringBuilder();
        for (SolicitudExterna s : canal.getPendientes()) ids.append(s.getIdExterno()).append(" ");
        assertEquals("E1 E2 ", ids.toString());
    }

    // --------------------------------------------------------- cola vacía

    @Test
    void procesarSinSolicitudesLanzaExcepcion() {
        assertThrows(EstructuraVaciaException.class, () -> canal.procesarSolicitud());
        assertThrows(EstructuraVaciaException.class, () -> canal.procesarSiguiente());
    }

    // ----------------------------------------------------------- aceptada

    @Test
    void solicitudValidaSeAceptaYCreaReservaExterna() {
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), 5, 7, 2));
        SolicitudExterna s = canal.procesarSolicitud();

        assertEquals(EstadoSolicitud.ACEPTADA, s.getEstado());
        assertNotNull(s.getReservaCreada());
        assertEquals(Canal.EXTERNO, s.getReservaCreada().getCanal());
        assertEquals(EstadoReserva.CONFIRMADA, s.getReservaCreada().getEstado());
        assertEquals(1, canal.getProcesadas().tamanio());
        assertTrue(canal.getConflictos().estaVacia());
        assertFalse(canal.hayPendientes());
    }

    @Test
    void procesarSiguienteDevuelveLaReservaSiFueAceptada() {
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), 5, 7, 2));
        Reserva r = canal.procesarSiguiente();
        assertNotNull(r);
        assertEquals(Canal.EXTERNO, r.getCanal());
    }

    @Test
    void mismasFechasEnApartamentosDistintosSeAceptanAmbas() {
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), 5, 7, 2));
        canal.recibirSolicitud(solicitud("E2", apto("A2", 4), 5, 7, 2));
        assertEquals(EstadoSolicitud.ACEPTADA, canal.procesarSolicitud().getEstado());
        assertEquals(EstadoSolicitud.ACEPTADA, canal.procesarSolicitud().getEstado());
    }

    @Test
    void laNocheDeSalidaNoChocaConLaSiguienteEntrada() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 7, 2));
        canal.recibirSolicitud(solicitud("E2", a, 7, 9, 2));   // entra el día que sale la otra
        assertEquals(EstadoSolicitud.ACEPTADA, canal.procesarSolicitud().getEstado());
        assertEquals(EstadoSolicitud.ACEPTADA, canal.procesarSolicitud().getEstado());
        assertTrue(canal.getConflictos().estaVacia());
    }

    // ------------------------------------------------ rechazo por choque

    @Test
    void solicitudQueSolapaSeRechazaYQuedaEnConflictos() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 8, 2));
        canal.recibirSolicitud(solicitud("E2", a, 7, 10, 2));   // comparte la noche del 7
        canal.procesarSolicitud();
        SolicitudExterna segunda = canal.procesarSolicitud();

        assertEquals(EstadoSolicitud.RECHAZADA, segunda.getEstado());
        assertNotNull(segunda.getMotivoRechazo());
        assertNull(segunda.getReservaCreada());
        assertEquals(1, canal.getConflictos().tamanio());
        assertSame(segunda, canal.getConflictos().obtener(0).getSolicitud());
        assertEquals(2, canal.getProcesadas().tamanio());
    }

    @Test
    void procesarSiguienteDevuelveNullSiFueRechazada() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 8, 2));
        canal.recibirSolicitud(solicitud("E2", a, 6, 9, 2));
        assertNotNull(canal.procesarSiguiente());
        assertNull(canal.procesarSiguiente());
    }

    // --------------------------------- rechazos que NO son conflicto de canal

    @Test
    void capacidadExcedidaSeRechazaSinConflicto() {
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), 5, 7, 6));
        SolicitudExterna s = canal.procesarSolicitud();
        assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
        assertNotNull(s.getMotivoRechazo());
        assertTrue(canal.getConflictos().estaVacia());
        assertEquals(1, canal.getProcesadas().tamanio());
    }

    @Test
    void fechasInvalidasSeRechazanSinConflicto() {
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), 7, 5, 2));   // salida antes de entrada
        SolicitudExterna s = canal.procesarSolicitud();
        assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
        assertTrue(canal.getConflictos().estaVacia());
    }

    @Test
    void fechaEnElPasadoSeRechazaSinConflicto() {
        canal.recibirSolicitud(solicitud("E1", apto("A1", 4), -5, -3, 2));
        SolicitudExterna s = canal.procesarSolicitud();
        assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
        assertTrue(canal.getConflictos().estaVacia());
    }

    @Test
    void solicitudConDatosNulosNoSePierde() {
        canal.recibirSolicitud(new SolicitudExterna("E1", null, hoy.plusDays(5), hoy.plusDays(7), 2, titular));
        SolicitudExterna s = canal.procesarSolicitud();
        assertEquals(EstadoSolicitud.RECHAZADA, s.getEstado());
        assertEquals(1, canal.getProcesadas().tamanio());
        assertFalse(canal.hayPendientes());
    }

    @Test
    void unaRechazadaNoDetieneLasSiguientes() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 7, 9));    // capacidad
        canal.recibirSolicitud(solicitud("E2", a, 5, 7, 2));    // válida
        assertEquals(EstadoSolicitud.RECHAZADA, canal.procesarSolicitud().getEstado());
        assertEquals(EstadoSolicitud.ACEPTADA, canal.procesarSolicitud().getEstado());
    }

    // ------------------------------------------------------------- orden

    @Test
    void seProcesaEnOrdenDeLlegada() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 7, 2));
        canal.recibirSolicitud(solicitud("E2", a, 10, 12, 2));
        canal.recibirSolicitud(solicitud("E3", a, 15, 17, 2));
        assertEquals("E1", canal.procesarSolicitud().getIdExterno());
        assertEquals("E2", canal.procesarSolicitud().getIdExterno());
        assertEquals("E3", canal.procesarSolicitud().getIdExterno());
        assertEquals("E1", canal.getProcesadas().obtener(0).getIdExterno());
    }

    @Test
    void elPrimeroEnLlegarGanaElChoque() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 8, 2));
        canal.recibirSolicitud(solicitud("E2", a, 5, 8, 2));
        assertEquals(EstadoSolicitud.ACEPTADA, canal.procesarSolicitud().getEstado());
        assertEquals(EstadoSolicitud.RECHAZADA, canal.procesarSolicitud().getEstado());
    }

    // ------------------------------------------------------ procesar todas

    @Test
    void procesarTodasVaciaLaColaYCuenta() {
        Apartamento a = apto("A1", 4);
        canal.recibirSolicitud(solicitud("E1", a, 5, 7, 2));
        canal.recibirSolicitud(solicitud("E2", a, 5, 7, 2));
        canal.recibirSolicitud(solicitud("E3", a, 20, 22, 9));
        assertEquals(3, canal.procesarTodas());
        assertFalse(canal.hayPendientes());
        assertEquals(3, canal.getProcesadas().tamanio());
        assertEquals(1, canal.getConflictos().tamanio());
    }

    @Test
    void procesarTodasConColaVaciaDevuelveCero() {
        assertEquals(0, canal.procesarTodas());
    }
}
