package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioReservas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.Locale;

public class FolioViewController {

    @FXML private Label lblTitulo, lblSubtitulo, lblEstadoReserva, lblUltimaOperacion;
    @FXML private Label lblTotalCargos, lblTotalPagos, lblSaldo;
    @FXML private ComboBox<Reserva> cmbReservas;

    @FXML private TableView<Movimiento> tablaMovimientos;
    @FXML private TableColumn<Movimiento, String> colFecha, colTipo, colDescripcion, colMonto;

    @FXML private Button btnDeshacer;
    @FXML private ToggleGroup grupoTipo;
    @FXML private ToggleButton btnCargo, btnPago;
    @FXML private TextField txtMonto, txtDescripcion;

    private static final Locale CO = new Locale("es", "CO");
    private ServicioReservas servicio;
    private Reserva actual;

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechayHora().toLocalDate() + " "
                        + c.getValue().getFechayHora().toLocalTime().withNano(0)));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTipo() + (c.getValue().isEsReverso() ? " (reverso)" : "")));
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colMonto.setCellValueFactory(c -> new SimpleStringProperty(dinero(c.getValue().getMonto())));

        cmbReservas.setConverter(new StringConverter<>() {
            @Override public String toString(Reserva r) {
                return r == null ? "" : r.getCodigoReserva() + " · " + r.getApartamento().getCodigo()
                        + " · " + r.getHuesped().getNombre();
            }
            @Override public Reserva fromString(String s) { return null; }
        });
        cmbReservas.valueProperty().addListener((o, a, nueva) -> mostrar(nueva));
        mostrar(null);
    }

    /** Lo llama VentanaPrincipal al abrir la pantalla. */
    public void setServicio(ServicioReservas servicio) {
        this.servicio = servicio;
        ObservableList<Reserva> opciones = FXCollections.observableArrayList();
        for (Reserva r : servicio.getReservas()) {
            opciones.add(r);
        }
        cmbReservas.setItems(opciones);
    }

    private void mostrar(Reserva r) {
        actual = r;
        boolean hay = r != null;
        txtMonto.setDisable(!hay);
        txtDescripcion.setDisable(!hay);
        btnCargo.setDisable(!hay);
        btnPago.setDisable(!hay);
        if (!hay) {
            lblTitulo.setText("Folio");
            lblSubtitulo.setText("Elige una reserva para ver su folio");
            lblEstadoReserva.setText("—");
            tablaMovimientos.setItems(FXCollections.observableArrayList());
            lblUltimaOperacion.setText("—");
            lblTotalCargos.setText("$0");
            lblTotalPagos.setText("$0");
            lblSaldo.setText("$0");
            btnDeshacer.setDisable(true);
            return;
        }
        Folio folio = r.getFolio();
        lblTitulo.setText("Folio · " + r.getCodigoReserva());
        lblSubtitulo.setText(r.getApartamento().getCodigo() + " " + r.getApartamento().getNombre()
                + " · Titular: " + r.getHuesped().getNombre());
        lblEstadoReserva.setText(r.getEstado().getDescripcion());

        ObservableList<Movimiento> filas = FXCollections.observableArrayList();
        double cargos = 0, pagos = 0;
        for (Movimiento m : folio.getMovimientos()) {
            filas.add(m);
            if (m.getTipo() == TipoMovimiento.CARGO) cargos += m.getMonto();
            else pagos += m.getMonto();
        }
        tablaMovimientos.setItems(filas);
        lblTotalCargos.setText(dinero(cargos));
        lblTotalPagos.setText(dinero(pagos));
        lblSaldo.setText(dinero(folio.calcularSaldo()));

        Movimiento ultima = folio.ultimaOperacion();
        lblUltimaOperacion.setText(ultima == null ? "—"
                : ultima.getTipo() + " " + dinero(ultima.getMonto()) + " · " + ultima.getDescripcion());
        btnDeshacer.setDisable(ultima == null);
    }

    @FXML
    private void onRegistrar() {
        if (actual == null) return;
        if (actual.getEstado() == EstadoReserva.CANCELADA || actual.getEstado() == EstadoReserva.NOSHOW) {
            aviso("La reserva " + actual.getCodigoReserva() + " está "
                    + actual.getEstado().getDescripcion() + ": no admite nuevos movimientos.");
            return;
        }
        String soloDigitos = txtMonto.getText() == null ? "" : txtMonto.getText().replaceAll("[^0-9]", "");
        if (soloDigitos.isEmpty()) {
            aviso("Escribe un monto válido en pesos.");
            return;
        }
        double monto = Double.parseDouble(soloDigitos);
        String descripcion = txtDescripcion.getText() == null ? "" : txtDescripcion.getText().trim();
        try {
            if (btnPago.isSelected()) {
                actual.getFolio().registrarPago(monto, descripcion.isEmpty() ? "Pago en recepción" : descripcion);
            } else {
                actual.getFolio().registrarCargo(monto, descripcion.isEmpty() ? "Cargo en recepción" : descripcion);
            }
            txtMonto.clear();
            txtDescripcion.clear();
            mostrar(actual);
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
        }
    }

    @FXML
    private void onDeshacer() {
        if (actual == null) return;
        try {
            actual.getFolio().deshacer();
            mostrar(actual);
        } catch (EstructuraVaciaException ex) {
            aviso("No hay operaciones para deshacer.");
        }
    }

    private String dinero(double v) {
        return "$" + String.format(CO, "%,.0f", v);
    }

    private void aviso(String texto) {
        new Alert(Alert.AlertType.WARNING, texto).showAndWait();
    }
}