package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.ReservaSolapadaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio encargado de gestionar el ciclo de vida completo de las reservas (Módulo P2).
 * <p>
 * Utiliza un {@link ArbolBinarioBusqueda} para indexar y consultar las reservas por código
 * de manera eficiente, delegando la validación de fechas y detección de solapamientos
 * a la clase {@link Estancia}.
 */
public class ServicioReservas {

    /** Árbol binario de búsqueda que almacena las reservas ordenadas por su código único. */
    private final ArbolBinarioBusqueda<Reserva> arbolReservas = new ArbolBinarioBusqueda<>();


    private int contador = 0;

    /**
     * Crea y registra una nueva reserva en el sistema tras validar fechas, capacidad
     * del apartamento y ausencia de solapamientos con otras reservas activas.
     *
     * @param titular     huésped principal responsable de la reserva
     * @param apartamento        apartamento solicitado para la estancia
     * @param entrada     fecha de llegada (primera noche)
     * @param salida      fecha de salida (noche no cobrada)
     * @param numPersonas cantidad total de huéspedes que ocuparán el apartamento
     * @param canal       canal de origen desde el cual se solicita la reserva
     * @return la instancia de {@link Reserva} creada e insertada en el árbol
     * @throws IllegalArgumentException   si el huésped titular o el apartamento son nulos
     * @throws FechasInvalidasException   si las fechas son nulas, incoherentes o están en el pasado
     * @throws CapacidadExcedidaException si el número de personas es menor o igual a 0 o supera la capacidad máxima del apartamento
     * @throws ReservaSolapadaException   si existe otra reserva activa en el mismo apartamento cuyas fechas se cruzan
     */
    public synchronized Reserva crearReserva(Huesped titular, Apartamento apartamento,
                                             LocalDate entrada, LocalDate salida,
                                             int numPersonas, Canal canal) {
        if (titular == null) {
            throw new IllegalArgumentException("El huésped titular es obligatorio.");
        }
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento es obligatorio.");
        }

        // 1. Validación de fechas delegada a Estancia
        Estancia nuevaEstancia = new Estancia(entrada, salida);
        if (entrada.isBefore(LocalDate.now())) {
            throw new FechasInvalidasException("La fecha de entrada no puede estar en el pasado.");
        }

        // 2. Validación de capacidad del apartamento
        if (numPersonas <= 0) {
            throw new CapacidadExcedidaException("Debe haber al menos una persona en la reserva.");
        }
        if (numPersonas > apartamento.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException(
                    "Capacidad máxima excedida para el apartamento " + apartamento.getCodigo()
                            + " (máximo " + apartamento.getCapacidadMaxima() + " personas).");
        }

        // 3. Validación de solapamiento usando el árbol de reservas y Estancia.seSolapaCon
        if (haySolapamiento(apartamento, nuevaEstancia)) {
            throw new ReservaSolapadaException(
                    "El apartamento " + apartamento.getCodigo() + " ya tiene una reserva activa en esas fechas.");
        }

        // 4. Creación de la reserva con Estancia y almacenamiento en el ABB
        double costo = apartamento.calcularCostoEstadia(nuevaEstancia.getNumeroNoches());
        String codigo = "RES-" + (++contador);
        Canal canalEfectivo = (canal != null) ? canal : Canal.PORTAL;

        Reserva reserva = new Reserva(
                codigo,
                titular,
                apartamento,
                nuevaEstancia,
                estadoInicial(canalEfectivo),
                costo,
                canalEfectivo
        );

