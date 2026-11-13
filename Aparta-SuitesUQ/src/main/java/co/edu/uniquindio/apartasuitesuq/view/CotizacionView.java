package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Cotizacion;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.NocheLiquidada;
import co.edu.uniquindio.apartasuitesuq.model.Ocupante;
import co.edu.uniquindio.apartasuitesuq.model.OrdenApartamentos;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Temporada;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Function;

/**
 * Cotización de una estancia con su desglose noche por noche (la estancia se recorre con su
 * Iterator y se liquida recursivamente). Los ocupantes con fecha de nacimiento permiten aplicar
 * la regla de los menores de 6 años; el resto del grupo se cobra como adulto.
 */
public class CotizacionView {

    private final SGA sga;
    private final Sesion sesion;
    private final ObservableList<Ocupante> ocupantes = FXCollections.observableArrayList();
    private final TableView<NocheLiquidada> desglose = new TableView<>();
    private final FlowPane totales = new FlowPane(12, 12);
    private final Node vista;

    public CotizacionView(SGA sga, Sesion sesion) {
        this.sga = sga;
        this.sesion = sesion;
        this.vista = construir();
    }

    public Node getVista() {
        return vista;
    }

    private Node construir() {
        if (sga.getApartamentos().estaVacio()) {
            return Vistas.pagina("Cotizar estancia", null, Vistas.aviso("No hay apartamentos registrados."));
        }
        ComboBox<Apartamento> apartamento = new ComboBox<>(Vistas.lista(sga.getApartamentos()));
        apartamento.getSelectionModel().selectFirst();
        apartamento.setMaxWidth(Double.MAX_VALUE);
        DatePicker entrada = new DatePicker(sga.hoy().plusDays(1));
        DatePicker salida = new DatePicker(sga.hoy().plusDays(3));
        Spinner<Integer> personas = new Spinner<>(1, 20, 2);
        personas.setEditable(true);

        // Ocupantes
        Huesped titular = sesion.getHuesped();
        if (titular != null && titular.getFechaNacimiento() != null && ocupantes.isEmpty()) {
            ocupantes.add(titular.comoOcupante());
        }
        ListView<Ocupante> lista = new ListView<>(ocupantes);
        lista.setPrefHeight(120);
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre del ocupante");
        DatePicker nacimiento = new DatePicker();
        nacimiento.setPromptText("Fecha de nacimiento");
        Button agregar = new Button("Agregar");
        agregar.getStyleClass().add("boton-secundario");
        agregar.setOnAction(e -> {
            try {
                ocupantes.add(new Ocupante(nombre.getText(), nacimiento.getValue()));
                nombre.clear();
                nacimiento.setValue(null);
                if (ocupantes.size() > personas.getValue()) {
                    personas.getValueFactory().setValue(ocupantes.size());
                }
            } catch (RuntimeException ex) {
                Vistas.error("Ocupante inválido", ex);
            }
        });
        Button quitar = new Button("Quitar seleccionado");
        quitar.getStyleClass().add("boton-contorno");
        quitar.setOnAction(e -> ocupantes.remove(lista.getSelectionModel().getSelectedItem()));

        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("Apartamento"), apartamento);
        formulario.addRow(1, new Label("Entrada"), entrada);
        formulario.addRow(2, new Label("Salida"), salida);
        formulario.addRow(3, new Label("Personas"), personas);

        Button cotizar = new Button("Cotizar");
        cotizar.getStyleClass().add("boton-primario");
        cotizar.setOnAction(e -> cotizar(apartamento.getValue(), entrada.getValue(), salida.getValue(),
                personas.getValue()));

        // Disponibilidad con el criterio de orden que elija el usuario (Comparator)
        ComboBox<String> criterio = new ComboBox<>();
        criterio.getItems().addAll("Código", "Precio", "Capacidad", "Dormitorios", "Más reservados");
        criterio.getSelectionModel().select(1);
        ListView<Apartamento> libres = new ListView<>();
        libres.setPrefHeight(130);
        libres.setPlaceholder(new Label("Busque disponibilidad para las fechas elegidas"));
        libres.getSelectionModel().selectedItemProperty().addListener((obs, antes, elegido) -> {
            if (elegido != null) {
                apartamento.setValue(elegido);
            }
        });
        Button buscar = new Button("Buscar disponibles");
        buscar.getStyleClass().add("boton-secundario");
        buscar.setOnAction(e -> {
            try {
                libres.setItems(Vistas.lista(sga.buscarDisponibles(entrada.getValue(), salida.getValue(),
                        personas.getValue(), orden(criterio.getValue()))));
            } catch (RuntimeException ex) {
                Vistas.error("No se pudo buscar", ex);
            }
        });

