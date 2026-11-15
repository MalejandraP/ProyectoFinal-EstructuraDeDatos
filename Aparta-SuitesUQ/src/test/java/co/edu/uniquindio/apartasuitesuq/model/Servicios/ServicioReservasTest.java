package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Canal;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.ReservaSolapadaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de {@link ServicioReservas}: ciclo de vida, solapamientos
 * en el árbol del apartamento, anticipos y cancelación con devolución.
 */
class ServicioReservasTest {

    private ServicioReservas servicio;
    private Apartamento apto;
    private Huesped titular;
    private LocalDate base;

    @BeforeEach
    void setUp() {
        servicio = new ServicioReservas();
        apto = new Apartamento("A101", "Suite Cafetal", "Bloque A", 1, 2, 4, 200_000.0);
        titular = new Huesped("1094000001", "Ana Gómez", "3101112233", "ana@correo.com", LocalDate.of(1990, 5, 10));
        base = LocalDate.now().plusDays(10);
    }

    @Test
    void crearReservaPorPortalIniciaPendienteYPorDirectaIniciaConfirmada() {
        Reserva portal = servicio.crearReserva(titular, apto, base, base.plusDays(2), 2, Canal.PORTAL);
        assertEquals(EstadoReserva.PENDIENTE, portal.getEstado());
        assertEquals("RES-1", portal.getCodigoReserva());

        Reserva directa = servicio.crearReserva(titular, apto, base.plusDays(5), base.plusDays(7), 2, Canal.DIRECTA);
        assertEquals(EstadoReserva.CONFIRMADA, directa.getEstado());
        assertEquals("RES-2", directa.getCodigoReserva());
    }

    @Test
    void rechazaFechasPasadasYCapacidadExcedida() {
        assertThrows(FechasInvalidasException.class, () ->
                servicio.crearReserva(titular, apto, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), 2, Canal.DIRECTA));

        assertThrows(CapacidadExcedidaException.class, () ->
                servicio.crearReserva(titular, apto, base, base.plusDays(2), 5, Canal.DIRECTA));

        assertThrows(CapacidadExcedidaException.class, () ->
                servicio.crearReserva(titular, apto, base, base.plusDays(2), 0, Canal.DIRECTA));
    }

    @Test
    void detectaSolapamientosYPermiteReservaConsecutivaElDiaDeSalida() {
        // Ocupa noches del día +10 y +11 (salida +12)
        servicio.crearReserva(titular, apto, base, base.plusDays(2), 2, Canal.DIRECTA);

        // Solapamiento parcial (día +11 a +13) debe fallar
        assertThrows(ReservaSolapadaException.class, () ->
                servicio.crearReserva(titular, apto, base.plusDays(1), base.plusDays(3), 2, Canal.PORTAL));

        // Reserva consecutiva que entra el mismo día de salida (+12 a +14) debe permitirse
        Reserva consecutiva = servicio.crearReserva(titular, apto, base.plusDays(2), base.plusDays(4), 2, Canal.PORTAL);
        assertNotNull(consecutiva);
    }

    @Test
    void pagarAnticipoConfirmaReservaPendienteYRechazaSiNoEstaPendiente() {
        Reserva portal = servicio.crearReserva(titular, apto, base, base.plusDays(3), 2, Canal.PORTAL);
        double pagado = servicio.pagarAnticipo(portal);

        assertEquals(portal.getValorAnticipo(), pagado, 0.01);
        assertEquals(EstadoReserva.CONFIRMADA, portal.getEstado());

        // Volver a pagar anticipo sobre una ya confirmada debe lanzar TransicionEstadoInvalidaException
        assertThrows(TransicionEstadoInvalidaException.class, () -> servicio.pagarAnticipo(portal));
    }

    @Test
    void cancelarConMasDe48HorasDevuelveAnticipoPagadoYLiberaNoches() {
        Reserva portal = servicio.crearReserva(titular, apto, base, base.plusDays(2), 2, Canal.PORTAL);
        servicio.pagarAnticipo(portal);
        double anticipo = portal.getValorAnticipo();

        double devuelto = servicio.cancelar(portal);
        assertEquals(anticipo, devuelto, 0.01);
        assertEquals(EstadoReserva.CANCELADA, portal.getEstado());
        assertEquals(0.0, portal.getFolio().calcularSaldo(), 0.01);

        // Las fechas liberadas pueden volver a reservarse
        Reserva nueva = servicio.crearReserva(titular, apto, base, base.plusDays(2), 2, Canal.DIRECTA);
        assertEquals(EstadoReserva.CONFIRMADA, nueva.getEstado());

        // Cancelar una reserva ya cancelada lanza excepción
        assertThrows(TransicionEstadoInvalidaException.class, () -> servicio.cancelar(portal));
    }

    @Test
    void cancelarCon48HorasOMenosNoGeneraDevolucion() {
        LocalDate manana = LocalDate.now().plusDays(1);
        Reserva cercana = servicio.crearReserva(titular, apto, manana, manana.plusDays(2), 2, Canal.PORTAL);
        servicio.pagarAnticipo(cercana);

        assertEquals(0.0, servicio.calcularDevolucion(cercana), 0.01);
        assertEquals(0.0, servicio.cancelar(cercana), 0.01);
        assertEquals(EstadoReserva.CANCELADA, cercana.getEstado());
    }

    @Test
    void cicloDeVidaCompletoYListadoOrdenadoPorFecha() {
        Reserva tardia = servicio.crearReserva(titular, apto, base.plusDays(10), base.plusDays(12), 2, Canal.DIRECTA);
        Reserva temprana = servicio.crearReserva(titular, apto, base, base.plusDays(2), 2, Canal.DIRECTA);

        // Listado ordenado por fecha de entrada (inorden del ABB)
        List<String> codigos = new ArrayList<>();
        for (Reserva r : servicio.listarTodasOrdenadas()) {
            codigos.add(r.getCodigoReserva());
        }
        assertEquals(List.of(temprana.getCodigoReserva(), tardia.getCodigoReserva()), codigos);

        // Transiciones válidas: CONFIRMADA -> ENCURSO -> FINALIZADA
        servicio.iniciarEstancia(temprana);
        assertEquals(EstadoReserva.ENCURSO, temprana.getEstado());

        servicio.finalizar(temprana);
        assertEquals(EstadoReserva.FINALIZADA, temprana.getEstado());
        assertEquals(temprana, servicio.buscarPorCodigo(temprana.getCodigoReserva()));
    }
}