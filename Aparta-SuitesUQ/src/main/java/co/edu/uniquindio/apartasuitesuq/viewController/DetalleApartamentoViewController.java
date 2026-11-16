package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Temporada;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Controlador de detalle-apartamento.fxml: ficha completa del apartamento y sus reservas. */
public class DetalleApartamentoViewController {

    @FXML private Label lblTitulo;
    @FXML private Label lblEstado;
    @FXML private Label lblNombre;
    @FXML private Label lblUbicacion;
    @FXML private Label lblCapacidad;
    @FXML private Label lblCaracteristicas;
    @FXML private Label lblTarifas;
    @FXML private Label lblMotivo;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colCodigo;
    @FXML private TableColumn<Reserva, String> colTitular;
    @FXML private TableColumn<Reserva, String> colEntrada;
    @FXML private TableColumn<Reserva, String> colSalida;
    @FXML private TableColumn<Reserva, String> colEstado;
    @FXML private TableColumn<Reserva, String> colTotal;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoReserva()));
        colTitular.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHuesped().getNombre()));
        colEntrada.setCellValueFactory(d -> new SimpleStringProperty(VistaUtil.fecha(d.getValue().getFechaInicio())));
        colSalida.setCellValueFactory(d -> new SimpleStringProperty(VistaUtil.fecha(d.getValue().getFechaFin())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().getDescripcion()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(VistaUtil.dinero(d.getValue().getCostoTotal())));
    }

    void mostrar(Apartamento apartamento, SGA sga) {
        lblTitulo.setText(apartamento.getCodigo() + " · " + apartamento.getNombre());
        lblEstado.setText(apartamento.getEstadoOperativo().getDescripcion());
        lblEstado.getStyleClass().setAll("chip", "chip-" + VistaUtil.claseEstado(apartamento.getEstadoOperativo()));
        lblNombre.setText(apartamento.getNombre());
        lblUbicacion.setText("Bloque " + apartamento.getBloque() + " · Piso " + apartamento.getPiso());
        lblCapacidad.setText(apartamento.getNumeroDormitorios() + " dormitorios · "
                + apartamento.getCapacidadMaxima() + " personas");
        lblCaracteristicas.setText(apartamento.getCaracteristicas().isEmpty() ? "—" : apartamento.getCaracteristicas());
        StringBuilder tarifas = new StringBuilder();
        for (Temporada temporada : Temporada.values()) {
            if (tarifas.length() > 0) {
                tarifas.append("  ·  ");
            }
            tarifas.append(temporada.getDescripcion()).append(' ').append(VistaUtil.dinero(apartamento.getTarifa(temporada)));
        }
        lblTarifas.setText(tarifas.toString());
        lblMotivo.setText(apartamento.estaFueraDeServicio() ? sga.motivoFueraDeServicio(apartamento) : "—");
        tablaReservas.setItems(VistaUtil.lista(sga.reservasDe(apartamento)));
    }
}