        VBox datos = Vistas.tarjeta("Datos de la estancia", formulario,
                new HBox(8, new Label("Ordenar por"), criterio, buscar), libres,
                Vistas.texto("Ocupantes con fecha de nacimiento (los menores de 6 años no pagan). "
                        + "Las personas sin registrar se cobran como adultas.", "texto-suave"),
                lista, new HBox(8, nombre, nacimiento, agregar), quitar, cotizar);
        datos.setMaxWidth(520);

        configurarDesglose();
        VBox resultado = Vistas.tarjeta("Desglose noche por noche", totales, desglose);
        HBox columnas = new HBox(16, datos, resultado);
        HBox.setHgrow(resultado, javafx.scene.layout.Priority.ALWAYS);
        return Vistas.pagina("Cotizar estancia", "Cada noche se cobra con la tarifa de su temporada "
                + "por cada persona que paga. La noche de salida no se cobra.", columnas);
    }

    private Comparator<Apartamento> orden(String criterio) {
        switch (criterio) {
            case "Precio":
                return OrdenApartamentos.porPrecio();
            case "Capacidad":
                return OrdenApartamentos.porCapacidad();
            case "Dormitorios":
                return OrdenApartamentos.porDormitorios();
            case "Más reservados":
                return OrdenApartamentos.porNumeroDeReservas(sga);
            default:
                return null; // orden natural: código
        }
    }

    private void configurarDesglose() {
        agregarColumna("Noche", n -> n.getNoche().toString());
        agregarColumna("Temporada", n -> n.getTemporada().getDescripcion());
        agregarColumna("Tarifa por persona", n -> Vistas.moneda(n.getTarifaPorPersona()));
        agregarColumna("Pagan", n -> String.valueOf(n.getPersonasQuePagan()));
        agregarColumna("Sin cobro", n -> String.valueOf(n.getPersonasSinCobro()));
        agregarColumna("Subtotal", n -> Vistas.moneda(n.getSubtotal()));
        desglose.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        desglose.setPrefHeight(360);
        desglose.setPlaceholder(new Label("Elija los datos y presione «Cotizar»"));
    }

    private void agregarColumna(String titulo, Function<NocheLiquidada, String> valor) {
        TableColumn<NocheLiquidada, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        desglose.getColumns().add(columna);
    }

    private void cotizar(Apartamento apartamento, LocalDate entrada, LocalDate salida, int personas) {
        try {
            if (apartamento == null) {
                throw new IllegalArgumentException("Elija un apartamento.");
            }
            ListaDoble<Ocupante> registrados = new ListaDoble<>();
            for (Ocupante ocupante : ocupantes) {
                registrados.agregarFinal(ocupante);
            }
            Cotizacion cotizacion = sga.cotizar(apartamento.getCodigo(), entrada, salida, registrados, personas);
            desglose.setItems(Vistas.lista(cotizacion.getDesglose()));

            totales.getChildren().setAll(
                    dato("Total", Vistas.moneda(cotizacion.getTotal())),
                    dato("Anticipo 30 %", Vistas.moneda(cotizacion.getAnticipo())),
                    dato("Noches", String.valueOf(cotizacion.getNumeroNoches())),
                    dato("Baja / Media / Alta", cotizacion.contarNochesEn(Temporada.BAJA) + " / "
                            + cotizacion.contarNochesEn(Temporada.MEDIA) + " / "
                            + cotizacion.contarNochesEn(Temporada.ALTA)),
                    dato("Disponible", sga.estaDisponible(apartamento, entrada, salida) ? "Sí" : "No"));
        } catch (RuntimeException ex) {
            Vistas.error("No se pudo cotizar", ex);
        }
    }

    private static VBox dato(String titulo, String valor) {
        VBox caja = new VBox(2, Vistas.texto(titulo, "texto-suave"), Vistas.texto(valor, "titulo-seccion"));
        caja.getStyleClass().add("caja-info");
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.setMinWidth(130);
        return caja;
    }
}
