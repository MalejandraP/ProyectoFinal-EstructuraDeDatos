package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Reserva de un apartamento para una estancia [fechaInicio, fechaFin).
 * La noche de salida no se ocupa ni se cobra: del 10 al 12 de marzo son dos noches.
 * <p>
 * Orden natural (Comparable): por fecha de entrada y, si coincide, por código. Es lo que
 * permite guardar las reservas de cada apartamento en el árbol binario de búsqueda y
 * detectar solapamientos recorriéndolo.
 * <p>
 * Ciclo de vida: Pendiente → Confirmada → En curso → Finalizada, o Cancelada / No-show.
 */
public class Reserva implements Comparable<Reserva> {

    /** Porcentaje del valor total que se paga como anticipo en el canal Portal. */
    public static final double PORCENTAJE_ANTICIPO = 0.30;
    /** Horas que tiene una reserva Pendiente para pagar el anticipo antes de vencerse. */
    public static final int HORAS_VENCIMIENTO = 24;
    /** Hora de llegada que se asume si el huésped no informa otra. */
    public static final LocalTime HORA_LLEGADA_POR_DEFECTO = LocalTime.of(15, 0);

    private String codigoReserva;
    private Huesped huesped;
    private Apartamento apartamento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoReserva estado;
    private double costoTotal;
    private Canal canal;
    private int numeroPersonas;
    private final ListaDoble<Ocupante> ocupantes;
    private final Folio folio;
    private final LocalDateTime fechaCreacion;
    private LocalTime horaEstimadaLlegada;

    /**
     * Constructor original, con un solo ocupante declarado y creada en este instante.
     * Se conserva por compatibilidad con ServicioReservas.
     */
    public Reserva(String codigoReserva, Huesped huesped,
                   Apartamento apartamento, LocalDate fechaInicio,
                   LocalDate fechaFin, EstadoReserva estado, double costoTotal, Canal canal) {
        this(codigoReserva, huesped, apartamento, fechaInicio, fechaFin, estado, costoTotal, canal,
                1, LocalDateTime.now());
    }

    /**
     * Crea una reserva completa. Si el titular tiene fecha de nacimiento, queda
     * registrado automáticamente como primer ocupante.
     *
     * @param numeroPersonas tamaño del grupo (incluye menores, que ocupan cupo)
     * @param fechaCreacion  momento de creación; de aquí sale el vencimiento a las 24 h
     * @throws FechasInvalidasException     si la salida no es posterior a la entrada
     * @throws CapacidadExcedidaException   si el grupo no cabe en el apartamento
     */
    public Reserva(String codigoReserva, Huesped huesped,
                   Apartamento apartamento, LocalDate fechaInicio,
                   LocalDate fechaFin, EstadoReserva estado, double costoTotal, Canal canal,
                   int numeroPersonas, LocalDateTime fechaCreacion) {
        setCodigoReserva(codigoReserva);
        setCliente(huesped);
        if (apartamento == null) {
            throw new IllegalArgumentException("La reserva debe tener un apartamento asociado.");
        }
        this.apartamento = apartamento;
        validarFechas(fechaInicio, fechaFin);
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        setEstado(estado);
        this.costoTotal = costoTotal;
        this.canal = canal;
        this.fechaCreacion = fechaCreacion == null ? LocalDateTime.now() : fechaCreacion;
        this.horaEstimadaLlegada = HORA_LLEGADA_POR_DEFECTO;
        this.ocupantes = new ListaDoble<>();
        setNumeroPersonas(numeroPersonas);
        if (huesped.getFechaNacimiento() != null) {
            this.ocupantes.agregarFinal(huesped.comoOcupante());
        }
        this.folio = new Folio(this);
    }

    // ---------------------------------------------------------------- fechas

