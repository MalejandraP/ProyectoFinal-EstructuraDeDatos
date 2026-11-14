package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDateTime;

public class Movimiento {
    private final TipoMovimiento tipo;
    private final double monto;
    private final String descripcion;
    private final LocalDateTime fechayHora;
    private final boolean esReverso;

    public Movimiento(TipoMovimiento tipo, double monto, String descripcion) {
        this(tipo, monto, descripcion, false);
    }
    private Movimiento(TipoMovimiento tipo, double monto, String descripcion, boolean esReverso) {
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser positivo");
        this.monto = monto;
        if (tipo == null) throw new IllegalArgumentException("El tipo no puede ser nulo");
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechayHora = LocalDateTime.now();
        this.esReverso = esReverso;
    }

    public Movimiento crearReverso() {
        TipoMovimiento contrario = null;
        if(tipo == TipoMovimiento.CARGO){
            contrario = TipoMovimiento.PAGO;
        } else {
            contrario = TipoMovimiento.CARGO;
        }
        return new Movimiento(contrario, monto, "Reverso: " + descripcion, true);
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechayHora() {
        return fechayHora;
    }


    public boolean isEsReverso() {
        return esReverso;
    }


}
