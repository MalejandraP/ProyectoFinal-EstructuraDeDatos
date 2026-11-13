package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Canal;
import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Estadisticas;
import co.edu.uniquindio.apartasuitesuq.model.Estancia;
import co.edu.uniquindio.apartasuitesuq.model.Indicador;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Reportes del administrador: ocupación por apartamento y por temporada, ingresos por mes,
 * reservas por canal (incluidas canceladas y no-show), conflictos de canal y tiempo promedio
 * de preparación, para el periodo elegido.
 */
public class EstadisticasView {

    private final SGA sga;
    private final DatePicker desde = new DatePicker();
    private final DatePicker hasta = new DatePicker();
    private final FlowPane indicadores = new FlowPane(12, 12);
    private final BarChart<String, Number> ocupacionApartamento = grafico("Ocupación por apartamento", "%");
    private final BarChart<String, Number> ocupacionTemporada = grafico("Ocupación por temporada", "%");
    private final BarChart<String, Number> ingresos = grafico("Ingresos por mes", "COP");
    private final TableView<Canal> canales = new TableView<>();
    private final ListView<String> conflictos = new ListView<>();
    private int[][] conteoCanales = new int[0][0];
    private final Node vista;

    public EstadisticasView(SGA sga) {
        this.sga = sga;
        this.vista = construir();
    }

    public Node getVista() {
        return vista;
    }

    private Node construir() {
        LocalDate inicioMes = sga.hoy().withDayOfMonth(1);
        desde.setValue(inicioMes.minusMonths(1));
        hasta.setValue(inicioMes.plusMonths(2));
        Button actualizar = new Button("Actualizar");
        actualizar.getStyleClass().add("boton-primario");
        actualizar.setOnAction(e -> actualizar());
        HBox filtro = new HBox(10, new Label("Desde"), desde, new Label("Hasta (sin incluir)"), hasta, actualizar);
        filtro.setAlignment(Pos.CENTER_LEFT);

        configurarTablaCanales();
        conflictos.setPrefHeight(160);
        conflictos.setPlaceholder(new Label("Sin conflictos de canal"));

        GridPane graficos = new GridPane();
        graficos.setHgap(16);
        graficos.setVgap(16);
        VBox tarjetaApartamento = Vistas.tarjeta(null, ocupacionApartamento);
        VBox tarjetaTemporada = Vistas.tarjeta(null, ocupacionTemporada);
        VBox tarjetaIngresos = Vistas.tarjeta(null, ingresos);
        VBox tarjetaCanales = Vistas.tarjeta("Reservas por canal y estado", canales);
        graficos.add(tarjetaApartamento, 0, 0);
        graficos.add(tarjetaTemporada, 1, 0);
        graficos.add(tarjetaIngresos, 0, 1);
        graficos.add(tarjetaCanales, 1, 1);
        for (Node n : graficos.getChildren()) {
            GridPane.setHgrow(n, Priority.ALWAYS);
        }

        actualizar();
        return Vistas.pagina("Estadísticas", "Calculadas recorriendo el árbol de reservas y cada estancia "
                        + "noche por noche. Una noche cuenta como ocupada si su reserva está Confirmada, "
                        + "En curso o Finalizada.",
                filtro, indicadores, graficos, Vistas.tarjeta("Conflictos del canal externo", conflictos));
    }

    private void actualizar() {
        Estancia periodo;
        try {
            periodo = new Estancia(desde.getValue(), hasta.getValue());
        } catch (RuntimeException ex) {
            Vistas.error("Periodo inválido", ex);
            return;
        }
        Estadisticas estadisticas = sga.getEstadisticas();

        indicadores.getChildren().setAll(
                indicador("Ocupación general", Vistas.porcentaje(estadisticas.ocupacionGeneral(periodo))),
                indicador("Ingresos del periodo", Vistas.moneda(estadisticas.ingresosTotales(periodo))),
                indicador("Conflictos de canal", String.valueOf(estadisticas.contarConflictosCanal())),
                indicador("Tiempo promedio de preparación",
                        String.format("%.0f min", estadisticas.tiempoPromedioPreparacionMin())));

        llenar(ocupacionApartamento, estadisticas.ocupacionPorApartamento(periodo));
        llenar(ocupacionTemporada, estadisticas.ocupacionPorTemporada(periodo));
        llenar(ingresos, estadisticas.ingresosPorMes(periodo));

        conteoCanales = estadisticas.reservasPorCanalYEstado(periodo);
        canales.getItems().setAll(Canal.values());
        canales.refresh();

        conflictos.getItems().clear();
        for (ConflictoCanal conflicto : estadisticas.getConflictosCanal()) {
            conflictos.getItems().add(conflicto.getSolicitud().getIdExterno() + " · "
                    + conflicto.getSolicitud().getCodigoApartamento().getCodigo() + " · " + conflicto.getMotivo());
        }
    }

    private void configurarTablaCanales() {
        TableColumn<Canal, String> canal = new TableColumn<>("Canal");
        canal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        canales.getColumns().add(canal);
        TableColumn<Canal, String> total = new TableColumn<>("Total");
        total.setCellValueFactory(d -> {
            int suma = 0;
            for (int cantidad : conteoCanales[d.getValue().ordinal()]) {
                suma += cantidad;
            }
            return new SimpleStringProperty(String.valueOf(suma));
        });
        canales.getColumns().add(total);
        for (EstadoReserva estado : EstadoReserva.values()) {
            TableColumn<Canal, String> columna = new TableColumn<>(estado.getDescripcion());
            columna.setCellValueFactory(d -> new SimpleStringProperty(
                    String.valueOf(conteoCanales[d.getValue().ordinal()][estado.ordinal()])));
            canales.getColumns().add(columna);
        }
        canales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        canales.setPrefHeight(170);
    }

    private static BarChart<String, Number> grafico(String titulo, String unidad) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel(unidad);
        BarChart<String, Number> grafico = new BarChart<>(x, y);
        grafico.setTitle(titulo);
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setPrefHeight(300);
        return grafico;
    }

    /** Pasa las filas de un reporte (ListaDoble propia) a una serie del gráfico. */
    private static void llenar(BarChart<String, Number> grafico, Iterable<Indicador> filas) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Indicador fila : filas) {
            serie.getData().add(new XYChart.Data<>(fila.getEtiqueta(), fila.getValor()));
        }
        grafico.getData().setAll(serie);
    }

    private static VBox indicador(String titulo, String valor) {
        Label numero = Vistas.texto(valor, "titulo-pagina");
        VBox caja = new VBox(4, Vistas.texto(titulo, "texto-suave"), numero);
        caja.getStyleClass().add("tarjeta");
        caja.setMinWidth(220);
        return caja;
    }
}