    private static void validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new FechasInvalidasException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!salida.isAfter(entrada)) {
            throw new FechasInvalidasException(
                    "La fecha de salida debe ser estrictamente posterior a la de entrada.");
        }
    }

    public int obtenerNumeroNoches() {
        return (int) ChronoUnit.DAYS.between(fechaInicio, fechaFin);
    }

    /** @return la estancia de la reserva, iterable noche por noche */
    public Estancia getEstancia() {
        return new Estancia(fechaInicio, fechaFin);
    }

    /**
     * @return true si la estancia [entrada, salida) comparte al menos una noche con esta reserva
     */
    public boolean seSolapaCon(LocalDate entrada, LocalDate salida) {
        return entrada.isBefore(fechaFin) && fechaInicio.isBefore(salida);
    }

    /** @return true si esta reserva ocupa la noche indicada (la de salida no cuenta) */
    public boolean ocupaNoche(LocalDate noche) {
        return !noche.isBefore(fechaInicio) && noche.isBefore(fechaFin);
    }

    // ------------------------------------------------------------ ocupantes

    /**
     * Registra un ocupante con su fecha de nacimiento.
     * @throws CapacidadExcedidaException si ya están registradas todas las personas declaradas
     */
    public void agregarOcupante(Ocupante ocupante) {
        if (ocupante == null) {
            throw new IllegalArgumentException("El ocupante no puede ser nulo.");
        }
        if (contarOcupantesRegistrados() >= numeroPersonas) {
            throw new CapacidadExcedidaException("La reserva " + codigoReserva + " ya tiene registradas sus "
                    + numeroPersonas + " personas.");
        }
        ocupantes.agregarFinal(ocupante);
    }

    /** @return cuántos ocupantes tienen datos registrados (recorre la lista doble) */
    public int contarOcupantesRegistrados() {
        int contador = 0;
        for (Ocupante ignored : ocupantes) {
            contador++;
        }
        return contador;
    }

    /**
     * Personas que pagan una noche dada. Los ocupantes registrados pagan si tienen 6 años o
     * más esa noche; los declarados sin datos (por ejemplo, del canal externo) se asumen adultos.
     */
    public int contarPagantesEn(LocalDate noche) {
        int pagantes = 0;
        int registrados = 0;
        for (Ocupante ocupante : ocupantes) {
            registrados++;
            if (ocupante.pagaEn(noche)) {
                pagantes++;
            }
        }
        return pagantes + (numeroPersonas - registrados);
    }

    public ListaDoble<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    /**
     * @throws CapacidadExcedidaException si supera la capacidad del apartamento
     */
    public void setNumeroPersonas(int numeroPersonas) {
        if (numeroPersonas < 1) {
            throw new IllegalArgumentException("La reserva debe tener al menos una persona.");
        }
        if (!apartamento.soportaCapacidad(numeroPersonas)) {
            throw new CapacidadExcedidaException("El apartamento " + apartamento.getCodigo()
                    + " admite máximo " + apartamento.getCapacidadMaxima() + " personas.");
        }
        if (numeroPersonas < contarOcupantesRegistrados()) {
            throw new IllegalArgumentException("Ya hay más ocupantes registrados que " + numeroPersonas + ".");
        }
        this.numeroPersonas = numeroPersonas;
    }

    // ------------------------------------------- anticipo y vencimiento

    /** @return valor del anticipo del 30 % sobre el costo total */
    public double getValorAnticipo() {
        return costoTotal * PORCENTAJE_ANTICIPO;
    }

    /** @return momento en que vence la reserva si sigue Pendiente (creación + 24 h) */
    public LocalDateTime getFechaVencimiento() {
        return fechaCreacion.plusHours(HORAS_VENCIMIENTO);
    }

    /** @return true si está Pendiente y ya pasó su hora de vencimiento */
    public boolean estaVencida(LocalDateTime ahora) {
        return estado == EstadoReserva.PENDIENTE && ahora.isAfter(getFechaVencimiento());
    }

    // ------------------------------------------------------------- estado

    /**
     * Cambia el estado respetando el ciclo de vida.
     * @throws TransicionEstadoInvalidaException si la transición no está permitida
     */
    public void cambiarEstado(EstadoReserva nuevo) {
        if (nuevo == null) {
            throw new TransicionEstadoInvalidaException("El estado no puede ser nulo.");
        }
        if (!transicionValida(this.estado, nuevo)) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede pasar de " + this.estado.getDescripcion() + " a " + nuevo.getDescripcion());
        }
        this.estado = nuevo;
    }

    private boolean transicionValida(EstadoReserva actual, EstadoReserva nuevo) {
        switch (actual) {
            case PENDIENTE:
                return nuevo == EstadoReserva.CONFIRMADA || nuevo == EstadoReserva.CANCELADA;
            case CONFIRMADA:
                return nuevo == EstadoReserva.ENCURSO
                        || nuevo == EstadoReserva.CANCELADA
                        || nuevo == EstadoReserva.NOSHOW;
            case ENCURSO:
                return nuevo == EstadoReserva.FINALIZADA;
            default:
                return false;
        }
    }

    /** Cancela la reserva pasando por las reglas del ciclo de vida. */
    public void cancelarReserva() {
        cambiarEstado(EstadoReserva.CANCELADA);
    }

    /** @return true si ocupa sus noches (Pendiente, Confirmada o En curso) */
    public boolean estaActiva() {
        return estado.esActiva();
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    /**
     * Asigna el estado sin validar transiciones. Usar solo al cargar datos;
     * en la operación normal se usa {@link #cambiarEstado(EstadoReserva)}.
     */
    public void setEstado(EstadoReserva estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la reserva no puede estar vacío.");
        }
        this.estado = estado;
    }

    // -------------------------------------------------------------- costo

    /**
     * Costo aproximado con la tarifa base. PROVISIONAL hasta integrar la liquidación
     * noche por noche por temporada.
     */
    public double calcularCostoTotal() {
        return this.apartamento.calcularCostoEstadia(obtenerNumeroNoches());
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    /** Lo asigna la liquidación noche por noche. */
    public void setCostoTotal(double costoTotal) {
        if (costoTotal < 0) {
            throw new IllegalArgumentException("El costo total no puede ser negativo.");
        }
        this.costoTotal = costoTotal;
    }

    // -------------------------------------------------- getters y setters

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        if (codigoReserva == null || codigoReserva.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la reserva no puede estar vacío.");
        }
        this.codigoReserva = codigoReserva.trim();
    }

    public Huesped getHuesped() {
        return huesped;
    }

    /** Alias de {@link #getHuesped()} con el nombre que usa el enunciado. */
    public Huesped getTitular() {
        return huesped;
    }

    public void setCliente(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("La reserva debe tener un huésped titular.");
        }
        this.huesped = huesped;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public void setApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("La reserva debe tener un apartamento asociado.");
        }
        if (!apartamento.soportaCapacidad(numeroPersonas)) {
            throw new CapacidadExcedidaException("El apartamento " + apartamento.getCodigo()
                    + " no tiene capacidad para " + numeroPersonas + " personas.");
        }
        this.apartamento = apartamento;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /**
     * @throws FechasInvalidasException si la salida no es posterior a la entrada
     */
    public void setFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        validarFechas(fechaInicio, fechaFin);
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public Folio getFolio() {
        return folio;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalTime getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    public void setHoraEstimadaLlegada(LocalTime horaEstimadaLlegada) {
        if (horaEstimadaLlegada == null) {
            throw new IllegalArgumentException("La hora estimada de llegada no puede ser nula.");
        }
        this.horaEstimadaLlegada = horaEstimadaLlegada;
    }

    // ------------------------------------------- Comparable, equals, hashCode

    /**
     * Orden natural por fecha de entrada; si dos reservas entran el mismo día se
     * desempata por código, para que el árbol no las tome como duplicadas.
     */
    @Override
    public int compareTo(Reserva otra) {
        if (otra == null) {
            return 1;
        }
        int porFecha = this.fechaInicio.compareTo(otra.fechaInicio);
        if (porFecha != 0) {
            return porFecha;
        }
        return this.codigoReserva.compareToIgnoreCase(otra.codigoReserva);
    }

    /** Dos reservas son iguales si tienen el mismo código. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva)) {
            return false;
        }
        return codigoReserva.equalsIgnoreCase(((Reserva) o).codigoReserva);
    }

    @Override
    public int hashCode() {
        return codigoReserva.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return codigoReserva + " | " + apartamento.getCodigo() + " | " + huesped.getNombre()
                + " | " + fechaInicio + " → " + fechaFin + " | " + estado.getDescripcion();
    }
}
