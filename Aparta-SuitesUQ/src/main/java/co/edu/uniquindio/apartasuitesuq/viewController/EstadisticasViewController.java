package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Canal;
import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Estadisticas;
import co.edu.uniquindio.apartasuitesuq.model.Estancia;
import co.edu.uniquindio.apartasuitesuq.model.Indicador;
import co.edu.uniquindio.apartasuitesuq.model.RegistroPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;

/** Controlador de estadisticas.fxml (I): los reportes de {@link Estadisticas} para el periodo elegido. */
public class EstadisticasViewController implements ControladorPantalla {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblOcupacion;
    @FXML private Label lblIngresos;
    @FXML private Label lblConflictos;
    @FXML private Label lblPreparacion;
    @FXML private BarChart<String, Number> graficoOcupacionApto;
    @FXML private BarChart<String, Number> graficoOcupacionTemporada;
    @FXML private BarChart<String, Number> graficoIngresos;
    @FXML private TableView<Canal> tablaCanales;
    @FXML private TableColumn<Canal, String> colCanal;
    @FXML private TableColumn<Canal, String> colCanalTotal;
    @FXML private TableColumn<Canal, String> colPendiente;
    @FXML private TableColumn<Canal, String> colConfirmada;
    @FXML private TableColumn<Canal, String> colEnCurso;
    @FXML private TableColumn<Canal, String> colFinalizada;
    @FXML private TableColumn<Canal, String> colCancelada;
    @FXML private TableColumn<Canal, String> colNoShow;
    @FXML private TableView<ConflictoCanal> tablaConflictos;
    @FXML private TableColumn<ConflictoCanal, String> colConfSolicitud;
    @FXML private TableColumn<ConflictoCanal, String> colConfApto;
    @FXML private TableColumn<ConflictoCanal, String> colConfMotivo;
    @FXML private TableView<RegistroPreparacion> tablaTiempos;
    @FXML private TableColumn<RegistroPreparacion, String> colTiempoApto;
    @FXML private TableColumn<RegistroPreparacion, String> colTiempoMinutos;

    private SGA sga;
    private int[][] conteo = new int[Canal.values().length][EstadoReserva.values().length];

    @FXML
    private void initialize() {
        colCanal.setCellValueFactory(d -> texto(d.getValue().getDescripcion()));
        colCanalTotal.setCellValueFactory(d -> {
            int total = 0;
            for (int cantidad : conteo[d.getValue().ordinal()]) {
                total += cantidad;
            }
            return texto(String.valueOf(total));
        });
        columnaEstado(colPendiente, EstadoReserva.PENDIENTE);
        columnaEstado(colConfirmada, EstadoReserva.CONFIRMADA);
        columnaEstado(colEnCurso, EstadoReserva.ENCURSO);
        columnaEstado(colFinalizada, EstadoReserva.FINALIZADA);
        columnaEstado(colCancelada, EstadoReserva.CANCELADA);
        columnaEstado(colNoShow, EstadoReserva.NOSHOW);
        colConfSolicitud.setCellValueFactory(d -> texto(d.getValue().getSolicitud().getIdExterno()));
        colConfApto.setCellValueFactory(d -> texto(d.getValue().getSolicitud().getApartamento().getCodigo()));
        colConfMotivo.setCellValueFactory(d -> texto(d.getValue().getMotivo()));
        colTiempoApto.setCellValueFactory(d -> texto(d.getValue().getCodigoApartamento()));
        colTiempoMinutos.setCellValueFactory(d -> texto(d.getValue().getMinutos() + " min"));
    }

    private void columnaEstado(TableColumn<Canal, String> columna, EstadoReserva estado) {
        columna.setCellValueFactory(d -> texto(String.valueOf(conteo[d.getValue().ordinal()][estado.ordinal()])));
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.sga = principal.getSga();
        LocalDate inicioMes = sga.hoy().withDayOfMonth(1);
        dpDesde.setValue(inicioMes.minusMonths(1));
        dpHasta.setValue(inicioMes.plusMonths(2));
        onActualizar();
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    @FXML
    private void onActualizar() {
        Estancia periodo;
        try {
            periodo = new Estancia(dpDesde.getValue(), dpHasta.getValue());
        } catch (RuntimeException ex) {
            VistaUtil.error("Periodo inválido", ex);
            return;
        }
        Estadisticas estadisticas = sga.getEstadisticas();
        lblOcupacion.setText(VistaUtil.porcentaje(estadisticas.ocupacionGeneral(periodo)));
        lblIngresos.setText(VistaUtil.dinero(estadisticas.ingresosTotales(periodo)));
        lblConflictos.setText(String.valueOf(estadisticas.contarConflictosCanal()));
        lblPreparacion.setText(String.format("%.0f min", estadisticas.tiempoPromedioPreparacionMin()));

        llenar(graficoOcupacionApto, estadisticas.ocupacionPorApartamento(periodo));
        llenar(graficoOcupacionTemporada, estadisticas.ocupacionPorTemporada(periodo));
        llenar(graficoIngresos, estadisticas.ingresosPorMes(periodo));

        conteo = estadisticas.reservasPorCanalYEstado(periodo);
        tablaCanales.getItems().setAll(Canal.values());
        tablaCanales.refresh();
        tablaConflictos.setItems(VistaUtil.lista(estadisticas.getConflictosCanal()));
        tablaTiempos.setItems(VistaUtil.lista(sga.getPreparacionesTerminadas()));
    }

    /** Pasa las filas de un reporte (ListaDoble propia) a una serie del gráfico. */
    private static void llenar(BarChart<String, Number> grafico, Iterable<Indicador> filas) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Indicador fila : filas) {
            serie.getData().add(new XYChart.Data<>(fila.getEtiqueta(), fila.getValor()));
        }
        grafico.getData().setAll(serie);
    }
}
