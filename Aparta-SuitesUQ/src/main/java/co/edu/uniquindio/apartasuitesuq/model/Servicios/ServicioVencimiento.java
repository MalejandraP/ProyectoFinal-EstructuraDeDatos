package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ColaPrioridad;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

public class ServicioVencimiento {

    public static final Duration PLAZO = Duration.ofHours(24);

    @FunctionalInterface
    public interface GestorReservas {
        void cancelarPorVencimiento(Reserva reserva);
    }

    private static class Seguimiento {
        final Reserva reserva;
        final LocalDateTime vence;

        Seguimiento(Reserva reserva, LocalDateTime creada) {
            this.reserva = reserva;
            this.vence = creada.plus(PLAZO);
        }
    }

    private final GestorReservas gestor;
    private final ColaPrioridad<Seguimiento> cola =
            new ColaPrioridad<>(Comparator.comparing((Seguimiento s) -> s.vence));

    public ServicioVencimiento(GestorReservas gestor) {
        this.gestor = gestor;
    }


    public void registrar(Reserva reserva, LocalDateTime creada) {
        if (reserva == null || creada == null) {
            throw new IllegalArgumentException("Reserva y hora de creación son obligatorias");
        }
        cola.encolar(new Seguimiento(reserva, creada));
    }


    public ListaDoble<Reserva> procesarVencimientos(LocalDateTime ahora) {
        ListaDoble<Reserva> canceladas = new ListaDoble<>();
        while (!cola.estaVacia() && !cola.ver().vence.isAfter(ahora)) {
            Reserva r = cola.desencolar().reserva;
            if (r.getEstado() == EstadoReserva.PENDIENTE) { // si ya se confirmó o canceló, se ignora
                gestor.cancelarPorVencimiento(r);
                canceladas.agregarFinal(r);
            }
        }
        return canceladas;
    }

    public boolean estaVencida(Reserva reserva, LocalDateTime ahora) {
        for (Seguimiento s : cola) {
            if (s.reserva == reserva) {
                return reserva.getEstado() == EstadoReserva.PENDIENTE && !s.vence.isAfter(ahora);
            }
        }
        return false;
    }


    public Duration tiempoRestante(Reserva reserva, LocalDateTime ahora) {
        for (Seguimiento s : cola) {
            if (s.reserva == reserva) {
                return Duration.between(ahora, s.vence);
            }
        }
        return null;
    }

}
