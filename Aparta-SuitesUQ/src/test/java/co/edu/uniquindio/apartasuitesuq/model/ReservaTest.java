package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private static final LocalDate ENTRADA = LocalDate.of(2026, 12, 10);
    private static final LocalDate SALIDA = LocalDate.of(2026, 12, 13);
    private static final LocalDateTime CREACION = LocalDateTime.of(2026, 11, 12, 10, 0);

    private Apartamento apartamento;
    private Huesped titular;

    @BeforeEach
    void preparar() {
        apartamento = new Apartamento("A101", "Suite Cafetal", "A", 1, 2, 3, 100000);
        titular = new Huesped("1094", "Ana Gómez", LocalDate.of(1990, 5, 20),
                "3001234567", "ana@correo.co", null);
    }

    private Reserva nueva(String codigo, LocalDate entrada, int personas) {
        return new Reserva(codigo, titular, apartamento, entrada, entrada.plusDays(3),
                EstadoReserva.PENDIENTE, 0, Canal.PORTAL, personas, CREACION);
    }

    @Test
    void elTitularConFechaDeNacimientoQuedaComoOcupante() {
        Reserva r = nueva("R1", ENTRADA, 2);
        assertEquals(1, r.contarOcupantesRegistrados());
        assertEquals(2, r.contarPagantesEn(ENTRADA), "la persona sin datos se cobra como adulta");
    }

    @Test
    void rechazaFechasYCapacidadInvalidas() {
        assertThrows(FechasInvalidasException.class, () -> new Reserva("R1", titular, apartamento,
                SALIDA, ENTRADA, EstadoReserva.PENDIENTE, 0, Canal.DIRECTA));
        assertThrows(CapacidadExcedidaException.class, () -> nueva("R1", ENTRADA, 4));
    }

    @Test
    void noSePuedenRegistrarMasOcupantesQuePersonas() {
        Reserva r = nueva("R1", ENTRADA, 2);
        r.agregarOcupante(new Ocupante("Tomás", LocalDate.of(2022, 3, 1)));
        assertThrows(CapacidadExcedidaException.class,
                () -> r.agregarOcupante(new Ocupante("Otro", LocalDate.of(2000, 1, 1))));
    }

    @Test
    void cicloDeVidaValido() {
        Reserva r = nueva("R1", ENTRADA, 1);
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
        r.cambiarEstado(EstadoReserva.ENCURSO);
        r.cambiarEstado(EstadoReserva.FINALIZADA);
        assertFalse(r.estaActiva());
    }

    @Test
    void transicionesInvalidasLanzanExcepcion() {
        Reserva r = nueva("R1", ENTRADA, 1);
        assertThrows(TransicionEstadoInvalidaException.class, () -> r.cambiarEstado(EstadoReserva.ENCURSO));
        r.cancelarReserva();
        assertThrows(TransicionEstadoInvalidaException.class, () -> r.cambiarEstado(EstadoReserva.CONFIRMADA));
    }

    @Test
    void venceA_las24HorasSiSigueSinConfirmar() {
        Reserva r = nueva("R1", ENTRADA, 1);
        assertFalse(r.estaVencida(CREACION.plusHours(23)));
        assertTrue(r.estaVencida(CREACION.plusHours(25)));
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
        assertFalse(r.estaVencida(CREACION.plusHours(25)));
    }

    @Test
    void solapamientoYNochesOcupadas() {
        Reserva r = nueva("R1", ENTRADA, 1);
        assertTrue(r.ocupaNoche(ENTRADA));
        assertFalse(r.ocupaNoche(SALIDA));
        assertFalse(r.seSolapaCon(SALIDA, SALIDA.plusDays(1)));
        assertTrue(r.seSolapaCon(ENTRADA.minusDays(2), ENTRADA.plusDays(1)));
        assertEquals(3, r.getEstancia().getNumeroNoches());
    }

    @Test
    void ordenNaturalPorFechaDeEntradaYLuegoPorCodigo() {
        Reserva temprana = nueva("R9", ENTRADA, 1);
        Reserva tardia = nueva("R1", ENTRADA.plusDays(5), 1);
        Reserva mismoDia = nueva("R2", ENTRADA, 1);
        assertTrue(temprana.compareTo(tardia) < 0);
        assertTrue(mismoDia.compareTo(temprana) < 0, "mismo día: desempata por código");
        assertNotEquals(0, mismoDia.compareTo(temprana));
    }

    @Test
    void anticipoDelTreintaPorCiento() {
        Reserva r = nueva("R1", ENTRADA, 1);
        r.setCostoTotal(1_000_000);
        assertEquals(300_000, r.getValorAnticipo(), 0.001);
    }
}
