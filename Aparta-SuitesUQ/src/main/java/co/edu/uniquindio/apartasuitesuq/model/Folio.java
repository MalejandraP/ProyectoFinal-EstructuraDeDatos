package co.edu.uniquindio.apartasuitesuq.model;

import java.util.NoSuchElementException;

public class Folio {
    private Reserva reserva;
    private ListaDoble<Movimiento> movimientos;
    private Pila<Movimiento> operaciones;

    public Folio(Reserva reserva) {
        this.reserva = reserva;
        this.movimientos = new ListaDoble<>();
        this.operaciones = new Pila<>();
    }

    public void registrarCargo(double monto, String descripcion) {
        Movimiento mov = new Movimiento(TipoMovimiento.CARGO, monto, descripcion);
        movimientos.agregarFinal(mov);
        operaciones.apilar(mov);
    }

    public void registrarPago(double monto, String descripcion) {
        Movimiento mov = new Movimiento(TipoMovimiento.PAGO, monto, descripcion);
        movimientos.agregarFinal(mov);
        operaciones.apilar(mov);
    }
    public void deshacer(){
        if (operaciones.estaVacia()){
            throw new NoSuchElementException("La pila esta vacía");
        }
        Movimiento ultimo = operaciones.desapilar();
        movimientos.agregarFinal(ultimo.crearReverso());
    }

    public double calcularSaldo(){
        double saldo = 0;
        for(Movimiento mov : movimientos){
            if(mov.getTipo() == TipoMovimiento.CARGO){
                saldo += mov.getMonto();
            }
            saldo -= mov.getMonto();
        }
        return saldo;
    }
    public double totalPago(){
        double pagado = 0;
        for(Movimiento mov : movimientos){
            if(mov.getTipo() == TipoMovimiento.PAGO){
                pagado += mov.getMonto();
            }
        }
        return pagado;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public ListaDoble<Movimiento> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(ListaDoble<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    public Pila<Movimiento> getOperaciones() {
        return operaciones;
    }

    public void setOperaciones(Pila<Movimiento> operaciones) {
        this.operaciones = operaciones;
    }

    @Override
    public String toString() {
        return "Folio{" +
                " reserva= " + reserva +
                ", movimientos= " + movimientos +
                ", operaciones= " + operaciones +
                ", saldo = " + calcularSaldo() +
                '}';
    }
}
