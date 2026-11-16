package co.edu.uniquindio.apartasuitesuq.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Preparación terminada de un apartamento: desde que entró a la cola (check-out o vuelta a
 * servicio) hasta que quedó Preparado.
 */
public class RegistroPreparacion {

    private final String codigoApartamento;
    private final LocalDateTime ingreso;
    private final LocalDateTime fin;

    public RegistroPreparacion(String codigoApartamento, LocalDateTime ingreso, LocalDateTime fin) {
        this.codigoApartamento = codigoApartamento;
        this.ingreso = ingreso;
        this.fin = fin;
    }

    public String getCodigoApartamento() {
        return codigoApartamento;
    }

    public LocalDateTime getIngreso() {
        return ingreso;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public long getMinutos() {
        return Duration.between(ingreso, fin).toMinutes();
    }
}
