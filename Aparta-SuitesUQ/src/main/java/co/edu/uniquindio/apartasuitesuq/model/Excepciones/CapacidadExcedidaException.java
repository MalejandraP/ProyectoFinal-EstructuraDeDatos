package co.edu.uniquindio.apartasuitesuq.model.Excepciones;

/**
 * Clase excepción propia, se muestra si la capacidad de personas sobrepasa la capacidad de un apartamento
 */
public class CapacidadExcedidaException extends RuntimeException{
    public CapacidadExcedidaException(String mensaje) {
        super(mensaje);
    }
}
