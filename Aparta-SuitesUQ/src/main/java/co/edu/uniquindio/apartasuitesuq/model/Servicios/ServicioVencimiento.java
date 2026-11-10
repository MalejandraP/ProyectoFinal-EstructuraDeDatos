package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ColaPrioridad;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Function;

public class ServicioVencimiento {

    @FunctionalInterface
    public interface GestorReservas {
        void cancelarPorVencimiento(Reserva reserva);
    }

    private final GestorReservas gestor;
    private final Function<Reserva, LocalDateTime> fechaCreacion;
    private final ColaPrioridad<Reserva> cola;

    public ServicioVencimiento(GestorReservas gestor, Function<Reserva, LocalDateTime> fechaCreacion) {
        this.gestor = gestor;
        this.fechaCreacion = fechaCreacion;
        this.cola = new ColaPrioridad<>(Comparator.comparing(this::vence));
    }

    private LocalDateTime vence(Reserva r) {
        return fechaCreacion.apply(r).plusHours(24);
    }

    /** Llamar cuando se crea una reserva por el portal (queda Pendiente). */
    public void registrar(Reserva reserva) { cola.encolar(reserva); }

    /** Cancela todas las vencidas. Devuelve cuántas canceló. Llamarlo con un Timer de la GUI. */
    public int procesarVencidas(LocalDateTime ahora) {
        int canceladas = 0;
        while (!cola.estaVacia() && !vence(cola.ver()).isAfter(ahora)) {
            Reserva r = cola.desencolar();
            if (r.getEstado() == EstadoReserva.PENDIENTE) { // si ya se confirmó o canceló, se ignora
                gestor.cancelarPorVencimiento(r);
                canceladas++;
            }
        }
        return canceladas;
    }
}
