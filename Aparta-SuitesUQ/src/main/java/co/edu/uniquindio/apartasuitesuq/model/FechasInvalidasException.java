package co.edu.uniquindio.apartasuitesuq.model;

/**
 * CLase excepción propia, se utiliza para alertar sobre un valor de fecha inválido.
 */
public class FechasInvalidasException extends RuntimeException {
    public FechasInvalidasException(String mensaje) {
        super(mensaje);
    }
}
