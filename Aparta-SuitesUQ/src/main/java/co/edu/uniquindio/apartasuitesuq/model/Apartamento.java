package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ArbolBinarioBusqueda;

/**
 * Representa un apartamento o suite disponible dentro del complejo Aparta-Suites UQ.
 * <p>
 * Cada apartamento administra su propio {@link ArbolBinarioBusqueda} de {@link Reserva}
 * ordenadas cronológicamente por fecha de entrada, lo que permite validar disponibilidad
 * y detectar solapamientos de forma recursiva mediante poda de ramas.
 */
public class Apartamento implements Comparable<Apartamento> {

    private String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private String caracteristicas;
    private final double[] tarifasPorTemporada;
    private EstadoApartamento estadoOperativo;
    private String motivoFueraServicio;
    private ArbolBinarioBusqueda<Reserva> reservas;


    /**
     * Crea un apartamento sin características, con la misma tarifa en todas las temporadas.
     * Se conserva por compatibilidad con el código existente.
     */
    public Apartamento(String codigo, String nombre, String bloque, int piso,
                       int numeroDormitorios, int capacidadMaxima, double tarifaBaseNoche) {
        this(codigo, nombre, bloque, piso, numeroDormitorios, capacidadMaxima, tarifaBaseNoche, "");
    }

    /**
     * Crea un apartamento en estado PREPARADO. Todas las temporadas arrancan con la tarifa base
     * (BAJA); el administrador define luego las tarifas de MEDIA y ALTA con {@link #setTarifa}.
     *
     * @param tarifaBaseNoche tarifa por persona por noche en temporada BAJA
     * @param caracteristicas descripción libre (vista, balcón, cocina...). Puede ser vacía
     */
    public Apartamento(String codigo, String nombre, String bloque, int piso,
                       int numeroDormitorios, int capacidadMaxima, double tarifaBaseNoche,
                       String caracteristicas) {
        setCodigo(codigo);
        setNombre(nombre);
        setBloque(bloque);
        setPiso(piso);
        setNumeroDormitorios(numeroDormitorios);
        setCapacidadMaxima(capacidadMaxima);
        setCaracteristicas(caracteristicas);

        this.tarifasPorTemporada = new double[Temporada.values().length];
        for (Temporada temporada : Temporada.values()) {
            setTarifa(temporada, tarifaBaseNoche);
        }
        this.estadoOperativo = EstadoApartamento.PREPARADO;
        this.motivoFueraServicio = null;
    }

    // ---------------------------------------------------------------- tarifas

