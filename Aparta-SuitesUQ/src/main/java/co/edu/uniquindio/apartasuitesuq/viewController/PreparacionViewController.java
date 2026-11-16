package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.RegistroPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ColaPreparacion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;

/**
 * Controlador de preparacion.fxml (E). Muestra la cola de prioridad en orden de atención,
 * despacha el siguiente apartamento, lo marca Preparado, calcula la ruta sugerida y lista los
 * tiempos registrados.
 */
public class PreparacionViewController implements ControladorPantalla {

    @FXML private Label lblEnCola;
    @FXML private Label lblEnPreparacion;
    @FXML private Label lblPromedio;
    @FXML private TableView<ColaPreparacion.Item> tablaCola;
    @FXML private TableColumn<ColaPreparacion.Item, String> colOrden;
    @FXML private TableColumn<ColaPreparacion.Item, String> colPrioridad;
    @FXML private TableColumn<ColaPreparacion.Item, String> colApto;
    @FXML private TableColumn<ColaPreparacion.Item, String> colMotivo;
    @FXML private TableColumn<ColaPreparacion.Item, String> colIngreso;
    @FXML private Button btnDespachar;
    @FXML private TableView<ColaPreparacion.Item> tablaEnPreparacion;
    @FXML private TableColumn<ColaPreparacion.Item, String> colEnPrepApto;
    @FXML private TableColumn<ColaPreparacion.Item, String> colEnPrepDesde;
    @FXML private Label lblRuta;
    @FXML private Label lblDistancia;
    @FXML private TableView<RegistroPreparacion> tablaTiempos;
    @FXML private TableColumn<RegistroPreparacion, String> colTiempoApto;
    @FXML private TableColumn<RegistroPreparacion, String> colTiempoFin;
    @FXML private TableColumn<RegistroPreparacion, String> colTiempoMinutos;

    private PrincipalViewController principal;
    private SGA sga;

    @FXML
    private void initialize() {
        colOrden.setCellValueFactory(d -> texto(String.valueOf(tablaCola.getItems().indexOf(d.getValue()) + 1)));
        colPrioridad.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getPrioridad())));
        colPrioridad.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String prioridad, boolean vacio) {
                super.updateItem(prioridad, vacio);
                if (vacio || prioridad == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label("Prioridad " + prioridad);
                chip.getStyleClass().addAll("chip", "1".equals(prioridad) ? "chip-pendiente" : "chip-neutro");
                setGraphic(chip);
            }
        });
        colApto.setCellValueFactory(d -> texto(d.getValue().getApartamento().getCodigo() + " · "
                + d.getValue().getApartamento().getNombre()));
        colMotivo.setCellValueFactory(d -> texto(motivo(d.getValue())));
        colIngreso.setCellValueFactory(d -> texto(momento(d.getValue().getIngreso())));
        colEnPrepApto.setCellValueFactory(d -> texto(d.getValue().getApartamento().getCodigo() + " · "
                + d.getValue().getApartamento().getNombre()));
        colEnPrepDesde.setCellValueFactory(d -> texto(momento(d.getValue().getIngreso())));
        colTiempoApto.setCellValueFactory(d -> texto(d.getValue().getCodigoApartamento()));
        colTiempoFin.setCellValueFactory(d -> texto(momento(d.getValue().getFin())));
        colTiempoMinutos.setCellValueFactory(d -> texto(d.getValue().getMinutos() + " min"));
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.principal = principal;
        this.sga = principal.getSga();
        refrescar();
    }

    private String motivo(ColaPreparacion.Item item) {
        if (item.getPrioridad() == 1) {
            Reserva entrada = sga.entradaDeHoy(item.getApartamento());
            return "Entrada hoy a las " + VistaUtil.hora(item.getHoraClave().toLocalTime())
                    + (entrada == null ? "" : " (" + entrada.getCodigoReserva() + ")");
        }
        return "Sin entrada hoy";
    }

    private static String momento(LocalDateTime momento) {
        return VistaUtil.fechaCorta(momento.toLocalDate()) + " " + VistaUtil.hora(momento.toLocalTime());
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    private void refrescar() {
        ObservableList<ColaPreparacion.Item> cola = VistaUtil.lista(sga.colaEnOrden());
        tablaCola.setItems(cola);
        tablaEnPreparacion.setItems(VistaUtil.lista(sga.getEnPreparacion()));
        tablaTiempos.setItems(VistaUtil.lista(sga.getPreparacionesTerminadas()));
        lblEnCola.setText(String.valueOf(cola.size()));
        lblEnPreparacion.setText(String.valueOf(tablaEnPreparacion.getItems().size()));
        lblPromedio.setText(String.format("%.0f min", sga.tiempoMedioPreparacionMin()));
        btnDespachar.setDisable(cola.isEmpty());
        mostrarRuta();
    }

    private void mostrarRuta() {
        try {
            Recorrido<String> ruta = sga.rutaPreparacion();
            if (ruta.tamano() <= 1) {
                lblRuta.setText("No hay apartamentos pendientes por recorrer.");
                lblDistancia.setText("");
                return;
            }
            StringBuilder camino = new StringBuilder();
            for (String nodo : ruta) {
                camino.append(camino.length() == 0 ? "" : "  →  ").append(nodo);
            }
            lblRuta.setText(camino.toString());
            lblDistancia.setText("Distancia total: " + String.format("%.0f m", ruta.getDistancia()));
        } catch (RuntimeException ex) {
            lblRuta.setText("No se puede calcular la ruta: " + ex.getMessage());
            lblDistancia.setText("");
        }
    }

    @FXML
    private void onDespachar() {
        try {
            ColaPreparacion.Item item = sga.iniciarSiguientePreparacion();
            VistaUtil.info("Preparación iniciada", item.getApartamento().getCodigo() + " pasó a En preparación.");
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo despachar", ex);
        }
        refrescar();
    }

    @FXML
    private void onTerminar() {
        ColaPreparacion.Item item = tablaEnPreparacion.getSelectionModel().getSelectedItem();
        if (item == null) {
            item = tablaEnPreparacion.getItems().isEmpty() ? null : tablaEnPreparacion.getItems().get(0);
        }
        if (item == null) {
            VistaUtil.info("Nada que terminar", "No hay apartamentos en preparación.");
            return;
        }
        try {
            sga.terminarPreparacion(item);
            VistaUtil.info("Apartamento preparado", item.getApartamento().getCodigo() + " ya puede recibir huéspedes.");
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo terminar la preparación", ex);
        }
        refrescar();
    }

    @FXML
    private void onVerEnMapa() {
        principal.abrirMapa(null, true);
    }

    @FXML
    private void onActualizar() {
        refrescar();
    }
}
