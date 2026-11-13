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
    private double tarifaBaseNoche;
    private EstadoApartamento estado;
    private String motivoFueraServicio;
    private ArbolBinarioBusqueda<Reserva> reservas;


    public Apartamento(String codigo, String nombre, String bloque, int piso,
                       int numeroDormitorios, int capacidadMaxima, double tarifaBaseNoche) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.piso = piso;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.tarifaBaseNoche = tarifaBaseNoche;
        this.estado = EstadoApartamento.PREPARADO;
        this.reservas = new ArbolBinarioBusqueda<>();
    }

    /**
     * Obtiene el código único del apartamento.
     *
     * @return código del apartamento
     */
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
            throw new IllegalArgumentException("El piso debe ser un número entero positivo mayor o igual a 1.");
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

    /**
     * Establece el motivo por el cual el apartamento pasa a estar fuera de servicio.
     *
     * @param motivoFueraServicio descripción del motivo
     */
    public void setMotivoFueraServicio(String motivoFueraServicio) {
        this.motivoFueraServicio = motivoFueraServicio;
    }

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
    public int compareTo(Apartamento auxiliar) {
        if (auxiliar == null || auxiliar.getCodigo() == null) {
            return 1;
        }
        return this.codigo.compareToIgnoreCase(auxiliar.getCodigo());
    }

    /**
     * Devuelve una representación textual con los atributos principales del apartamento.
     *
     * @return cadena descriptiva del apartamento
     */
    @Override
    public String toString() {
        return "Apartamento{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", bloque='" + bloque + '\'' +
                ", piso=" + piso +
                ", numeroDormitorios=" + numeroDormitorios +
                ", capacidadMaxima=" + capacidadMaxima +
                ", tarifaBaseNoche=" + tarifaBaseNoche +
                '}';
    }
}