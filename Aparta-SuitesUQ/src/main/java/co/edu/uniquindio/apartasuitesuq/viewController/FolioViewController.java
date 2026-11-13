package co.edu.uniquindio.apartasuitesuq.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FolioViewController {

    // Los nombres deben ser iguales a los fx:id del FXML
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblEstadoReserva;
    @FXML private Label lblUltimaOperacion;
    @FXML private Label lblTotalCargos;
    @FXML private Label lblTotalPagos;
    @FXML private Label lblSaldo;

    @FXML private TableView<?> tablaMovimientos;   // luego será TableView<Movimiento>
    @FXML private TableColumn<?, ?> colFecha;
    @FXML private TableColumn<?, ?> colTipo;
    @FXML private TableColumn<?, ?> colDescripcion;
    @FXML private TableColumn<?, ?> colMonto;

    @FXML private Button btnDeshacer;
    @FXML private ToggleGroup grupoTipo;
    @FXML private ToggleButton btnCargo;
    @FXML private ToggleButton btnPago;
    @FXML private TextField txtMonto;
    @FXML private TextField txtDescripcion;

    @FXML
    private void initialize() {
        // aquí se configuran las columnas y se carga el folio
    }

    @FXML
    private void onDeshacer() {
        // TODO: folio.deshacerUltimo(); y refrescar la tabla
    }

    @FXML
    private void onRegistrar() {
        // TODO: leer txtMonto y txtDescripcion, registrar cargo o pago, refrescar
    }
}