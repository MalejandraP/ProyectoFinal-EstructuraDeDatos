package co.edu.uniquindio.apartasuitesuq.model.Excepciones;

/**
 * Clase excepción propia, se utiliza cuando una reserva se superpone a otra en tiempo.
 */
public class ReservaSolapadaException extends RuntimeException {
    public ReservaSolapadaException(String mensaje) {
        super(mensaje);
    }
}
