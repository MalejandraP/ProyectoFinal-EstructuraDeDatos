package co.edu.uniquindio.apartasuitesuq.model;

public class Apartamento implements Comparable<Apartamento> {
    private String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private double tarifaBaseNoche;

    public Apartamento(String codigo, String nombre, String bloque, int piso,
                       int numeroDormitorios, int capacidadMaxima, double tarifaBaseNoche) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.piso = piso;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.tarifaBaseNoche = tarifaBaseNoche;
    }


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
            throw new IllegalArgumentException("El piso debe ser un número entero positivo mayor o igual a 1.");
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

    public double getTarifaBaseNoche() {
        return tarifaBaseNoche;
    }

    public void setTarifaBaseNoche(double tarifaBaseNoche) {
        if (tarifaBaseNoche <= 0) {
            throw new IllegalArgumentException("La tarifa base por noche debe ser un valor positivo.");
        }
        this.tarifaBaseNoche = tarifaBaseNoche;
    }


    public double calcularCostoEstadia(int numNoches) {
        if (numNoches <= 0) {
            throw new IllegalArgumentException("El número de noches debe ser mayor a cero.");
        }
        return this.tarifaBaseNoche * numNoches;
    }

    public boolean soportaCapacidad(int numPersonas) {
        boolean capacidadValida = numPersonas > 0 && numPersonas <= this.capacidadMaxima;
        return capacidadValida;
    }

    //COMPARABLE, los ordena alfabeticamente
    @Override
    public int compareTo(Apartamento auxiliar) {
        if (auxiliar == null || auxiliar.getCodigo() == null) {
            return 1;
        }
        return this.codigo.compareToIgnoreCase(auxiliar.getCodigo());
    }

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