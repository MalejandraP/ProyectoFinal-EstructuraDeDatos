package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioCanalExterno;
import co.edu.uniquindio.apartasuitesuq.model.SolicitudExterna;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Function;

public class CanalExternoView {
    private final SGA sga;
    private final ServicioCanalExterno servicio;
    private final TableView<SolicitudExterna> pendientes = new TableView<>();
    private final TableView<SolicitudExterna> procesadas = new TableView<>();
    private final TableView<ConflictoCanal> conflictos = new TableView<>();
    private final Node vista;

    public CanalExternoView(SGA sga) {
        this.sga = sga;
        this.servicio = sga.getCanalExterno();
        this.vista = construir();
    }

    public Node getVista() { return vista; }

    private Node construir() {
        columnasSolicitud(pendientes, false);
        columnasSolicitud(procesadas, true);
        columnasConflicto();

        Button siguiente = new Button("Procesar siguiente");
        siguiente.getStyleClass().add("boton-primario");
        siguiente.setOnAction(e -> {
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
        });

        Button todas = new Button("Procesar todas");
        todas.getStyleClass().add("boton-secundario");
        todas.setOnAction(e -> {
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
        });

        VBox colaCard = Vistas.tarjeta("Pendientes (orden de llegada)", pendientes,
                new HBox(8, siguiente, todas));
        VBox procesadasCard = Vistas.tarjeta("Procesadas", procesadas);
        VBox conflictosCard = Vistas.tarjeta("Conflictos de canal", conflictos);

        refrescar();
        return Vistas.pagina("Canal externo",
                "Solicitudes de la plataforma de terceros, atendidas una a una en orden de llegada.",
                new VBox(16, colaCard, procesadasCard, conflictosCard));
    }

    private void refrescar() {
        pendientes.setItems(Vistas.lista(servicio.getPendientes()));
        procesadas.setItems(Vistas.lista(servicio.getProcesadas()));
        conflictos.setItems(Vistas.lista(servicio.getConflictos()));
    }

    private void columnasSolicitud(TableView<SolicitudExterna> t, boolean conResultado) {
        col(t, "ID", s -> s.getIdExterno());
        col(t, "Apartamento", s -> s.getApartamento().getCodigo());
        col(t, "Entrada", s -> s.getEntrada().toString());
        col(t, "Salida", s -> s.getSalida().toString());
        col(t, "Personas", s -> String.valueOf(s.getNumPersonas()));
        col(t, "Titular", s -> s.getTitular().getNombre());
        if (conResultado) {
            col(t, "Estado", s -> s.getEstado().toString());
            col(t, "Motivo", s -> s.getMotivoRechazo() == null ? "" : s.getMotivoRechazo());
        }
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(180);
    }

    private void columnasConflicto() {
        TableColumn<ConflictoCanal, String> a = new TableColumn<>("Fecha");
        a.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaRegistro().toString()));
        TableColumn<ConflictoCanal, String> b = new TableColumn<>("Solicitud");
        b.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSolicitud().getIdExterno()));
        TableColumn<ConflictoCanal, String> c = new TableColumn<>("Motivo");
        c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));
        conflictos.getColumns().addAll(a, b, c);
        conflictos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        conflictos.setPrefHeight(150);
    }

    private static void col(TableView<SolicitudExterna> t, String titulo, Function<SolicitudExterna, String> f) {
        TableColumn<SolicitudExterna, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(d -> new SimpleStringProperty(f.apply(d.getValue())));
        t.getColumns().add(c);
    }
}
