package co.edu.uniquindio.apartasuitesuq.model.ViewPrueba;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstructuraVaciaException;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ColaPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioPreparacion;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;

public class PanelPreparacionView extends VBox {
    private final ServicioPreparacion servicio;
    private final ColaPreparacion cola;
    private final Runnable alCambiar;
    private final ListView<String> listaCola = new ListView<>();
    private final ListView<ColaPreparacion.Item> listaEnCurso = new ListView<>();
    private final Label tiempoMedio = new Label();
    private final TextField codigo = new TextField();
    private final TextField motivo = new TextField();
    private final Iterable<Apartamento> apartamentos;

    public PanelPreparacionView(ServicioPreparacion servicio, ColaPreparacion cola,
                                Iterable<Apartamento> apartamentos, Runnable alCambiar) {
        this.servicio = servicio;
        this.cola = cola;
        this.apartamentos = apartamentos;
        this.alCambiar = alCambiar;
        setSpacing(8);
        setPadding(new javafx.geometry.Insets(10));
        setPrefWidth(300);

        listaEnCurso.setCellFactory(lv -> new javafx.scene.control.ListCell<ColaPreparacion.Item>() {
            @Override
            protected void updateItem(ColaPreparacion.Item it, boolean vacio) {
                super.updateItem(it, vacio);
                setText(vacio || it == null ? null : it.getApartamento().getCodigo());
            }
        });
        listaCola.setPrefHeight(150);
        listaEnCurso.setPrefHeight(90);

        Button iniciar = new Button("Iniciar siguiente");
        iniciar.setOnAction(e -> accion(() -> {
            try {
                listaEnCurso.getItems().add(servicio.iniciarSiguiente());
            } catch (EstructuraVaciaException ex) {
                throw new IllegalStateException("No hay apartamentos en la cola");
            }
        }));
        Button terminar = new Button("Terminar seleccionado");
        terminar.setOnAction(e -> accion(() -> {
            ColaPreparacion.Item it = listaEnCurso.getSelectionModel().getSelectedItem();
            if (it == null) throw new IllegalStateException("Seleccione un apartamento en preparación");
            servicio.terminarPreparacion(it, LocalDateTime.now());
            listaEnCurso.getItems().remove(it);
        }));
        codigo.setPromptText("Código (ej. A101)");
        motivo.setPromptText("Motivo");
        Button fuera = new Button("Fuera de servicio");
        fuera.setOnAction(e -> accion(() -> servicio.ponerFueraDeServicio(buscar(), motivo.getText())));
        Button volver = new Button("Volver a servicio");
        volver.setOnAction(e -> accion(() -> servicio.volverAServicio(buscar(), LocalDateTime.now())));

        getChildren().addAll(new Label("Cola de preparación (prioridad)"), listaCola, new HBox(6, iniciar),
                new Label("En preparación"), listaEnCurso, terminar,
                new Label("Fuera de servicio"), codigo, motivo, new HBox(6, fuera, volver), tiempoMedio);
        refrescar();
    }

    private Apartamento buscar() {
        for (Apartamento a : apartamentos) {
            if (a.getCodigo().equalsIgnoreCase(codigo.getText().trim())) return a;
        }
        throw new IllegalArgumentException("No existe el apartamento " + codigo.getText());
    }

    private void accion(Runnable r) {
        try {
            r.run();
            refrescar();
            alCambiar.run();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
        }
    }

    public void refrescar() {
        listaCola.setItems(FXCollections.observableArrayList());
        for (ColaPreparacion.Item it : cola) {
            String etiqueta = it.getApartamento().getCodigo()
                    + (it.getPrioridad() == 1 ? "  (entra hoy " + it.getHoraClave().toLocalTime() + ")" : "  (sin entrada hoy)");
            listaCola.getItems().add(etiqueta);
        }
        tiempoMedio.setText(String.format("Tiempo medio de preparación: %.0f min", servicio.tiempoMedioPreparacionMin()));
    }
}
