package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.PeriodoTemporada;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Temporada;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.temporal.ChronoUnit;

/** Controlador de temporadas.fxml: periodos del calendario de temporadas y tarifas por apartamento. */
public class TemporadasViewController implements ControladorPantalla {

    @FXML private TableView<PeriodoTemporada> tablaPeriodos;
    @FXML private TableColumn<PeriodoTemporada, String> colPerTemporada;
    @FXML private TableColumn<PeriodoTemporada, String> colPerDesde;
    @FXML private TableColumn<PeriodoTemporada, String> colPerHasta;
    @FXML private TableColumn<PeriodoTemporada, String> colPerDias;
    @FXML private ComboBox<Temporada> cbTemporada;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private DatePicker dpConsulta;
    @FXML private Label lblConsulta;
    @FXML private TableView<Apartamento> tablaTarifas;
    @FXML private TableColumn<Apartamento, String> colTarCodigo;
    @FXML private TableColumn<Apartamento, String> colTarNombre;
    @FXML private TableColumn<Apartamento, String> colTarBaja;
    @FXML private TableColumn<Apartamento, String> colTarMedia;
    @FXML private TableColumn<Apartamento, String> colTarAlta;
    @FXML private Label lblAptoTarifa;
    @FXML private TextField txtBaja;
    @FXML private TextField txtMedia;
    @FXML private TextField txtAlta;

    private SGA sga;

    @FXML
    private void initialize() {
        colPerTemporada.setCellValueFactory(d -> texto(d.getValue().getTemporada().getDescripcion()));
        colPerDesde.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getInicio())));
        colPerHasta.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getFin())));
        colPerDias.setCellValueFactory(d -> texto(String.valueOf(
                ChronoUnit.DAYS.between(d.getValue().getInicio(), d.getValue().getFin()) + 1)));
        colTarCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigo()));
        colTarNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colTarBaja.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getTarifa(Temporada.BAJA))));
        colTarMedia.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getTarifa(Temporada.MEDIA))));
        colTarAlta.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getTarifa(Temporada.ALTA))));
        cbTemporada.getItems().setAll(Temporada.MEDIA, Temporada.ALTA, Temporada.BAJA);
        cbTemporada.getSelectionModel().selectFirst();
        tablaTarifas.getSelectionModel().selectedItemProperty().addListener((o, a, apto) -> {
            if (apto != null) {
                lblAptoTarifa.setText(apto.toString());
                txtBaja.setText(String.format("%.0f", apto.getTarifa(Temporada.BAJA)));
                txtMedia.setText(String.format("%.0f", apto.getTarifa(Temporada.MEDIA)));
                txtAlta.setText(String.format("%.0f", apto.getTarifa(Temporada.ALTA)));
            }
        });
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.sga = principal.getSga();
        dpDesde.setValue(sga.hoy());
        dpHasta.setValue(sga.hoy().plusDays(7));
        dpConsulta.setValue(sga.hoy());
        tablaTarifas.setItems(VistaUtil.lista(sga.getApartamentos()));
        refrescarPeriodos();
        onConsultar();
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    private void refrescarPeriodos() {
        tablaPeriodos.setItems(VistaUtil.lista(sga.getCalendario().getPeriodos()));
    }

    @FXML
    private void onAgregarPeriodo() {
        try {
            sga.definirTemporada(cbTemporada.getValue(), dpDesde.getValue(), dpHasta.getValue());
            refrescarPeriodos();
            onConsultar();
        } catch (RuntimeException ex) {
            VistaUtil.error("Periodo inválido", ex);
        }
    }

    @FXML
    private void onEliminarPeriodo() {
        PeriodoTemporada periodo = tablaPeriodos.getSelectionModel().getSelectedItem();
        if (periodo != null && VistaUtil.confirmar("Eliminar periodo", "Esas fechas volverán a ser temporada Baja. ¿Continuar?")) {
            sga.eliminarPeriodo(periodo);
            refrescarPeriodos();
            onConsultar();
        }
    }

    @FXML
    private void onConsultar() {
        if (dpConsulta.getValue() == null) {
            return;
        }
        Temporada temporada = sga.getCalendario().temporadaDe(dpConsulta.getValue());
        lblConsulta.setText("Temporada " + temporada.getDescripcion());
        lblConsulta.getStyleClass().setAll("chip", temporada == Temporada.ALTA ? "chip-fuera"
                : temporada == Temporada.MEDIA ? "chip-pendiente" : "chip-preparado");
    }

    @FXML
    private void onGuardarTarifas() {
        Apartamento apto = tablaTarifas.getSelectionModel().getSelectedItem();
        if (apto == null) {
            VistaUtil.info("Elige un apartamento", "Selecciona un apartamento de la tabla.");
            return;
        }
        try {
            double baja = numero(txtBaja);
            double media = numero(txtMedia);
            double alta = numero(txtAlta);
            sga.definirTarifa(apto.getCodigo(), Temporada.BAJA, baja);
            sga.definirTarifa(apto.getCodigo(), Temporada.MEDIA, media);
            sga.definirTarifa(apto.getCodigo(), Temporada.ALTA, alta);
            tablaTarifas.refresh();
        } catch (RuntimeException ex) {
            VistaUtil.error("Tarifa inválida", ex);
        }
    }

    private static double numero(TextField campo) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Escribe las tarifas como números enteros (pesos).");
        }
    }
}
