package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;

/**
 * Resultado de cotizar una estancia en un apartamento: el valor total y su desglose
 * noche por noche, guardado en una {@link ListaDoble} de {@link NocheLiquidada}.
 * <p>
 * La crea {@code LiquidadorTarifas}; la GUI la muestra al huésped antes de reservar.
 */
public class Cotizacion {

    private final Apartamento apartamento;
    private final Estancia estancia;
    private final int numeroPersonas;
    private final ListaDoble<NocheLiquidada> desglose;
    private final double total;

    public Cotizacion(Apartamento apartamento, Estancia estancia, int numeroPersonas,
                      ListaDoble<NocheLiquidada> desglose, double total) {
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.numeroPersonas = numeroPersonas;
        this.desglose = desglose;
        this.total = total;
    }

    /** @return anticipo del 30 % que se paga en el canal Portal para confirmar la reserva */
    public double getAnticipo() {
        return total * Reserva.PORCENTAJE_ANTICIPO;
    }

    /** @return lo que queda por pagar después del anticipo */
    public double getSaldoTrasAnticipo() {
        return total - getAnticipo();
    }

    /** @return cuántas noches del desglose caen en la temporada indicada */
    public int contarNochesEn(Temporada temporada) {
        int contador = 0;
        for (NocheLiquidada noche : desglose) {
            if (noche.getTemporada() == temporada) {
                contador++;
            }
        }
        return contador;
    }

    public int getNumeroNoches() {
        return estancia.getNumeroNoches();
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public ListaDoble<NocheLiquidada> getDesglose() {
        return desglose;
    }

    public double getTotal() {
        return total;
    }

    /** @return texto con el encabezado, el desglose línea por línea y los totales */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cotización ").append(apartamento.getCodigo()).append(" - ").append(apartamento.getNombre())
                .append('\n').append("Estancia: ").append(estancia)
                .append(" | Personas: ").append(numeroPersonas).append('\n');
        for (NocheLiquidada noche : desglose) {
            sb.append("  ").append(noche).append('\n');
        }
        sb.append(String.format("Total: $%,.0f | Anticipo 30 %%: $%,.0f", total, getAnticipo()));
        return sb.toString();
    }
}
