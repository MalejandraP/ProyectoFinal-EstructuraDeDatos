package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Reportes del administrador, calculados recorriendo las estructuras del sistema:
 * <ul>
 *   <li>ocupación por apartamento y por temporada;</li>
 *   <li>ingresos por periodo (mes a mes);</li>
 *   <li>reservas por canal, incluidas las canceladas y los no-show;</li>
 *   <li>conflictos del canal externo;</li>
 *   <li>tiempo promedio de preparación.</li>
 * </ul>
 * Los periodos se expresan como {@link Estancia}: [desde, hasta), la fecha final no se incluye.
 * <p>
 * Una noche cuenta como <b>ocupada</b> (vendida) si pertenece a una reserva Confirmada, En curso
 * o Finalizada. Las Pendientes aún no están pagadas, y las Canceladas y No-show liberaron
 * sus noches.
 */
public class Estadisticas {

    private final SGA sga;

    public Estadisticas(SGA sga) {
        if (sga == null) {
            throw new IllegalArgumentException("El sistema es obligatorio.");
        }
        this.sga = sga;
    }

    /** @return true si las noches de la reserva cuentan como vendidas */
    public static boolean cuentaComoOcupada(Reserva reserva) {
        EstadoReserva estado = reserva.getEstado();
        return estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.ENCURSO
                || estado == EstadoReserva.FINALIZADA;
    }

    // ================================================================ ocupación

    /**
     * Porcentaje de noches vendidas de cada apartamento dentro del periodo.
     * @return una fila por apartamento, en orden de código (recorrido inorden del árbol)
     */
    public ListaDoble<Indicador> ocupacionPorApartamento(Estancia periodo) {
        Apartamento[] apartamentos = apartamentosEnOrden();
        int[] ocupadas = nochesVendidas(apartamentos, periodo);
        ListaDoble<Indicador> filas = new ListaDoble<>();
        int noches = periodo.getNumeroNoches();
        for (int i = 0; i < apartamentos.length; i++) {
            filas.agregarFinal(new Indicador(apartamentos[i].getCodigo(), porcentaje(ocupadas[i], noches)));
        }
        return filas;
    }

    /** @return noches vendidas de cada apartamento (mismo índice del arreglo) dentro del periodo */
    private int[] nochesVendidas(Apartamento[] apartamentos, Estancia periodo) {
        int[] ocupadas = new int[apartamentos.length];
        for (Reserva reserva : sga.getReservas()) {
            if (!cuentaComoOcupada(reserva)) {
                continue;
            }
            int indice = indiceDe(apartamentos, reserva.getApartamento());
            if (indice < 0) {
                continue;
            }
            for (LocalDate noche : reserva.getEstancia()) {
                if (periodo.contiene(noche)) {
                    ocupadas[indice]++;
                }
            }
        }
        return ocupadas;
    }

    /**
     * Porcentaje de ocupación de todo el alojamiento en cada temporada dentro del periodo:
     * noches vendidas en esa temporada / (noches de esa temporada × número de apartamentos).
     * @return una fila por temporada (Baja, Media, Alta)
     */
    public ListaDoble<Indicador> ocupacionPorTemporada(Estancia periodo) {
        Temporada[] temporadas = Temporada.values();
        int[] disponibles = new int[temporadas.length];
        int[] vendidas = new int[temporadas.length];
        int cantidadApartamentos = apartamentosEnOrden().length;
        CalendarioTemporadas calendario = sga.getCalendario();

        for (LocalDate noche : periodo) {
            disponibles[calendario.temporadaDe(noche).ordinal()] += cantidadApartamentos;
        }
        for (Reserva reserva : sga.getReservas()) {
            if (!cuentaComoOcupada(reserva)) {
                continue;
            }
            for (LocalDate noche : reserva.getEstancia()) {
                if (periodo.contiene(noche)) {
                    vendidas[calendario.temporadaDe(noche).ordinal()]++;
                }
            }
        }

        ListaDoble<Indicador> filas = new ListaDoble<>();
        for (Temporada temporada : temporadas) {
            filas.agregarFinal(new Indicador(temporada.getDescripcion(),
                    porcentaje(vendidas[temporada.ordinal()], disponibles[temporada.ordinal()])));
        }
        return filas;
    }

    /** @return porcentaje de ocupación de todo el alojamiento en el periodo */
    public double ocupacionGeneral(Estancia periodo) {
        Apartamento[] apartamentos = apartamentosEnOrden();
        int vendidas = 0;
        for (int noches : nochesVendidas(apartamentos, periodo)) {
            vendidas += noches;
        }
        return porcentaje(vendidas, periodo.getNumeroNoches() * apartamentos.length);
    }

