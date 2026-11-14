package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Canal;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.viewController.ReservasViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Vista pura del panel de reservas (P2). Delega la lógica y eventos a {@link ReservasViewController}.
 */
public class ReservasView {

    public enum CriterioOrden {
        CODIGO("Orden natural (Código)"),
        PRECIO("Menor tarifa base por noche"),
        CAPACIDAD("Mayor capacidad máxima"),
        DORMITORIOS("Mayor número de dormitorios"),
        RESERVAS("Más reservados (n.º de reservas)");

        final String etiqueta;

        CriterioOrden(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final SGA sga;
    private final Sesion sesion;

    // Controles de búsqueda
    private final DatePicker dpEntrada = new DatePicker();
    private final DatePicker dpSalida = new DatePicker();
    private final Spinner<Integer> spPersonas = new Spinner<>(1, 20, 2);
    private final ComboBox<CriterioOrden> cbOrden = new ComboBox<>();
    private final Button btnBuscar = new Button("Buscar disponibles");
    private final TableView<Apartamento> tablaDisponibles = new TableView<>();

    // Controles de creación de reserva
    private final Label lblSeleccionado = Vistas.texto("Ningún apartamento seleccionado", "texto-suave");
    private final VBox zonaTitular = new VBox(8);
    private final TextField txtDocumento = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtTelefono = new TextField();
    private final TextField txtCorreo = new TextField();
    private final DatePicker dpNacimiento = new DatePicker(LocalDate.of(1995, 1, 1));
    private final ComboBox<Canal> cbCanal = new ComboBox<>();
    private final Button btnAutocompletar = new Button("Buscar titular");
    private final Button btnReservar = new Button("Confirmar creación de reserva");

    // Tabla y acciones de gestión
    private final TableView<Reserva> tablaReservas = new TableView<>();
    private final Button btnAnticipo = new Button("Pagar anticipo (30 %) y confirmar");
    private final Button btnCancelar = new Button("Cancelar reserva (ver devolución)");
    private final Button btnRefrescar = new Button("Actualizar lista");

    public ReservasView(SGA sga, Sesion sesion) {
        this.sga = sga;
        this.sesion = sesion;
    }

    public Node getVista() {
        configurarEstilos();
        configurarTablaDisponibles();
        configurarTablaReservas();

        VBox tarjetaBusqueda = Vistas.tarjeta(
                "1. Buscar disponibilidad",
                construirBarraBusqueda(),
                tablaDisponibles
        );

        VBox tarjetaCreacion = Vistas.tarjeta(
                "2. Crear nueva reserva",
                construirFormularioReserva()
        );

        VBox tarjetaGestion = Vistas.tarjeta(
                "3. Gestión de reservas registradas",
                tablaReservas,
                construirBarraAccionesReserva()
        );

        new ReservasViewController(this, sga, sesion);

        String subtitulo = sesion.getRol() == Rol.HUESPED
                ? "Busque apartamentos disponibles según su criterio preferido, reserve por el portal y gestione sus reservas."
                : "Consulte disponibilidad, registre reservas directas o de portal, confirme anticipos y gestione cancelaciones.";

        return Vistas.pagina("Panel de reservas", subtitulo, tarjetaBusqueda, tarjetaCreacion, tarjetaGestion);
    }

    private void configurarEstilos() {
        spPersonas.setEditable(true);
        spPersonas.setPrefWidth(85);
        btnBuscar.getStyleClass().add("boton-primario");
        btnBuscar.setMinWidth(155);
        btnAutocompletar.getStyleClass().add("boton-contorno");
        btnReservar.getStyleClass().add("boton-primario");
        btnAnticipo.getStyleClass().add("boton-primario");
        btnCancelar.getStyleClass().add("boton-contorno");
        btnRefrescar.getStyleClass().add("boton-secundario");
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaDisponibles() {
        TableColumn<Apartamento, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colCodigo.setPrefWidth(95);

        TableColumn<Apartamento, String> colNombre = new TableColumn<>("Apartamento");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(190);

        TableColumn<Apartamento, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(d -> new SimpleStringProperty(
                "Bloque " + d.getValue().getBloque() + " · Piso " + d.getValue().getPiso()));
        colUbicacion.setPrefWidth(145);

        TableColumn<Apartamento, String> colCapacidad = new TableColumn<>("Capacidad");
        colCapacidad.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCapacidadMaxima() + " pers. (" + d.getValue().getNumeroDormitorios() + " dorm.)"));
        colCapacidad.setPrefWidth(145);

        TableColumn<Apartamento, String> colTarifa = new TableColumn<>("Tarifa base / noche");
        colTarifa.setCellValueFactory(d -> new SimpleStringProperty(
                Vistas.moneda(d.getValue().getTarifaBaseNoche())));
        colTarifa.setPrefWidth(145);

        TableColumn<Apartamento, String> colReservas = new TableColumn<>("Reservas hist.");
        colReservas.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getNumeroReservas())));
        colReservas.setPrefWidth(110);

        tablaDisponibles.getColumns().setAll(colCodigo, colNombre, colUbicacion, colCapacidad, colTarifa, colReservas);
        tablaDisponibles.setPrefHeight(210);
        tablaDisponibles.setPlaceholder(new Label("No hay apartamentos disponibles para los filtros indicados."));
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaReservas() {
        TableColumn<Reserva, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoReserva()));
        colCodigo.setPrefWidth(105);

        TableColumn<Reserva, String> colApto = new TableColumn<>("Apartamento");
        colApto.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getApartamento().getCodigo() + " - " + d.getValue().getApartamento().getNombre()));
        colApto.setPrefWidth(180);

        TableColumn<Reserva, String> colTitular = new TableColumn<>("Titular");
        colTitular.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTitular().getNombre() + " (" + d.getValue().getTitular().getDocumento() + ")"));
        colTitular.setPrefWidth(190);

        TableColumn<Reserva, String> colEstancia = new TableColumn<>("Estancia");
        colEstancia.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaInicio() + " → " + d.getValue().getEstancia().getSalida()));
        colEstancia.setPrefWidth(175);

        TableColumn<Reserva, String> colCanal = new TableColumn<>("Canal");
        colCanal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCanal().name()));
        colCanal.setPrefWidth(95);

        TableColumn<Reserva, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
        colEstado.setPrefWidth(115);

        TableColumn<Reserva, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                Vistas.moneda(d.getValue().getCostoTotal())));
        colTotal.setPrefWidth(110);

        tablaReservas.getColumns().setAll(colCodigo, colApto, colTitular, colEstancia, colCanal, colEstado, colTotal);
        tablaReservas.setPrefHeight(230);
        tablaReservas.setPlaceholder(new Label("No hay reservas para mostrar."));
    }

    private Node construirBarraBusqueda() {
        HBox barra = new HBox(12,
                etiquetarCampo("Entrada:", dpEntrada),
                etiquetarCampo("Salida:", dpSalida),
                etiquetarCampo("Personas:", spPersonas),
                etiquetarCampo("Ordenar por:", cbOrden),
                btnBuscar
        );
        barra.setAlignment(Pos.BOTTOM_LEFT);
        return barra;
    }

    private Node construirFormularioReserva() {
        HBox pie = new HBox(btnReservar);
        pie.setAlignment(Pos.CENTER_RIGHT);
        return new VBox(12, lblSeleccionado, zonaTitular, pie);
    }

    private Node construirBarraAccionesReserva() {
        HBox acciones = new HBox(12, btnAnticipo, btnCancelar, btnRefrescar);
        acciones.setAlignment(Pos.CENTER_LEFT);
        return acciones;
    }

    private VBox etiquetarCampo(String etiqueta, Node control) {
        VBox caja = new VBox(4, Vistas.texto(etiqueta, "texto-suave"), control);
        HBox.setHgrow(caja, Priority.NEVER);
        return caja;
    }

    public void mostrarModoHuesped(String resumenTitular) {
        zonaTitular.getChildren().setAll(Vistas.texto(resumenTitular, "texto-suave"));
    }

    public void mostrarModoEmpleado() {
        txtDocumento.setPromptText("Documento");
        txtNombre.setPromptText("Nombre completo");
        txtTelefono.setPromptText("Teléfono");
        txtCorreo.setPromptText("correo@ejemplo.com");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(4, 0, 4, 0));

        grid.add(new Label("Documento titular:"), 0, 0);
        grid.add(new HBox(8, txtDocumento, btnAutocompletar), 1, 0);
        grid.add(new Label("Nombre completo:"), 2, 0);
        grid.add(txtNombre, 3, 0);

        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(txtTelefono, 1, 1);
        grid.add(new Label("Correo:"), 2, 1);
        grid.add(txtCorreo, 3, 1);

        grid.add(new Label("Fecha nacimiento:"), 0, 2);
        grid.add(dpNacimiento, 1, 2);
        grid.add(new Label("Canal de reserva:"), 2, 2);
        grid.add(cbCanal, 3, 2);

        zonaTitular.getChildren().setAll(grid);
    }

    // ================================================================ getters para el controlador

    public DatePicker getDpEntrada() { return dpEntrada; }
    public DatePicker getDpSalida() { return dpSalida; }
    public Spinner<Integer> getSpPersonas() { return spPersonas; }
    public ComboBox<CriterioOrden> getCbOrden() { return cbOrden; }
    public Button getBtnBuscar() { return btnBuscar; }
    public TableView<Apartamento> getTablaDisponibles() { return tablaDisponibles; }
    public Label getLblSeleccionado() { return lblSeleccionado; }
    public TextField getTxtDocumento() { return txtDocumento; }
    public TextField getTxtNombre() { return txtNombre; }
    public TextField getTxtTelefono() { return txtTelefono; }
    public TextField getTxtCorreo() { return txtCorreo; }
    public DatePicker getDpNacimiento() { return dpNacimiento; }
    public ComboBox<Canal> getCbCanal() { return cbCanal; }
    public Button getBtnAutocompletar() { return btnAutocompletar; }
    public Button getBtnReservar() { return btnReservar; }
    public TableView<Reserva> getTablaReservas() { return tablaReservas; }
    public Button getBtnAnticipo() { return btnAnticipo; }
    public Button getBtnCancelar() { return btnCancelar; }
    public Button getBtnRefrescar() { return btnRefrescar; }
}