        arbolReservas.insertar(reserva);
        return reserva;
    }

    /**
     * Determina el estado inicial de una reserva según el canal de origen.
     * Las reservas provenientes del portal web inician pendientes de pago/confirmación;
     * las demás inician confirmadas.
     *
     * @param canal canal por el cual ingresó la reserva
     * @return {@link EstadoReserva#PENDIENTE} si es {@link Canal#PORTAL}, o {@link EstadoReserva#CONFIRMADA} en otro caso
     */
    private EstadoReserva estadoInicial(Canal canal) {
        return canal == Canal.PORTAL ? EstadoReserva.PENDIENTE : EstadoReserva.CONFIRMADA;
    }

    /**
     * Verifica si existe alguna reserva activa en el árbol para el apartamento indicado
     * cuya estancia se solape con la nueva estancia solicitada.
     *
     * @param apartamento apartamento a consultar
     * @param nuevaEstancia rango de fechas [entrada, salida) que se desea reservar
     * @return {@code true} si existe al menos una noche en conflicto con una reserva activa; {@code false} en caso contrario
     */
    public boolean haySolapamiento(Apartamento apartamento, Estancia nuevaEstancia) {
        if (apartamento == null || nuevaEstancia == null) {
            return false;
        }
        for (Reserva r : arbolReservas) {
            if (r.estaActiva()
                    && r.getApartamento() != null
                    && r.getApartamento().getCodigo().equals(apartamento.getCodigo())
                    && r.getEstancia() != null
                    && r.getEstancia().seSolapaCon(nuevaEstancia)) {
                return true;
            }
        }
        return false;
    }

    // --- Gestión del ciclo de vida de la reserva ---

    /**
     * Cambia el estado de una reserva a {@link EstadoReserva#CONFIRMADA}.
     *
     * @param r reserva a confirmar
     * @throws IllegalArgumentException          si la reserva es nula
     * @throws TransicionEstadoInvalidaException si el estado actual no permite pasar a confirmada
     */
    public void confirmar(Reserva r) {
        validarReservaNoNula(r);
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
    }

    /**
     * Registra el inicio de la estadía (check-in), pasando la reserva a {@link EstadoReserva#ENCURSO}.
     *
     * @param reserva reserva que inicia su estancia
     * @throws IllegalArgumentException          si la reserva es nula
     * @throws TransicionEstadoInvalidaException si la reserva no estaba confirmada
     */
    public void iniciarEstancia(Reserva reserva) {
        validarReservaNoNula(reserva);
        reserva.cambiarEstado(EstadoReserva.ENCURSO);
    }

    /**
     * Registra la finalización de la estadía (check-out), pasando la reserva a {@link EstadoReserva#FINALIZADA}.
     *
     * @param reserva reserva a finalizar
     * @throws IllegalArgumentException          si la reserva es nula
     * @throws TransicionEstadoInvalidaException si la reserva no estaba en curso
     */
    public void finalizar(Reserva reserva) {
        validarReservaNoNula(reserva);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
    }

    /**
     * Marca una reserva confirmada como {@link EstadoReserva#NOSHOW} cuando el huésped no se presenta.
     *
     * @param reserva reserva a marcar como no-show
     * @throws IllegalArgumentException          si la reserva es nula
     * @throws TransicionEstadoInvalidaException si la reserva no estaba confirmada
     */
    public void marcarNoShow(Reserva reserva) {
        validarReservaNoNula(reserva);
        reserva.cambiarEstado(EstadoReserva.NOSHOW);
    }

    /**
     * Cancela una reserva activa y calcula el monto correspondiente a devolver al huésped.
     *
     * @param reserva reserva a cancelar
     * @return valor monetario a reembolsar según las políticas de anticipación
     * @throws IllegalArgumentException          si la reserva es nula
     * @throws TransicionEstadoInvalidaException si el estado actual no admite cancelación
     */
    public double cancelar(Reserva reserva) {
        validarReservaNoNula(reserva);
        double devolucion = calcularDevolucion(reserva); // Se calcula antes de cambiar el estado
        reserva.cambiarEstado(EstadoReserva.CANCELADA);
        return devolucion;
    }

    /**
     * Calcula el valor de reembolso en caso de cancelación.
     * Con más de 48 horas de anticipación respecto a la fecha de entrada se devuelve
     * el anticipo del 30 % del costo total; con 48 horas o menos no hay devolución.
     *
     * @param reserva reserva sobre la cual se calcula la devolución
     * @return monto a devolver ({@code costoTotal * 0.30} o {@code 0.0})
     * @throws IllegalArgumentException si la reserva es nula
     */
    public double calcularDevolucion(Reserva reserva) {
        validarReservaNoNula(reserva);
        long horas = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                reserva.getFechaInicio().atStartOfDay()
        );
        return horas > 48 ? reserva.getCostoTotal() * 0.30 : 0.0;
    }

    // --- Gestión de acompañantes en la ListaDoble<Huesped> de la reserva ---

    /**
     * Agrega un huésped acompañante a la lista doblemente enlazada de ocupantes de la reserva,
     * verificando previamente que no se supere la capacidad máxima del apartamento.
     *
     * @param reserva           reserva a la que se añadirá el acompañante
     * @param acompanante huésped acompañante a registrar
     * @throws IllegalArgumentException   si la reserva o el acompañante son nulos
     * @throws CapacidadExcedidaException si agregar al acompañante excede la capacidad del apartamento
     */
    public void agregarAcompanante(Reserva reserva, Huesped acompanante) {
        validarReservaNoNula(reserva);
        if (acompanante == null) {
            throw new IllegalArgumentException("El acompañante no puede ser nulo.");
        }
        int ocupacionActual = contarHuespedes(reserva);
        if (ocupacionActual + 1 > reserva.getApartamento().getCapacidadMaxima()) {
            throw new CapacidadExcedidaException(
                    "No se pueden agregar más huéspedes; capacidad máxima alcanzada.");
        }
        reserva.agregarHuesped(acompanante);
    }

    /**
     * Cuenta el número total de huéspedes registrados actualmente en la {@link ListaDoble}
     * de una reserva.
     *
     * @param reserva reserva a inspeccionar
     * @return cantidad de huéspedes asociados a la reserva
     */
    private int contarHuespedes(Reserva reserva) {
        int total = 0;
        if (reserva.getHuespedes() != null) {
            for (Huesped ignored : reserva.getHuespedes()) {
                total++;
            }
        }
        return total;
    }

    // --- Consultas sobre el Árbol Binario de Búsqueda ---

    /**
     * Busca una reserva en el árbol binario de búsqueda a partir de su código único.
     *
     * @param codigo identificador de la reserva (por ejemplo, {@code "RES-1"})
     * @return la instancia de {@link Reserva} encontrada, o {@code null} si no existe
     */
    public Reserva buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        Reserva claveBusqueda = new Reserva(
                codigo.trim(), null, null, (Estancia) null, null, 0.0, null
        );
        return arbolReservas.buscar(claveBusqueda);
    }

    /**
     * Obtiene todas las reservas cuyo huésped titular coincide con el huésped indicado.
     *
     * @param huesped huésped titular a consultar
     * @return una {@link ListaDoble} con las reservas asociadas al huésped
     */
    public ListaDoble<Reserva> reservasDe(Huesped huesped) {
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        if (huesped == null) {
            return resultado;
        }
        for (Reserva r : arbolReservas) {
            if (r.getHuesped() != null && r.getHuesped().equals(huesped)) {
                resultado.agregarFinal(r);
            }
        }
        return resultado;
    }

    /**
     * Devuelve la estructura interna del árbol binario de búsqueda de reservas.
     *
     * @return el {@link ArbolBinarioBusqueda} que gestiona las reservas
     */
    public ArbolBinarioBusqueda<Reserva> getArbolReservas() {
        return arbolReservas;
    }

    /**
     * Genera una lista doblemente enlazada con todas las reservas del sistema
     * ordenadas ascendentemente por código mediante un recorrido inorden del árbol.
     *
     * @return {@link ListaDoble} con todas las reservas ordenadas por código
     */
    public ListaDoble<Reserva> listarTodasOrdenadas() {
        ListaDoble<Reserva> lista = new ListaDoble<>();
        arbolReservas.recorridoInorden(lista::agregarFinal);
        return lista;
    }

    /**
     * Valida que la referencia a una reserva no sea nula antes de operar sobre ella.
     *
     * @param reserva reserva a validar
     * @throws IllegalArgumentException si {@code r} es {@code null}
     */
    private void validarReservaNoNula(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }
    }
}