    // ================================================================= ingresos

    /**
     * Ingresos por mes: cada noche vendida aporta su valor liquidado (tarifa de su temporada ×
     * personas que pagan) al mes en que cae. Una estancia que cruza de mes reparte su valor.
     * @return una fila por mes del periodo, etiquetada "AAAA-MM"
     */
    public ListaDoble<Indicador> ingresosPorMes(Estancia periodo) {
        YearMonth primero = YearMonth.from(periodo.getEntrada());
        YearMonth ultimo = YearMonth.from(periodo.getSalida().minusDays(1));
        int meses = (int) (ultimo.getYear() * 12L + ultimo.getMonthValue()
                - primero.getYear() * 12L - primero.getMonthValue()) + 1;
        double[] ingresos = new double[meses];

        for (Reserva reserva : sga.getReservas()) {
            if (!cuentaComoOcupada(reserva) || !reserva.seSolapaCon(periodo.getEntrada(), periodo.getSalida())) {
                continue;
            }
            for (NocheLiquidada noche : sga.getLiquidador().cotizar(reserva).getDesglose()) {
                if (periodo.contiene(noche.getNoche())) {
                    YearMonth mes = YearMonth.from(noche.getNoche());
                    int indice = (int) (mes.getYear() * 12L + mes.getMonthValue()
                            - primero.getYear() * 12L - primero.getMonthValue());
                    ingresos[indice] += noche.getSubtotal();
                }
            }
        }

        ListaDoble<Indicador> filas = new ListaDoble<>();
        for (int i = 0; i < meses; i++) {
            filas.agregarFinal(new Indicador(primero.plusMonths(i).toString(), ingresos[i]));
        }
        return filas;
    }

    /** @return suma de los ingresos del periodo */
    public double ingresosTotales(Estancia periodo) {
        double total = 0;
        for (Indicador mes : ingresosPorMes(periodo)) {
            total += mes.getValor();
        }
        return total;
    }

    // =========================================================== canales

    /**
     * Cuenta las reservas del periodo por canal y por estado.
     * @return matriz [canal.ordinal()][estado.ordinal()]
     */
    public int[][] reservasPorCanalYEstado(Estancia periodo) {
        int[][] conteo = new int[Canal.values().length][EstadoReserva.values().length];
        for (Reserva reserva : sga.getReservas()) {
            if (reserva.seSolapaCon(periodo.getEntrada(), periodo.getSalida())) {
                conteo[reserva.getCanal().ordinal()][reserva.getEstado().ordinal()]++;
            }
        }
        return conteo;
    }

    /** @return número total de reservas de cada canal en el periodo (todas, sin importar el estado) */
    public ListaDoble<Indicador> reservasPorCanal(Estancia periodo) {
        int[][] conteo = reservasPorCanalYEstado(periodo);
        ListaDoble<Indicador> filas = new ListaDoble<>();
        for (Canal canal : Canal.values()) {
            int total = 0;
            for (int cantidad : conteo[canal.ordinal()]) {
                total += cantidad;
            }
            filas.agregarFinal(new Indicador(canal.getDescripcion(), total));
        }
        return filas;
    }

    // ========================================== conflictos y preparación

    /** @return conflictos de canal registrados (solicitudes externas rechazadas por solapamiento) */
    public int contarConflictosCanal() {
        int total = 0;
        for (ConflictoCanal ignored : sga.getConflictosCanal()) {
            total++;
        }
        return total;
    }

    public ListaDoble<ConflictoCanal> getConflictosCanal() {
        return sga.getConflictosCanal();
    }

    /** @return minutos promedio que tarda una preparación (desde que entra a la cola hasta quedar Preparado) */
    public double tiempoPromedioPreparacionMin() {
        return sga.tiempoMedioPreparacionMin();
    }

    // ============================================================ utilidades

    /** Pasa el catálogo (árbol) a un arreglo, en inorden, para indexar los conteos. */
    private Apartamento[] apartamentosEnOrden() {
        Apartamento[] arreglo = new Apartamento[sga.getApartamentos().getTamano()];
        int i = 0;
        for (Apartamento apartamento : sga.getApartamentos()) {
            arreglo[i++] = apartamento;
        }
        return arreglo;
    }

    private static int indiceDe(Apartamento[] apartamentos, Apartamento buscado) {
        for (int i = 0; i < apartamentos.length; i++) {
            if (apartamentos[i].equals(buscado)) {
                return i;
            }
        }
        return -1;
    }

    private static double porcentaje(int parte, int total) {
        return total == 0 ? 0 : parte * 100.0 / total;
    }
}
