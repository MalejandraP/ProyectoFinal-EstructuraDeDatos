package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Grafo;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioRecepcionTest {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 9);
    private ListaDoble<Reserva> reservas;
    private ServicioPreparacion preparacion;
    private ServicioRecepcion recepcion;
    private Apartamento a101;
    private Apartamento a102;
    private Reserva r1;
    private Reserva r2;

    private Reserva reserva(String cod, Apartamento a, LocalDate entrada, EstadoReserva e) {
        return new Reserva(cod, new Huesped("1", "Ana", "300", "a@x.co"), a, entrada, entrada.plusDays(2), e, 0, Canal.DIRECTA);
    }

    @BeforeEach
    void preparar() {
        a101 = new Apartamento("A101", "S1", "A", 1, 1, 2, 100000);
        a102 = new Apartamento("A102", "S2", "A", 1, 1, 2, 100000);
        r1 = reserva("R1", a101, HOY, EstadoReserva.CONFIRMADA);
        r2 = reserva("R2", a102, HOY.plusDays(1), EstadoReserva.CONFIRMADA);
        reservas = new ListaDoble<>();
        reservas.agregarFinal(r1);
        reservas.agregarFinal(r2);
        Grafo<String> mapa = new Grafo<>();
        mapa.agregarNodo("REC");
        ColaPreparacion cola = new ColaPreparacion(new ConsultaEntradasReservas(() -> reservas, () -> HOY));
        preparacion = new ServicioPreparacion(mapa, "REC", cola);
        recepcion = new ServicioRecepcion(preparacion);
    }

    @Test
    void checkInMueveReservaYApartamento() {
        recepcion.checkIn(r1, HOY);
        assertEquals(EstadoReserva.ENCURSO, r1.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, a101.getEstado());
    }

    @Test
    void checkInRechazadoNoCambiaNada() {
        assertThrows(TransicionEstadoInvalidaException.class, () -> recepcion.checkIn(r2, HOY)); // entra mañana
        a101.setEstado(EstadoApartamento.PENDIENTEDEPREPARACION);
        assertThrows(TransicionEstadoInvalidaException.class, () -> recepcion.checkIn(r1, HOY));
        assertEquals(EstadoReserva.CONFIRMADA, r1.getEstado());
        assertEquals(EstadoApartamento.PENDIENTEDEPREPARACION, a101.getEstado());
    }

    @Test
    void checkOutFinalizaYEncolaConPrioridad() {
        recepcion.checkIn(r1, HOY);
        recepcion.checkOut(r1, HOY.atTime(11, 0));
        assertEquals(EstadoReserva.FINALIZADA, r1.getEstado());
        assertEquals(EstadoApartamento.PENDIENTEDEPREPARACION, a101.getEstado());
        assertSame(a101, preparacion.iniciarSiguiente().getApartamento());
    }

    @Test
    void checkOutSinCheckInEsInvalido() {
        assertThrows(TransicionEstadoInvalidaException.class, () -> recepcion.checkOut(r1, HOY.atTime(11, 0)));
    }

    @Test
    void consultaDevuelveLaHoraDeHoyOCero() {
        ConsultaEntradasReservas c = new ConsultaEntradasReservas(() -> reservas, () -> HOY, r -> LocalTime.of(14, 0));
        assertEquals(LocalDateTime.of(2026, 11, 9, 14, 0), c.proximaEntradaHoy(a101));
        assertNull(c.proximaEntradaHoy(a102));  // su reserva es mañana
        r1.cambiarEstado(EstadoReserva.CANCELADA);
        assertNull(c.proximaEntradaHoy(a101));  // cancelada no cuenta
    }

}