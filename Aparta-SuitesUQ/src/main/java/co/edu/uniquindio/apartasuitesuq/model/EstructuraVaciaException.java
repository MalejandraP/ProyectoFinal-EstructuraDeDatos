package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Clase Excepción propia, alerta cuando una estructura está vacía
 */
public class EstructuraVaciaException extends RuntimeException {
    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}
