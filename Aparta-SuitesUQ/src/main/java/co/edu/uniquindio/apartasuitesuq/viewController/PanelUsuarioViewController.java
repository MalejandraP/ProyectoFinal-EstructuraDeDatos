package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PanelUsuarioViewController {
    @FXML
    private Label lblIniciales;
    @FXML private Label lblNombre;
    @FXML private Label lblCedula;
    @FXML private Label lblTelefono;
    @FXML private Label lblCorreo;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colCodigo;
    @FXML private TableColumn<Reserva, String> colApartamento;
    @FXML private TableColumn<Reserva, String> colEntrada;
    @FXML private TableColumn<Reserva, String> colSalida;
    @FXML private TableColumn<Reserva, String> colEstado;
    @FXML private TableColumn<Reserva, String> colTotal;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoReserva()));
        colApartamento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApartamento().getNombre()));
        colEntrada.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaInicio().toString()));
        colSalida.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaFin().toString()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().toString()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getCostoTotal()));
    }

    public void setReservas(ListaDoble<Reserva> reservas) {
        ObservableList<Reserva> filas = FXCollections.observableArrayList();
        for (Reserva r : reservas) {          // usa el Iterator de tu lista
            filas.add(r);
        }
        tablaReservas.setItems(filas);
    }

    public void setHuesped(Huesped huesped) {
        lblNombre.setText(huesped.getNombre());
        lblIniciales.setText(iniciales(huesped.getNombre()));
        lblCedula.setText(huesped.getCedula());
        lblTelefono.setText(huesped.getTelefono());
        lblCorreo.setText(huesped.getCorreo());
    }

    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        String resultado = partes[0].substring(0, 1);
        if (partes.length > 1) {
            resultado += partes[1].substring(0, 1);
        }
        return resultado.toUpperCase();
    }
}
