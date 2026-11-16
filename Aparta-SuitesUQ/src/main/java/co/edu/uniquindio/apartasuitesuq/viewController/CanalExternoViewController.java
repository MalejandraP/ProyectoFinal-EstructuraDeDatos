package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.EstadoSolicitud;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioCanalExterno;
import co.edu.uniquindio.apartasuitesuq.model.SolicitudExterna;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de canal-externo.fxml: la cola FIFO de solicitudes de la plataforma de terceros.
 * Se procesan en orden de llegada, una a una o todas; las que chocan con una reserva vigente se
 * rechazan y quedan como conflicto de canal.
 */
public class CanalExternoViewController implements ControladorPantalla {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("d MMM yyyy", VistaUtil.CO);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("hh:mm a", VistaUtil.CO);

    @FXML private TextField txtIdExterno;
    @FXML private ComboBox<Apartamento> cbApartamento;
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private ComboBox<Huesped> cbTitular;

    @FXML private Label lblContadorPendientes;
    @FXML private Label lblContadorConflictos;
    @FXML private Label lblContadorProcesadas;
    @FXML private TableView<SolicitudExterna> tablaPendientes;
    @FXML private TableColumn<SolicitudExterna, String> colPendId;
    @FXML private TableColumn<SolicitudExterna, String> colPendApto;
    @FXML private TableColumn<SolicitudExterna, String> colPendEntrada;
    @FXML private TableColumn<SolicitudExterna, String> colPendSalida;
    @FXML private TableColumn<SolicitudExterna, String> colPendPersonas;
    @FXML private TableColumn<SolicitudExterna, String> colPendTitular;
    @FXML private TableView<ConflictoCanal> tablaConflictos;
    @FXML private TableColumn<ConflictoCanal, LocalDateTime> colConfFecha;
    @FXML private TableColumn<ConflictoCanal, String> colConfSolicitud;
    @FXML private TableColumn<ConflictoCanal, String> colConfMotivo;
    @FXML private TableView<SolicitudExterna> tablaProcesadas;
    @FXML private TableColumn<SolicitudExterna, String> colProcId;
    @FXML private TableColumn<SolicitudExterna, String> colProcApto;
    @FXML private TableColumn<SolicitudExterna, String> colProcPersonas;
    @FXML private TableColumn<SolicitudExterna, String> colProcTitular;
    @FXML private TableColumn<SolicitudExterna, String> colProcEstado;
    @FXML private TableColumn<SolicitudExterna, String> colProcMotivo;

    private SGA sga;
    private ServicioCanalExterno servicio;

    @FXML
    private void initialize() {
        spPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        configurarColumnas();
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.sga = principal.getSga();
        this.servicio = sga.getCanalExterno();
        cbApartamento.setItems(VistaUtil.lista(sga.getApartamentos()));
        cbTitular.setItems(VistaUtil.lista(sga.getHuespedes()));
        cbApartamento.getSelectionModel().selectFirst();
        cbTitular.getSelectionModel().selectFirst();
        dpEntrada.setValue(sga.hoy().plusDays(10));
        dpSalida.setValue(sga.hoy().plusDays(12));
        txtIdExterno.setText("EXT-" + (200 + contar(servicio.getProcesadas()) + sga.getSolicitudesExternasPendientes()));
        refrescar();
    }

    @FXML
    private void onEncolar() {
        try {
            sga.recibirSolicitudExterna(new SolicitudExterna(txtIdExterno.getText().trim(), cbApartamento.getValue(),
                    dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue(), cbTitular.getValue()));
            txtIdExterno.setText("EXT-" + (200 + contar(servicio.getProcesadas()) + sga.getSolicitudesExternasPendientes()));
            refrescar();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo encolar la solicitud", ex);
        }
    }

    @FXML
    private void onProcesarSiguiente() {
        if (sga.getSolicitudesExternasPendientes() == 0) {
            VistaUtil.info("Cola vacía", "No hay solicitudes pendientes.");
            return;
        }
        try {
            SolicitudExterna solicitud = sga.procesarSolicitudCompleta(); // la más antigua (FIFO)
            VistaUtil.info("Solicitud " + solicitud.getIdExterno(), solicitud.getEstado() == EstadoSolicitud.ACEPTADA
                    ? "Aceptada: se creó la reserva " + solicitud.getReservaCreada().getCodigoReserva() + "."
                    : "Rechazada: " + solicitud.getMotivoRechazo());
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo procesar", ex);
        }
        refrescar();
    }

