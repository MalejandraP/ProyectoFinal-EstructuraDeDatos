package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.TransicionEstadoInvalidaException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ServicioRecepcion {
    private final ServicioPreparacion preparacion;

    public ServicioRecepcion(ServicioPreparacion preparacion) {
        this.preparacion = preparacion;
    }


    public void checkIn(Reserva reserva, LocalDate hoy) {
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new TransicionEstadoInvalidaException(
                    "La reserva " + reserva.getCodigoReserva() + " está " + reserva.getEstado() + ", debe estar CONFIRMADA");
        }
        if (!hoy.equals(reserva.getFechaInicio())) {
            throw new TransicionEstadoInvalidaException(
                    "La reserva " + reserva.getCodigoReserva() + " entra el " + reserva.getFechaInicio() + ", no hoy");
        }
        preparacion.checkIn(reserva.getApartamento());
        reserva.cambiarEstado(EstadoReserva.ENCURSO);
    }


    public void checkOut(Reserva reserva, LocalDateTime ahora) {
        if (reserva.getEstado() != EstadoReserva.ENCURSO) {
            throw new TransicionEstadoInvalidaException(
                    "La reserva " + reserva.getCodigoReserva() + " está " + reserva.getEstado() + ", debe estar EN CURSO");
        }
        preparacion.checkOut(reserva.getApartamento(), ahora);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
    }
}
