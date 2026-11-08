package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Apartamento del alojamiento. Se entrega completo y en exclusiva a un solo grupo.
 * <p>
 * Orden natural (Comparable): por código, sin distinguir mayúsculas. Es lo que permite
 * guardarlo en el árbol binario de búsqueda del catálogo.
 * <p>
 * Las tarifas se guardan por persona y por noche, una por cada {@link Temporada}.
 * Se usa un arreglo indexado por {@code Temporada.ordinal()} (no una colección de java.util).
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

    /**
     * Cambia el estado operativo. Para pasar a FUERADESERVICIO se debe usar
     * {@link #ponerFueraDeServicio(String)}, porque el motivo es obligatorio.
     * Al salir de Fuera de servicio el motivo se borra.
     */
    public void setEstadoOperativo(EstadoApartamento estadoOperativo) {
        if (estadoOperativo == null) {
            throw new IllegalArgumentException("El estado operativo del apartamento no puede ser nulo.");
        }
        if (estadoOperativo == EstadoApartamento.FUERADESERVICIO) {
            throw new IllegalArgumentException(
                    "Para poner el apartamento fuera de servicio se debe registrar un motivo.");
        }
        this.estadoOperativo = estadoOperativo;
        this.motivoFueraServicio = null;
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

    /** @return el motivo si está Fuera de servicio; null en otro caso */
    public String getMotivoFueraServicio() {
        return motivoFueraServicio;
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

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del apartamento no puede estar vacío.");
        }
        this.codigo = codigo.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del apartamento no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getBloque() {
        return bloque;
    }

    public void setBloque(String bloque) {
        if (bloque == null || bloque.trim().isEmpty()) {
            throw new IllegalArgumentException("El bloque no puede estar vacío.");
        }
        this.bloque = bloque.trim();
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        if (piso < 1) {
            throw new IllegalArgumentException("El piso debe ser un número entero mayor o igual a 1.");
        }
        this.piso = piso;
    }

    public int getNumeroDormitorios() {
        return numeroDormitorios;
    }

    public void setNumeroDormitorios(int numeroDormitorios) {
        if (numeroDormitorios < 1) {
            throw new IllegalArgumentException("El número de dormitorios debe ser al menos 1.");
        }
        this.numeroDormitorios = numeroDormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        if (capacidadMaxima < 1) {
            throw new IllegalArgumentException("La capacidad máxima debe ser de al menos 1 persona.");
        }
        this.capacidadMaxima = capacidadMaxima;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas == null ? "" : caracteristicas.trim();
    }

    // ------------------------------------------- Comparable, equals, hashCode

    /** Orden natural por código, sin distinguir mayúsculas. */
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

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (Bloque " + bloque + ", piso " + piso + ")";
    }
}
