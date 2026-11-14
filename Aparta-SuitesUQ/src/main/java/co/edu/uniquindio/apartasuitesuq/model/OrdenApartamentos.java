package co.edu.uniquindio.apartasuitesuq.model;

import java.util.Comparator;

/**
 * Órdenes alternativos (Comparator) para la búsqueda de disponibilidad. El orden natural de
 * {@link Apartamento} es por código; estos criterios los elige el huésped. Cada uno desempata
 * por código con {@code thenComparing} para que el resultado sea estable.
 */
public final class OrdenApartamentos {

    private OrdenApartamentos() {
    }

    /** De menor a mayor tarifa por persona por noche en temporada base. */
    public static Comparator<Apartamento> porPrecio() {
        return Comparator.comparingDouble(Apartamento::getTarifaBaseNoche)
                .thenComparing(Comparator.naturalOrder());
    }

    /** De mayor a menor capacidad máxima. */
    public static Comparator<Apartamento> porCapacidad() {
        return Comparator.comparingInt(Apartamento::getCapacidadMaxima).reversed()
                .thenComparing(Comparator.naturalOrder());
    }

    /** De mayor a menor número de dormitorios. */
    public static Comparator<Apartamento> porDormitorios() {
        return Comparator.comparingInt(Apartamento::getNumeroDormitorios).reversed()
                .thenComparing(Comparator.naturalOrder());
    }

    /** De mayor a menor número de reservas registradas (los más solicitados primero). */
    public static Comparator<Apartamento> porNumeroDeReservas(SGA sga) {
        return Comparator.comparingInt((Apartamento a) -> contarReservas(sga, a)).reversed()
                .thenComparing(Comparator.naturalOrder());
    }

    private static int contarReservas(SGA sga, Apartamento apartamento) {
        int total = 0;
        for (Reserva ignored : sga.reservasDe(apartamento)) {
            total++;
        }
        return total;
    }
}
