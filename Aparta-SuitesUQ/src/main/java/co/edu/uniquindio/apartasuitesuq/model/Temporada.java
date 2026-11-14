package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Temporadas del alojamiento. BAJA es la temporada base: toda noche que no
 * pertenezca a un periodo definido como MEDIA o ALTA se cobra como BAJA.
 */
public enum Temporada {

    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    private final String descripcion;

    Temporada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** @return la temporada que se usa cuando una fecha no tiene periodo asignado */
    public static Temporada base() {
        return BAJA;
    }
}