    /**
     * @param temporada temporada a consultar
     * @return tarifa por persona por noche en esa temporada
     */
    public double getTarifa(Temporada temporada) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada no puede ser nula.");
        }
        return tarifasPorTemporada[temporada.ordinal()];
    }

    /**
     * Define la tarifa por persona por noche de una temporada.
     * @throws IllegalArgumentException si la temporada es nula o la tarifa no es positiva
     */
    public void setTarifa(Temporada temporada, double tarifa) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada no puede ser nula.");
        }
        if (tarifa <= 0) {
            throw new IllegalArgumentException("La tarifa de temporada " + temporada.getDescripcion()
                    + " debe ser un valor positivo.");
        }
        tarifasPorTemporada[temporada.ordinal()] = tarifa;
    }

    /** @return la tarifa de la temporada base (BAJA) */
    public double getTarifaBaseNoche() {
        return getTarifa(Temporada.base());
    }

    /** Cambia la tarifa de la temporada base (BAJA). */
    public void setTarifaBaseNoche(double tarifaBaseNoche) {
        setTarifa(Temporada.base(), tarifaBaseNoche);
    }

    /**
     * Costo aproximado con la tarifa base, sin temporadas ni personas.
     * PROVISIONAL: lo usan ServicioReservas y Reserva mientras se integra la liquidación
     * noche por noche por temporada (LiquidadorTarifas).
     */
    public double calcularCostoEstadia(int numNoches) {
        if (numNoches <= 0) {
            throw new IllegalArgumentException("El número de noches debe ser mayor a cero.");
        }
        return getTarifaBaseNoche() * numNoches;
    }

    // ------------------------------------------------------- estado operativo

    public EstadoApartamento getEstadoOperativo() {
        return estadoOperativo;
    }

    /** Alias de {@link #getEstadoOperativo()}; lo usan ServicioPreparacion y sus pruebas (P3). */
    public EstadoApartamento getEstado() {
        return getEstadoOperativo();
    }

    /**
     * Cambia el estado operativo. Las reglas de transición (check-in solo si está Preparado,
     * check-out a Pendiente de preparación, etc.) las aplica ServicioPreparacion.
     * Al salir de Fuera de servicio el motivo se borra.
     * <p>
     * Para pasar a Fuera de servicio desde fuera de ServicioPreparacion se recomienda
     * {@link #ponerFueraDeServicio(String)}, que exige el motivo.
     */
    public void setEstadoOperativo(EstadoApartamento estadoOperativo) {
        if (estadoOperativo == null) {
            throw new IllegalArgumentException("El estado operativo del apartamento no puede ser nulo.");
        }
        if (estadoOperativo != EstadoApartamento.FUERADESERVICIO) {
            this.motivoFueraServicio = null;
        }
        this.estadoOperativo = estadoOperativo;
    }

    /** Alias de {@link #setEstadoOperativo(EstadoApartamento)}; lo usa ServicioPreparacion (P3). */
    public void setEstado(EstadoApartamento estado) {
        setEstadoOperativo(estado);
    }

    /**
     * Pone el apartamento Fuera de servicio registrando el motivo (obligatorio).
     * @param motivo razón del bloqueo (daño, mantenimiento, fumigación...)
     */
    public void ponerFueraDeServicio(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de fuera de servicio es obligatorio.");
        }
        this.estadoOperativo = EstadoApartamento.FUERADESERVICIO;
        this.motivoFueraServicio = motivo.trim();
    }

    /**
     * Obtiene el código único del apartamento.
     *
     * @return código del apartamento
     */
    /** @return el motivo si está Fuera de servicio; null en otro caso */
    public String getMotivoFueraServicio() {
        return motivoFueraServicio;
    }

    /**
     * Registra o corrige el motivo de Fuera de servicio.
     * Solo se acepta mientras el apartamento esté en ese estado.
     */
    public void setMotivoFueraServicio(String motivoFueraServicio) {
        if (!estaFueraDeServicio()) {
            throw new IllegalStateException("El apartamento " + codigo + " no está fuera de servicio.");
        }
        if (motivoFueraServicio == null || motivoFueraServicio.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de fuera de servicio es obligatorio.");
        }
        this.motivoFueraServicio = motivoFueraServicio.trim();
    }

    public boolean estaFueraDeServicio() {
        return estadoOperativo == EstadoApartamento.FUERADESERVICIO;
    }

    /** @return true si está Preparado y por tanto puede hacer check-in */
    public boolean estaPreparado() {
        return estadoOperativo.puedeRecibirHuespedes();
    }

    // ------------------------------------------------------------- capacidad

    /** @return true si el grupo cabe (los menores de 6 años también ocupan cupo) */
    public boolean soportaCapacidad(int numPersonas) {
        return numPersonas > 0 && numPersonas <= this.capacidadMaxima;
    }

    // -------------------------------------------------- getters y setters

    public String getCodigo() {
        return codigo;
    }

    /**
     * Establece el código único del apartamento.
     *
     * @param codigo nuevo código del apartamento
     * @throws IllegalArgumentException si el código es nulo o está vacío
     */
    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del apartamento no puede estar vacío.");
        }
        this.codigo = codigo.trim();
    }

    /**
     * Obtiene el nombre descriptivo del apartamento.
     *
     * @return nombre del apartamento
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre descriptivo del apartamento.
     *
     * @param nombre nuevo nombre del apartamento
     * @throws IllegalArgumentException si el nombre es nulo o está vacío
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del apartamento no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    /**
     * Obtiene el bloque o torre donde se encuentra el apartamento.
     *
     * @return nombre del bloque
     */
    public String getBloque() {
        return bloque;
    }

    /**
     * Establece el bloque o torre del apartamento.
     *
     * @param bloque nuevo bloque del apartamento
     * @throws IllegalArgumentException si el bloque es nulo o está vacío
     */
    public void setBloque(String bloque) {
        if (bloque == null || bloque.trim().isEmpty()) {
            throw new IllegalArgumentException("El bloque no puede estar vacío.");
        }
        this.bloque = bloque.trim();
    }

    /**
     * Obtiene el número de piso del apartamento.
     *
     * @return piso en el que se ubica el apartamento
     */
    public int getPiso() {
        return piso;
    }

    /**
     * Establece el número de piso del apartamento.
     *
     * @param piso número de piso (debe ser mayor o igual a 1)
     * @throws IllegalArgumentException si el piso es menor a 1
     */
    public void setPiso(int piso) {
        if (piso < 1) {
            throw new IllegalArgumentException("El piso debe ser un número entero mayor o igual a 1.");
        }
        this.piso = piso;
    }

    /**
     * Obtiene la cantidad de dormitorios del apartamento.
     *
     * @return número de dormitorios
     */
    public int getNumeroDormitorios() {
        return numeroDormitorios;
    }

    /**
     * Establece la cantidad de dormitorios del apartamento.
     *
     * @param numeroDormitorios número de dormitorios (debe ser al menos 1)
     * @throws IllegalArgumentException si el número de dormitorios es menor a 1
     */
    public void setNumeroDormitorios(int numeroDormitorios) {
        if (numeroDormitorios < 1) {
            throw new IllegalArgumentException("El número de dormitorios debe ser al menos 1.");
        }
        this.numeroDormitorios = numeroDormitorios;
    }

    /**
     * Obtiene la capacidad máxima de huéspedes del apartamento.
     *
     * @return capacidad máxima de personas
     */
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    /**
     * Establece la capacidad máxima de huéspedes del apartamento.
     *
     * @param capacidadMaxima número máximo de personas (debe ser al menos 1)
     * @throws IllegalArgumentException si la capacidad máxima es menor a 1
     */
    public void setCapacidadMaxima(int capacidadMaxima) {
        if (capacidadMaxima < 1) {
            throw new IllegalArgumentException("La capacidad máxima debe ser de al menos 1 persona.");
        }
        this.capacidadMaxima = capacidadMaxima;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    /**
     * Obtiene la tarifa base por noche del apartamento.
     *
     * @return tarifa base por noche
     */
    public double getTarifaBaseNoche() {
        return tarifaBaseNoche;
    }

    /**
     * Establece la tarifa base por noche del apartamento.
     *
     * @param tarifaBaseNoche nueva tarifa base por noche (debe ser mayor a 0)
     * @throws IllegalArgumentException si la tarifa es menor o igual a 0
     */
    public void setTarifaBaseNoche(double tarifaBaseNoche) {
        if (tarifaBaseNoche <= 0) {
            throw new IllegalArgumentException("La tarifa base por noche debe ser un valor positivo.");
        }
        this.tarifaBaseNoche = tarifaBaseNoche;
    }

    /**
     * Obtiene el estado operativo actual del apartamento.
     *
     * @return estado del apartamento
     */
    public EstadoApartamento getEstado() {
        return estado;
    }

    /**
     * Cambia el estado operativo del apartamento.
     *
     * @param estado nuevo {@link EstadoApartamento}
     */
    public void setEstado(EstadoApartamento estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el motivo por el cual el apartamento está fuera de servicio, si aplica.
     *
     * @return motivo de fuera de servicio, o {@code null} si está operativo
     */
    public String getMotivoFueraServicio() {
        return motivoFueraServicio;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas == null ? "" : caracteristicas.trim();
    }

    // ------------------------------------------- Comparable, equals, hashCode
    /**
     * Devuelve el árbol binario de búsqueda de reservas asociadas a este apartamento,
     * ordenadas cronológicamente por fecha de entrada.
     *
     * @return {@link ArbolBinarioBusqueda} de reservas del apartamento
     */
    public ArbolBinarioBusqueda<Reserva> getReservas() {
        if (reservas == null) {
            reservas = new ArbolBinarioBusqueda<>();
        }
        return reservas;
    }

    /**
     * Inserta una reserva en el árbol propio de este apartamento.
     *
     * @param reserva reserva asociada al apartamento
     */
    public void registrarReserva(Reserva reserva) {
        if (reserva != null) {
            getReservas().insertar(reserva);
        }
    }

    /** Orden natural por código, sin distinguir mayúsculas. */
    /**
     * Detecta de forma recursiva si alguna reserva activa de este apartamento se solapa
     * con {@code nuevaEstancia}, podando el subárbol derecho cuando la fecha de entrada
     * del nodo actual es igual o posterior a la salida solicitada.
     *
     * @param nuevaEstancia rango de fechas [entrada, salida) a verificar
     * @return {@code true} si existe solapamiento con una reserva activa; {@code false} en caso contrario
     */
    public boolean tieneSolapamiento(Estancia nuevaEstancia) {
        if (nuevaEstancia == null || getReservas().estaVacio()) {
            return false;
        }
        return getReservas().existeConPoda(
                // 1. Condición de conflicto en el nodo actual
                r -> r.estaActiva() && r.getEstancia() != null && r.getEstancia().seSolapaCon(nuevaEstancia),
                // 2. Explorar izquierda: siempre puede haber una reserva anterior cuya salida invada nuevaEstancia
                r -> true,
                // 3. Explorar derecha: SOLO si la reserva actual empieza antes de que termine nuevaEstancia
                r -> r.getFechaInicio() != null && r.getFechaInicio().isBefore(nuevaEstancia.getSalida())
        );
    }

    /**
     * Calcula el costo total de una estadía multiplicando la tarifa base por la cantidad de noches.
     *
     * @param numNoches cantidad de noches de alojamiento
     * @return costo total de la estadía
     * @throws IllegalArgumentException si el número de noches es menor o igual a cero
     */
    public double calcularCostoEstadia(int numNoches) {
        if (numNoches <= 0) {
            throw new IllegalArgumentException("El número de noches debe ser mayor a cero.");
        }
        return this.tarifaBaseNoche * numNoches;
    }

    /**
     * Verifica si el apartamento soporta la cantidad de huéspedes indicada.
     *
     * @param numPersonas cantidad de huéspedes a alojar
     * @return {@code true} si {@code numPersonas} es mayor a 0 y no supera {@code capacidadMaxima}
     */
    public boolean soportaCapacidad(int numPersonas) {
        return numPersonas > 0 && numPersonas <= this.capacidadMaxima;
    }

    /**
     * Compara este apartamento con otro ordenándolos alfabéticamente por su código
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param auxiliar otro apartamento a comparar
     * @return un entero negativo, cero o positivo según el orden lexicográfico de sus códigos
     */
    @Override
    public int compareTo(Apartamento otro) {
        if (otro == null || otro.getCodigo() == null) {
            return 1;
        }
        return this.codigo.compareToIgnoreCase(otro.getCodigo());
    }

    /**
     * Dos apartamentos son iguales si tienen el mismo código (coherente con compareTo).
     * Lo usa el Conjunto de favoritos para no guardar repetidos.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apartamento)) {
            return false;
        }
        return codigo.equalsIgnoreCase(((Apartamento) o).codigo);
    }

    @Override
    public int hashCode() {
        return codigo.toUpperCase().hashCode();
    }

    /**
     * Devuelve una representación textual con los atributos principales del apartamento.
     *
     * @return cadena descriptiva del apartamento
     */
    @Override
    public String toString() {
        return codigo + " - " + nombre + " (Bloque " + bloque + ", piso " + piso + ")";
    }
}
