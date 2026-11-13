package co.edu.uniquindio.apartasuitesuq.model.Excepciones;

/**
 * Clase excepción propia, se utiliza para transicionar entre estados de una reserva
 */
public class TransicionEstadoInvalidaException extends RuntimeException{
    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
