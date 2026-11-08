package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ColaPrioridad;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Iterator;

public class ColaPreparacion implements Iterable<ColaPreparacion.Item> {


    @FunctionalInterface
    public interface ConsultaEntradas {
        LocalDateTime proximaEntradaHoy(Apartamento apartamento);
    }


    public static class Item {
        private final Apartamento apartamento;
        private final int prioridad;
        private final LocalDateTime horaClave;
        private final LocalDateTime ingreso;

        Item(Apartamento apartamento, int prioridad, LocalDateTime horaClave, LocalDateTime ingreso) {
            this.apartamento = apartamento;
            this.prioridad = prioridad;
            this.horaClave = horaClave;
            this.ingreso = ingreso;
        }

        public Apartamento getApartamento() { return apartamento; }
        public int getPrioridad() { return prioridad; }
        public LocalDateTime getHoraClave() { return horaClave; }
        public LocalDateTime getIngreso() { return ingreso; }
    }

    private final ConsultaEntradas consulta;
    private final ColaPrioridad<Item> cola;

    public ColaPreparacion(ConsultaEntradas consulta) {
        this.consulta = consulta;
        // Comparator combinado con thenComparing (exigido por el enunciado)
        Comparator<Item> orden = Comparator
                .comparingInt(Item::getPrioridad)
                .thenComparing(Item::getHoraClave)
                .thenComparing(i -> i.getApartamento().getCodigo());
        this.cola = new ColaPrioridad<>(orden);
    }

    public void encolar(Apartamento apartamento, LocalDateTime ahora) {
        LocalDateTime entrada = consulta.proximaEntradaHoy(apartamento);
        if (entrada != null) {
            cola.encolar(new Item(apartamento, 1, entrada, ahora));
        } else {
            cola.encolar(new Item(apartamento, 2, ahora, ahora));
        }
    }

    public Item siguiente() { return cola.desencolar(); }

    public Item ver() { return cola.ver(); }

    public boolean estaVacia() { return cola.estaVacia(); }

    public int tamano() { return cola.tamano(); }


    @Override
    public Iterator<Item> iterator() { return cola.iterator(); }
}
