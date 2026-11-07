package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ServicioReservas { //PROVISIONAL

    private ListaDoble<Reserva> reservas = new ListaDoble<>();
    private int contador = 0;

    public synchronized Reserva crearReserva(Huesped titular, Apartamento apto,
                                             LocalDate entrada, LocalDate salida,
                                             int numPersonas, Canal canal) {
        // Fechas
        if (entrada == null || salida == null) {
            throw new FechasInvalidasException("Las fechas no pueden ser nulas.");
        }
        if (!salida.isAfter(entrada)) {
            throw new FechasInvalidasException("La salida debe ser posterior a la entrada.");
        }
        if (entrada.isBefore(LocalDate.now())) {
            throw new FechasInvalidasException("La entrada no puede estar en el pasado.");
        }

        // Capacidad
        if (numPersonas <= 0) {
            throw new CapacidadExcedidaException("Debe haber al menos una persona.");
        }
        if (numPersonas > apto.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException("Capacidad máxima: " + apto.getCapacidadMaxima());
        }

        // Solapamiento
        if (haySolapamiento(apto, entrada, salida)) {
            throw new ReservaSolapadaException(
                    "El apartamento " + apto.getCodigo() + " ya está reservado en esas fechas.");
        }

        // Crear con costo provisional
        int noches = (int) ChronoUnit.DAYS.between(entrada, salida);
        double costo = apto.calcularCostoEstadia(noches);
        String codigo = "RES-" + (++contador);
        Reserva r = new Reserva(codigo, titular, apto, entrada, salida,
                estadoInicial(canal), costo, canal);
        r.setCanal(canal);

        // Guardar
        reservas.agregarFinal(r);
        return r;
    }

    private EstadoReserva estadoInicial(Canal canal) {
        return canal == Canal.PORTAL ? EstadoReserva.PENDIENTE : EstadoReserva.CONFIRMADA;
    }

    // PROVISIONAL
    private boolean haySolapamiento(Apartamento apto, LocalDate entrada, LocalDate salida) {
        for (Reserva r : reservas) {
            if (r.getApartamento().getCodigo().equals(apto.getCodigo())
                    && r.estaActiva()
                    && entrada.isBefore(r.getFechaFin())
                    && r.getFechaInicio().isBefore(salida)) {
                return true;
            }
        }
        return false;
    }

    //  Ciclo de vida
//    public void confirmar(Reserva r)        { r.cambiarEstado(EstadoReserva.CONFIRMADA); }
//    public void iniciarEstancia(Reserva r)  { r.cambiarEstado(EstadoReserva.ENCURSO); }
//    public void finalizar(Reserva r)        { r.cambiarEstado(EstadoReserva.FINALIZADA); }
//    public void marcarNoShow(Reserva r)     { r.cambiarEstado(EstadoReserva.NOSHOW); }

    public double cancelar(Reserva r) {
        double devolucion = calcularDevolucion(r);   // se calcula antes de cambiar el estado
        r.cambiarEstado(EstadoReserva.CANCELADA);
        return devolucion;
    }

    // Con más de 48 h de anticipación se devuelve el anticipo del 30 %; con menos, nada.
    public double calcularDevolucion(Reserva r) {
        long horas = ChronoUnit.HOURS.between(LocalDate.now().atStartOfDay(),
                r.getFechaInicio().atStartOfDay());
        return horas > 48 ? r.getCostoTotal() * 0.30 : 0.0;
    }

    public Reserva buscarPorCodigo(String codigo) {
        for (Reserva r : reservas) {
            if (r.getCodigoReserva().equalsIgnoreCase(codigo)) return r;
        }
        return null;
    }

    public ListaDoble<Reserva> reservasDe(Huesped h) {
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        for (Reserva r : reservas) {
            if (r.getHuesped() == h) resultado.agregarFinal(r);
        }
        return resultado;
    }
}