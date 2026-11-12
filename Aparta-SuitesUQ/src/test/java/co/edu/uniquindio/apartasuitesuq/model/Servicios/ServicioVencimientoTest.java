package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioVencimientoTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 8, 8, 0);
    private ServicioVencimiento servicio;
    private Reserva vieja;
    private Reserva nueva;

    private Reserva reserva(String codigo, EstadoReserva estado) {
        Apartamento a = new Apartamento("A101", "S", "A", 1, 1, 2, 100000);
        Huesped h = new Huesped("1", "Ana", "300", "a@x.co");
        return new Reserva(codigo, h, a, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3),
                estado, 0, Canal.values()[0]);
    }

    @BeforeEach
    void preparar() {
        servicio = new ServicioVencimiento(r -> r.cambiarEstado(EstadoReserva.CANCELADA));
        vieja = reserva("R7", EstadoReserva.PENDIENTE);
        nueva = reserva("R8", EstadoReserva.PENDIENTE);
        servicio.registrar(vieja, T0);
        servicio.registrar(nueva, T0.plusHours(20));
    }

    @Test
    void cancelaSoloLasQueSuperaronLas24Horas() {
        ListaDoble<Reserva> canceladas = servicio.procesarVencimientos(T0.plusHours(25));
        assertEquals(EstadoReserva.CANCELADA, vieja.getEstado());
        assertEquals(EstadoReserva.PENDIENTE, nueva.getEstado());
        assertSame(vieja, canceladas.obtener(0));
    }

    @Test
    void exactamente24HorasYaVence() {
        assertFalse(servicio.estaVencida(vieja, T0.plusHours(24).minusSeconds(1)));
        assertTrue(servicio.estaVencida(vieja, T0.plusHours(24)));
    }

    @Test
    void confirmadaNoVence() {
        vieja.cambiarEstado(EstadoReserva.CONFIRMADA);
        nueva.cambiarEstado(EstadoReserva.CONFIRMADA);
        assertTrue(servicio.procesarVencimientos(T0.plusDays(5)).estaVacia());
        assertEquals(EstadoReserva.CONFIRMADA, vieja.getEstado());
    }

    @Test
    void noCancelaDosVeces() {
        assertEquals(1, contar(servicio.procesarVencimientos(T0.plusHours(25))));
        assertEquals(0, contar(servicio.procesarVencimientos(T0.plusHours(26))));
    }

    @Test
    void tiempoRestanteEsNegativoCuandoYaVencio() {
        assertEquals(Duration.ofHours(4), servicio.tiempoRestante(vieja, T0.plusHours(20)));
        assertTrue(servicio.tiempoRestante(vieja, T0.plusHours(30)).isNegative());
        assertNull(servicio.tiempoRestante(reserva("R9", EstadoReserva.PENDIENTE), T0));
    }

    private int contar(ListaDoble<Reserva> lista) {
        int n = 0;
        for (Reserva r : lista) {
            n++;
        }
        return n;
    }

}