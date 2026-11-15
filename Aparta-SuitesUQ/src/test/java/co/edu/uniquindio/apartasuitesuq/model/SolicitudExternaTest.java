package co.edu.uniquindio.apartasuitesuq.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudExternaTest {

    private SolicitudExterna solicitud;
    private LocalDate entrada;
    private LocalDate salida;

    @BeforeEach
    void preparar() {
        entrada = LocalDate.now().plusDays(5);
        salida = LocalDate.now().plusDays(7);
        solicitud = new SolicitudExterna("EXT-1", null, entrada, salida, 3, null);
    }

    // AJUSTA a tus constructores de Apartamento, Huesped y Reserva
    private Reserva crearReservaDePrueba() {
        Apartamento apto = new Apartamento("A1", "Las Heliconias", "BloqueA", 1, 1, 4, 130000);
        Huesped h = new Huesped("1094000001", "Ana Gómez", "3001234567",
                "ana@correo.co", LocalDate.of(1990, 5, 20));
        return new Reserva("RES-1", h, apto, entrada, salida,
                EstadoReserva.CONFIRMADA, 0, Canal.EXTERNO, 2, java.time.LocalDateTime.now());
    }

    // ------------------------------------------------------- estado inicial

    @Test
    void nuevaSolicitudNaceEnPendienteSinResultado() {
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertNull(solicitud.getMotivoRechazo());
        assertNull(solicitud.getReservaCreada());
    }

    @Test
    void conservaLosDatosDelConstructor() {
        assertEquals("EXT-1", solicitud.getIdExterno());
        assertEquals(entrada, solicitud.getEntrada());
        assertEquals(salida, solicitud.getSalida());
        assertEquals(3, solicitud.getNumPersonas());
        assertNull(solicitud.getApartamento());
        assertNull(solicitud.getTitular());
    }

    // ------------------------------------------------------------ aceptar

    @Test
    void marcarAceptadaCambiaElEstadoYGuardaLaReserva() {
        Reserva reserva = crearReservaDePrueba();
        solicitud.marcarAceptada(reserva);
        assertEquals(EstadoSolicitud.ACEPTADA, solicitud.getEstado());
        assertSame(reserva, solicitud.getReservaCreada());
    }

    @Test
    void aceptadaNoTieneMotivoDeRechazo() {
        solicitud.marcarAceptada(crearReservaDePrueba());
        assertNull(solicitud.getMotivoRechazo());
    }

    // ----------------------------------------------------------- rechazar

    @Test
    void marcarRechazadaCambiaElEstadoYGuardaElMotivo() {
        solicitud.marcarRechazada("El apartamento ya está reservado");
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
        assertEquals("El apartamento ya está reservado", solicitud.getMotivoRechazo());
    }

    @Test
    void rechazadaNoTieneReservaCreada() {
        solicitud.marcarRechazada("motivo");
        assertNull(solicitud.getReservaCreada());
    }

    @Test
    void rechazarConMotivoNuloNoFalla() {
        // documenta el comportamiento actual: ocurre cuando la excepción no trae mensaje
        solicitud.marcarRechazada(null);
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
        assertNull(solicitud.getMotivoRechazo());
    }

    // --------------------------------------------------- opcionales (blindaje)
    // Solo si lanzas IllegalStateException cuando el estado ya no es PENDIENTE.

    // @Test
    // void noSePuedeAceptarUnaSolicitudYaRechazada() {
    //     solicitud.marcarRechazada("motivo");
    //     assertThrows(IllegalStateException.class, () -> solicitud.marcarAceptada(crearReservaDePrueba()));
    // }
    //
    // @Test
    // void noSePuedeRechazarUnaSolicitudYaAceptada() {
    //     solicitud.marcarAceptada(crearReservaDePrueba());
    //     assertThrows(IllegalStateException.class, () -> solicitud.marcarRechazada("motivo"));
    // }
}