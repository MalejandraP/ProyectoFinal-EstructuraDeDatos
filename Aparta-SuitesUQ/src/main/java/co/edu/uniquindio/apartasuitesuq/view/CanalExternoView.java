package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioCanalExterno;
import co.edu.uniquindio.apartasuitesuq.model.SolicitudExterna;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class CanalExternoView {

    private final SGA sga;
    private final ServicioCanalExterno servicio;
    private final Node vista;

    @FXML private Label lblContadorPendientes;
    @FXML private Label lblContadorProcesadas;
    @FXML private Label lblContadorConflictos;

    // Tabla Pendientes
    @FXML private TableView<SolicitudExterna> tablaPendientes;
    @FXML private TableColumn<SolicitudExterna, String> colPendId;
    @FXML private TableColumn<SolicitudExterna, String> colPendApto;
    @FXML private TableColumn<SolicitudExterna, String> colPendEntrada;
    @FXML private TableColumn<SolicitudExterna, String> colPendSalida;
    @FXML private TableColumn<SolicitudExterna, String> colPendPersonas;
    @FXML private TableColumn<SolicitudExterna, String> colPendTitular;

    // Tabla Procesadas
    @FXML private TableView<SolicitudExterna> tablaProcesadas;
    @FXML private TableColumn<SolicitudExterna, String> colProcId;
    @FXML private TableColumn<SolicitudExterna, String> colProcApto;
    @FXML private TableColumn<SolicitudExterna, String> colProcPersonas;
    @FXML private TableColumn<SolicitudExterna, String> colProcTitular;
    @FXML private TableColumn<SolicitudExterna, String> colProcEstado;
    @FXML private TableColumn<SolicitudExterna, String> colProcMotivo;

    // Tabla Conflictos
    @FXML private TableView<ConflictoCanal> tablaConflictos;
    @FXML private TableColumn<ConflictoCanal, LocalDateTime> colConfFecha;
    @FXML private TableColumn<ConflictoCanal, String> colConfSolicitud;
    @FXML private TableColumn<ConflictoCanal, String> colConfMotivo;

    public CanalExternoView(SGA sga) {
        this.sga = sga;
        this.servicio = sga.getCanalExterno();
        try {
            var url = Objects.requireNonNull(
                    getClass().getResource("/co/edu/uniquindio/apartasuitesuq/canal-externo.fxml"),
                    "No se encontró canal-externo.fxml en la ruta especificada"
            );
            FXMLLoader loader = new FXMLLoader(url);
            loader.setController(this);// <-- Asegúrate de que el FXML NO tenga fx:controller="..."
            this.vista = loader.load();
            configurarColumnas();
            configurarEstilosTablas();
            refrescar();
        } catch (Exception e) {
            e.printStackTrace(); // Muestra en consola la línea exacta del error
            throw new RuntimeException("Error al cargar canal-externo.fxml: " + e.getMessage(), e);
        }
    }

    public Node getVista() {
        return vista;
    }

    @FXML
    private void onProcesarSiguiente() {
        if (sga.getSolicitudesExternasPendientes() == 0) {
            Vistas.info("Cola vacía", "No hay solicitudes pendientes.");
            return;
        }
        try {
            SolicitudExterna s = sga.procesarSolicitudCompleta();
            Vistas.info("Solicitud " + s.getIdExterno(), s.getEstado()
                    + (s.getMotivoRechazo() != null ? ": " + s.getMotivoRechazo() : ""));
        } catch (RuntimeException ex) {
            Vistas.error("No se pudo procesar", ex);
        }
        refrescar();
    }

    @FXML
    private void onProcesarTodas() {
        int n = 0;
        try {
            while (sga.getSolicitudesExternasPendientes() > 0) {
                sga.procesarSolicitudCompleta();
                n++;
            }
        } catch (RuntimeException ex) {
            Vistas.error("Se detuvo el procesamiento", ex);
        }
        Vistas.info("Cola procesada", n + " solicitudes procesadas.");
        refrescar();
    }

    private void refrescar() {
        ObservableList<SolicitudExterna> listaPend = Vistas.lista(servicio.getPendientes());
        ObservableList<SolicitudExterna> listaProc = Vistas.lista(servicio.getProcesadas());
        ObservableList<ConflictoCanal> listaConf = Vistas.lista(servicio.getConflictos());

        tablaPendientes.setItems(listaPend);
        tablaProcesadas.setItems(listaProc);
        tablaConflictos.setItems(listaConf);

        lblContadorPendientes.setText(listaPend.size() + (listaPend.size() == 1 ? " pendiente" : " pendientes"));
        lblContadorProcesadas.setText(listaProc.size() + (listaProc.size() == 1 ? " registro reciente" : " registros recientes"));
        lblContadorConflictos.setText("● " + listaConf.size() + (listaConf.size() == 1 ? " detectado" : " detectados"));
    }

    private void configurarColumnas() {
        // Pendientes
        colPendId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdExterno()));
        colPendApto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApartamento().getCodigo()));
        colPendEntrada.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEntrada().toString()));
        colPendSalida.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSalida().toString()));
        colPendPersonas.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getNumPersonas())));
        colPendTitular.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitular().getNombre()));

        // Procesadas
        colProcId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdExterno()));
        colProcApto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApartamento().getCodigo()));
        colProcPersonas.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getNumPersonas())));
        colProcTitular.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitular().getNombre()));
        colProcEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().toString()));
        colProcMotivo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMotivoRechazo() == null ? "" : d.getValue().getMotivoRechazo()));

        // Conflictos
        colConfFecha.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaRegistro()));
        colConfSolicitud.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSolicitud().getIdExterno()));
        colConfMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));

    }

    private void configurarEstilosTablas() {
        Locale enUS = Locale.US;
        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("MMM d, yyyy", enUS);
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("hh:mm a", enUS);

        // Badge ACEPTADA / RECHAZADA
        colProcEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                } else {
                    Label pill = new Label(estado.toUpperCase());
                    pill.getStyleClass().addAll("chip",
                            estado.equalsIgnoreCase("ACEPTADA") ? "chip-preparado" : "chip-fuera");
                    pill.setStyle("-fx-font-size: 10px; -fx-padding: 3 10 3 10;");
                    setGraphic(pill);
                }
            }
        });

        // Motivo con salto de línea o guion "—"
        colProcMotivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String motivo, boolean vacio) {
                super.updateItem(motivo, vacio);
                if (vacio) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    String texto = (motivo == null || motivo.isBlank()) ? "—" : motivo;
                    Label lbl = new Label(texto);
                    lbl.getStyleClass().add("texto-suave");
                    lbl.setStyle("-fx-font-size: 12px;");
                    setGraphic(lbl);
                    setTooltip(new Tooltip(texto));   // texto completo al pasar el mouse
                }
            }
        });

        // Fecha en 2 líneas ("Nov 14, 2026\n10:32 PM")
        colConfFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (vacio || fecha == null) {
                    setGraphic(null);
                } else {
                    Label lblDia = new Label(fecha.format(fmtFecha));
                    lblDia.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-texto;");
                    Label lblHora = new Label(fecha.format(fmtHora));
                    lblHora.getStyleClass().add("texto-suave");
                    lblHora.setStyle("-fx-font-size: 11px;");
                    setGraphic(new VBox(2, lblDia, lblHora));
                }
            }
        });

        // Código de solicitud con borde redondeado ("EXT-101")
        colConfSolicitud.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String codigo, boolean vacio) {
                super.updateItem(codigo, vacio);
                if (vacio || codigo == null) {
                    setGraphic(null);
                } else {
                    Label tag = new Label(codigo);
                    tag.getStyleClass().add("badge-codigo-solicitud");
                    setGraphic(tag);
                }
            }
        });

        // Motivo del conflicto con salto de línea
        colConfMotivo.setCellFactory(col -> new TableCell<>() {
            private final Label lbl = new Label();
            {
                lbl.setWrapText(true);
                lbl.setStyle("-fx-font-size: 12px;");
                lbl.maxWidthProperty().bind(colConfMotivo.widthProperty().subtract(16));
                setGraphic(lbl);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String motivo, boolean vacio) {
                super.updateItem(motivo, vacio);
                if (vacio) {
                    lbl.setText(null);
                    setGraphic(null);
                } else {
                    lbl.setText((motivo == null || motivo.isBlank()) ? "—" : motivo);
                    setGraphic(lbl);
                }
            }
        });
    }

}
