package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConsultaEntradasReservas {

    public static final LocalTime HORA_POR_DEFECTO = LocalTime.of(15, 0);

    private final Supplier<Iterable<Reserva>> reservas;
    private final Supplier<LocalDate> hoy;
    private final Function<Reserva, LocalTime> horaLlegada;

    public ConsultaEntradasReservas(Supplier<Iterable<Reserva>> reservas, Supplier<LocalDate> hoy,
                                    Function<Reserva, LocalTime> horaLlegada) {
        this.reservas = reservas;
        this.hoy = hoy;
        this.horaLlegada = horaLlegada;
    }

    public ConsultaEntradasReservas(Supplier<Iterable<Reserva>> reservas, Supplier<LocalDate> hoy) {
        this(reservas, hoy, r -> HORA_POR_DEFECTO);
    }


    public LocalDateTime proximaEntradaHoy(Apartamento apartamento) {
        LocalDate dia = hoy.get();
        LocalDateTime mejor = null;
        for (Reserva r : reservas.get()) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA
                    && dia.equals(r.getFechaInicio())
                    && r.getApartamento().getCodigo().equalsIgnoreCase(apartamento.getCodigo())) {
                LocalTime h = horaLlegada.apply(r);
                LocalDateTime t = dia.atTime(h == null ? HORA_POR_DEFECTO : h);
                if (mejor == null || t.isBefore(mejor)) {
                    mejor = t;
                }
            }
        }
        return mejor;
    }
}
