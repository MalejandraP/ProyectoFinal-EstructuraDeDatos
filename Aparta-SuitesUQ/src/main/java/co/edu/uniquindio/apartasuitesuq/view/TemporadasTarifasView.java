package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.PeriodoTemporada;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Temporada;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.temporal.ChronoUnit;
import java.util.function.Function;

/**
 * Pantalla del administrador para definir las fechas de cada temporada (las fechas sin periodo
 * son temporada Baja) y las tarifas por persona por noche de cada apartamento.
 */
public class TemporadasTarifasView {

    private final SGA sga;
    private final TableView<PeriodoTemporada> periodos = new TableView<>();
    private final TableView<Apartamento> tarifas = new TableView<>();
    private final Node vista;

    public TemporadasTarifasView(SGA sga) {
        this.sga = sga;
        this.vista = construir();
    }

    public Node getVista() {
        return vista;
    }

    private Node construir() {
        HBox columnas = new HBox(16, construirTemporadas(), construirTarifas());
        columnas.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return Vistas.pagina("Temporadas y tarifas", "Una estancia que cruza temporadas se liquida noche "
                + "por noche con la tarifa de cada una.", columnas);
    }

    // ================================================================ temporadas

    private VBox construirTemporadas() {
        columna(periodos, "Temporada", p -> p.getTemporada().getDescripcion());
        columna(periodos, "Desde", p -> p.getInicio().toString());
        columna(periodos, "Hasta", p -> p.getFin().toString());
        columna(periodos, "Días", p -> String.valueOf(ChronoUnit.DAYS.between(p.getInicio(), p.getFin()) + 1));
        periodos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        periodos.setPrefHeight(300);
        periodos.setPlaceholder(new Label("Sin periodos: todo el año es temporada Baja"));
        refrescarPeriodos();

        ComboBox<Temporada> temporada = new ComboBox<>();
        temporada.getItems().addAll(Temporada.MEDIA, Temporada.ALTA, Temporada.BAJA);
        temporada.getSelectionModel().selectFirst();
        DatePicker inicio = new DatePicker(sga.hoy());
        DatePicker fin = new DatePicker(sga.hoy().plusDays(7));

        Button agregar = new Button("Agregar periodo");
        agregar.getStyleClass().add("boton-primario");
        agregar.setOnAction(e -> {
            try {
                sga.definirTemporada(temporada.getValue(), inicio.getValue(), fin.getValue());
                refrescarPeriodos();
            } catch (RuntimeException ex) {
                Vistas.error("Periodo inválido", ex);
            }
        });
        Button eliminar = new Button("Eliminar seleccionado");
        eliminar.getStyleClass().add("boton-contorno");
        eliminar.setOnAction(e -> {
            PeriodoTemporada elegido = periodos.getSelectionModel().getSelectedItem();
            if (elegido != null) {
                sga.eliminarPeriodo(elegido);
                refrescarPeriodos();
            }
        });

        DatePicker consulta = new DatePicker(sga.hoy());
        Label resultado = Vistas.texto("", "texto-fuerte");
        Runnable consultar = () -> resultado.setText(consulta.getValue() == null ? ""
                : "Temporada " + sga.getCalendario().temporadaDe(consulta.getValue()).getDescripcion());
        consulta.setOnAction(e -> consultar.run());
        consultar.run();

        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("Temporada"), temporada);
        formulario.addRow(1, new Label("Desde"), inicio);
        formulario.addRow(2, new Label("Hasta (incluido)"), fin);

        return Vistas.tarjeta("Periodos de temporada", periodos, formulario, new HBox(8, agregar, eliminar),
                Vistas.texto("¿Qué temporada es una fecha?", "texto-suave"), new HBox(8, consulta, resultado));
    }

    private void refrescarPeriodos() {
        periodos.setItems(Vistas.lista(sga.getCalendario().getPeriodos()));
    }

    // ================================================================== tarifas

    private VBox construirTarifas() {
        columna(tarifas, "Código", Apartamento::getCodigo);
        columna(tarifas, "Nombre", Apartamento::getNombre);
        columna(tarifas, "Cap.", a -> String.valueOf(a.getCapacidadMaxima()));
        for (Temporada temporada : Temporada.values()) {
            columna(tarifas, temporada.getDescripcion(), a -> Vistas.moneda(a.getTarifa(temporada)));
        }
        tarifas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tarifas.setPrefHeight(300);
        tarifas.setItems(Vistas.lista(sga.getApartamentos()));

        TextField[] campos = new TextField[Temporada.values().length];
        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(10);
        for (Temporada temporada : Temporada.values()) {
            TextField campo = new TextField();
            campo.setPromptText("Tarifa por persona por noche");
            campos[temporada.ordinal()] = campo;
            formulario.addRow(temporada.ordinal(), new Label(temporada.getDescripcion()), campo);
        }
        Label seleccionado = Vistas.texto("Seleccione un apartamento de la tabla", "texto-suave");
        tarifas.getSelectionModel().selectedItemProperty().addListener((obs, antes, apartamento) -> {
            if (apartamento != null) {
                seleccionado.setText(apartamento.toString());
                for (Temporada temporada : Temporada.values()) {
                    campos[temporada.ordinal()].setText(String.format("%.0f", apartamento.getTarifa(temporada)));
                }
            }
        });

        Button guardar = new Button("Guardar tarifas");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> {
            Apartamento apartamento = tarifas.getSelectionModel().getSelectedItem();
            if (apartamento == null) {
                Vistas.info("Tarifas", "Seleccione primero un apartamento.");
                return;
            }
            try {
                double[] valores = new double[campos.length];
                for (int i = 0; i < campos.length; i++) {
                    valores[i] = Double.parseDouble(campos[i].getText().trim().replace(".", "").replace(",", ""));
                }
                for (Temporada temporada : Temporada.values()) {
                    sga.definirTarifa(apartamento.getCodigo(), temporada, valores[temporada.ordinal()]);
                }
                tarifas.refresh();
            } catch (NumberFormatException ex) {
                Vistas.error("Tarifa inválida", new IllegalArgumentException("Escriba las tarifas como números enteros."));
            } catch (RuntimeException ex) {
                Vistas.error("Tarifa inválida", ex);
            }
        });

        return Vistas.tarjeta("Tarifas por apartamento", tarifas, seleccionado, formulario, guardar);
    }

    private static <T> void columna(TableView<T> tabla, String titulo, Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        tabla.getColumns().add(columna);
    }
}