    @FXML
    private void onProcesarTodas() {
        int procesadas = 0;
        try {
            while (sga.getSolicitudesExternasPendientes() > 0) {
                sga.procesarSolicitudCompleta();
                procesadas++;
            }
            VistaUtil.info("Cola procesada", procesadas + " solicitudes procesadas en orden de llegada.");
        } catch (RuntimeException ex) {
            VistaUtil.error("Se detuvo el procesamiento", ex);
        }
        refrescar();
    }

    private void refrescar() {
        ObservableList<SolicitudExterna> pendientes = VistaUtil.lista(servicio.getPendientes());
        ObservableList<SolicitudExterna> procesadas = VistaUtil.lista(servicio.getProcesadas());
        ObservableList<ConflictoCanal> conflictos = VistaUtil.lista(sga.getConflictosCanal());
        tablaPendientes.setItems(pendientes);
        tablaProcesadas.setItems(procesadas);
        tablaConflictos.setItems(conflictos);
        lblContadorPendientes.setText(pendientes.size() + (pendientes.size() == 1 ? " pendiente" : " pendientes"));
        lblContadorProcesadas.setText(procesadas.size() + (procesadas.size() == 1 ? " procesada" : " procesadas"));
        lblContadorConflictos.setText("● " + conflictos.size() + (conflictos.size() == 1 ? " detectado" : " detectados"));
    }

    private static int contar(Iterable<?> datos) {
        int n = 0;
        for (Object ignored : datos) {
            n++;
        }
        return n;
    }

    private void configurarColumnas() {
        colPendId.setCellValueFactory(d -> texto(d.getValue().getIdExterno()));
        colPendApto.setCellValueFactory(d -> texto(d.getValue().getApartamento().getCodigo()));
        colPendEntrada.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getEntrada())));
        colPendSalida.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getSalida())));
        colPendPersonas.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumPersonas())));
        colPendTitular.setCellValueFactory(d -> texto(d.getValue().getTitular().getNombre()));

        colProcId.setCellValueFactory(d -> texto(d.getValue().getIdExterno()));
        colProcApto.setCellValueFactory(d -> texto(d.getValue().getApartamento().getCodigo()));
        colProcPersonas.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumPersonas())));
        colProcTitular.setCellValueFactory(d -> texto(d.getValue().getTitular().getNombre()));
        colProcEstado.setCellValueFactory(d -> texto(d.getValue().getEstado().toString()));
        colProcMotivo.setCellValueFactory(d -> texto(d.getValue().getMotivoRechazo() == null ? "" : d.getValue().getMotivoRechazo()));
        colProcEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(estado);
                chip.getStyleClass().addAll("chip", "ACEPTADA".equalsIgnoreCase(estado) ? "chip-preparado" : "chip-fuera");
                setGraphic(chip);
            }
        });
        colProcMotivo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String motivo, boolean vacio) {
                super.updateItem(motivo, vacio);
                if (vacio) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                String texto = motivo == null || motivo.isBlank() ? "—" : motivo;
                setText(texto);
                setTooltip(new Tooltip(texto));
            }
        });

        colConfFecha.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaRegistro()));
        colConfFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (vacio || fecha == null) {
                    setGraphic(null);
                    return;
                }
                Label hora = new Label(fecha.format(HORA));
                hora.getStyleClass().add("texto-suave");
                setGraphic(new VBox(2, new Label(fecha.format(DIA)), hora));
            }
        });
        colConfSolicitud.setCellValueFactory(d -> texto(d.getValue().getSolicitud().getIdExterno()));
        colConfMotivo.setCellValueFactory(d -> texto(d.getValue().getMotivo()));
        colConfMotivo.setCellFactory(columna -> new TableCell<>() {
            private final Label etiqueta = new Label();

            {
                etiqueta.setWrapText(true);
                etiqueta.maxWidthProperty().bind(colConfMotivo.widthProperty().subtract(16));
            }

            @Override
            protected void updateItem(String motivo, boolean vacio) {
                super.updateItem(motivo, vacio);
                etiqueta.setText(vacio ? null : (motivo == null || motivo.isBlank() ? "—" : motivo));
                setGraphic(vacio ? null : etiqueta);
            }
        });
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }
}
