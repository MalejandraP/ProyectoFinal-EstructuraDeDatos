package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.CalendarioTemporadas;
import co.edu.uniquindio.apartasuitesuq.model.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Cotizacion;
import co.edu.uniquindio.apartasuitesuq.model.Estancia;
import co.edu.uniquindio.apartasuitesuq.model.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.NocheLiquidada;
import co.edu.uniquindio.apartasuitesuq.model.Ocupante;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Temporada;

import java.time.LocalDate;
import java.util.Iterator;

/**
 * Liquida el valor de una estancia noche por noche.
 * <p>
 * Reglas: se cobra por persona y por noche; cada noche toma la tarifa de su temporada
 * (una estancia que cruza temporadas se liquida noche a noche) y los ocupantes menores de
 * 6 años esa noche ocupan cupo pero no pagan. La noche de salida no se cobra.
 * <p>
 * El cálculo es <b>recursivo</b> y recorre la estancia con su <b>Iterator</b>:
 * <pre>
 *   liquidar(noches) = 0                                   si no quedan noches  (caso base)
 *   liquidar(noches) = valor(siguiente) + liquidar(resto)  en otro caso         (caso recursivo)
 * </pre>
 */
public class LiquidadorTarifas {

    private final CalendarioTemporadas calendario;

    public LiquidadorTarifas(CalendarioTemporadas calendario) {
        if (calendario == null) {
            throw new IllegalArgumentException("El calendario de temporadas es obligatorio.");
        }
        this.calendario = calendario;
    }

    /**
     * Cotiza una estancia antes de crear la reserva.
     *
     * @param ocupantes      ocupantes con fecha de nacimiento (puede estar vacía)
     * @param numeroPersonas tamaño del grupo; los que no estén en {@code ocupantes} se cobran como adultos
     * @throws CapacidadExcedidaException si el grupo no cabe en el apartamento
     */
    public Cotizacion cotizar(Apartamento apartamento, Estancia estancia,
                              ListaDoble<Ocupante> ocupantes, int numeroPersonas) {
        if (apartamento == null || estancia == null) {
            throw new IllegalArgumentException("El apartamento y la estancia son obligatorios.");
        }
        if (!apartamento.soportaCapacidad(numeroPersonas)) {
            throw new CapacidadExcedidaException("El apartamento " + apartamento.getCodigo()
                    + " admite máximo " + apartamento.getCapacidadMaxima() + " personas.");
        }
        ListaDoble<Ocupante> conocidos = ocupantes == null ? new ListaDoble<>() : ocupantes;
        ListaDoble<NocheLiquidada> desglose = new ListaDoble<>();
        double total = liquidarDesde(estancia.iterator(), apartamento, conocidos, numeroPersonas, desglose);
        return new Cotizacion(apartamento, estancia, numeroPersonas, desglose, total);
    }

    /** Cotiza un grupo del que solo se conoce el tamaño (todos se cobran como adultos). */
    public Cotizacion cotizar(Apartamento apartamento, Estancia estancia, int numeroPersonas) {
        return cotizar(apartamento, estancia, null, numeroPersonas);
    }

    /** Cotiza una reserva existente con sus ocupantes registrados. */
    public Cotizacion cotizar(Reserva reserva) {
        return cotizar(reserva.getApartamento(), reserva.getEstancia(),
                reserva.getOcupantes(), reserva.getNumeroPersonas());
    }

    /**
     * Liquida la reserva y le asigna el valor total como costo.
     * @return el total liquidado
     */
    public double liquidar(Reserva reserva) {
        double total = cotizar(reserva).getTotal();
        reserva.setCostoTotal(total);
        return total;
    }

    /**
     * Método recursivo de la liquidación. Cada llamada toma una noche del iterador,
     * la agrega al desglose y suma su valor al de las noches restantes.
     *
     * @param noches iterador de la estancia, posicionado en la próxima noche por cobrar
     * @return valor de las noches que quedan en el iterador
     */
    private double liquidarDesde(Iterator<LocalDate> noches, Apartamento apartamento,
                                 ListaDoble<Ocupante> ocupantes, int numeroPersonas,
                                 ListaDoble<NocheLiquidada> desglose) {
        if (!noches.hasNext()) {
            return 0; // caso base: no quedan noches
        }
        LocalDate noche = noches.next();
        Temporada temporada = calendario.temporadaDe(noche);
        double tarifa = apartamento.getTarifa(temporada);
        int pagantes = Ocupante.contarPagantes(ocupantes, numeroPersonas, noche);

        NocheLiquidada liquidada = new NocheLiquidada(noche, temporada, tarifa, pagantes, numeroPersonas);
        desglose.agregarFinal(liquidada);

        // caso recursivo: valor de esta noche + valor del resto de la estancia
        return liquidada.getSubtotal()
                + liquidarDesde(noches, apartamento, ocupantes, numeroPersonas, desglose);
    }

    public CalendarioTemporadas getCalendario() {
        return calendario;
    }
}
