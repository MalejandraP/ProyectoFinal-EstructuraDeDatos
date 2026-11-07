package co.edu.uniquindio.apartasuitesuq.model;

public class TransicionEstadoInvalidaException extends RuntimeException{
    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
