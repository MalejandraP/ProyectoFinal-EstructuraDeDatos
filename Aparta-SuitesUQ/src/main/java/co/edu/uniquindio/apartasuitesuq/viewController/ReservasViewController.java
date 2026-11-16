package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.controller.Sesion;
import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Canal;
import co.edu.uniquindio.apartasuitesuq.model.Cotizacion;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.NocheLiquidada;
import co.edu.uniquindio.apartasuitesuq.model.Ocupante;
import co.edu.uniquindio.apartasuitesuq.model.OrdenApartamentos;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Temporada;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Controlador de reservas.fxml (B. Panel de reservas). El huésped reserva por el portal para sí
 * mismo; recepción y administración reservan para cualquier titular y manejan el ciclo de vida.
 * Las reglas (solapamiento, capacidad, fechas, transiciones) las valida la fachada: aquí solo se
 * capturan sus excepciones y se muestran.
 */
public class ReservasViewController implements ControladorPantalla {

    private static final String[] CRITERIOS = {"Precio", "Capacidad", "Dormitorios", "Más reservados", "Código"};

    @FXML private Button btnVencimientos;
    @FXML private TabPane pestanas;
    @FXML private Tab tabRegistradas;

    // disponibilidad
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private ComboBox<String> cbOrden;
    @FXML private TableView<Apartamento> tablaDisponibles;
    @FXML private TableColumn<Apartamento, String> colDispCodigo;
    @FXML private TableColumn<Apartamento, String> colDispNombre;
    @FXML private TableColumn<Apartamento, String> colDispUbicacion;
    @FXML private TableColumn<Apartamento, String> colDispDormitorios;
    @FXML private TableColumn<Apartamento, String> colDispCapacidad;
    @FXML private TableColumn<Apartamento, String> colDispTarifa;
    @FXML private TableColumn<Apartamento, String> colDispReservas;
    @FXML private Label lblSeleccionado;

    // titular y ocupantes
    @FXML private Label lblTitularHuesped;
    @FXML private GridPane formTitular;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private DatePicker dpNacimiento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<Canal> cbCanal;
    @FXML private ListView<Ocupante> listaOcupantes;
    @FXML private TextField txtNombreOcupante;
    @FXML private DatePicker dpNacimientoOcupante;

    // cotización
    @FXML private Label lblTotal;
    @FXML private Label lblAnticipo;
    @FXML private Label lblResumenCotizacion;
    @FXML private TableView<NocheLiquidada> tablaDesglose;
    @FXML private TableColumn<NocheLiquidada, String> colNoche;
    @FXML private TableColumn<NocheLiquidada, String> colTemporada;
    @FXML private TableColumn<NocheLiquidada, String> colTarifa;
    @FXML private TableColumn<NocheLiquidada, String> colPagan;
    @FXML private TableColumn<NocheLiquidada, String> colSinCobro;
    @FXML private TableColumn<NocheLiquidada, String> colSubtotal;

    // grupos grandes
    @FXML private DatePicker dpGrupoEntrada;
    @FXML private DatePicker dpGrupoSalida;
    @FXML private Spinner<Integer> spGrupoPersonas;
    @FXML private TextField txtGrupoDocumento;
    @FXML private ComboBox<Canal> cbGrupoCanal;
    @FXML private TableView<Apartamento> tablaGrupo;
    @FXML private TableColumn<Apartamento, String> colGrupoCodigo;
    @FXML private TableColumn<Apartamento, String> colGrupoNombre;
    @FXML private TableColumn<Apartamento, String> colGrupoCapacidad;
    @FXML private TableColumn<Apartamento, String> colGrupoTarifa;
    @FXML private Label lblGrupoResumen;
    @FXML private Button btnReservarGrupo;

    // reservas registradas
    @FXML private TextField txtBuscarCodigo;
    @FXML private ToggleGroup grupoOrden;
    @FXML private ToggleButton tgPorFecha;
    @FXML private ToggleButton tgPorCodigo;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colResCodigo;
    @FXML private TableColumn<Reserva, String> colResTitular;
    @FXML private TableColumn<Reserva, String> colResApto;
    @FXML private TableColumn<Reserva, String> colResEntrada;
    @FXML private TableColumn<Reserva, String> colResSalida;
    @FXML private TableColumn<Reserva, String> colResNoches;
    @FXML private TableColumn<Reserva, String> colResPersonas;
    @FXML private TableColumn<Reserva, String> colResCanal;
    @FXML private TableColumn<Reserva, EstadoReserva> colResEstado;
    @FXML private TableColumn<Reserva, String> colResTotal;
    @FXML private TableColumn<Reserva, String> colResPagado;
    @FXML private TableColumn<Reserva, String> colResSaldo;
    @FXML private Label lblDetalleReserva;
    @FXML private Button btnConfirmar;
    @FXML private Button btnAnticipo;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;
    @FXML private Button btnNoShow;
    @FXML private Button btnCancelar;
    @FXML private Button btnFolio;

    private final ObservableList<Ocupante> ocupantes = FXCollections.observableArrayList();
    private ListaDoble<Apartamento> combinacionGrupo = new ListaDoble<>();
    private PrincipalViewController principal;
    private SGA sga;
    private Sesion sesion;

    @FXML
    private void initialize() {
        spPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 2));
        spGrupoPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 80, 10));
        cbOrden.getItems().setAll(CRITERIOS);
        cbOrden.setValue("Precio");
        cbOrden.setOnAction(e -> onBuscarDisponibles());

        configurarColumnas();
        listaOcupantes.setItems(ocupantes);
        listaOcupantes.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(Ocupante ocupante, boolean vacio) {
                super.updateItem(ocupante, vacio);
                if (vacio || ocupante == null) {
                    setText(null);
                    return;
                }
                LocalDate entrada = dpEntrada.getValue() == null ? sga.hoy() : dpEntrada.getValue();
                int edad = ocupante.edadEn(entrada);
                setText(ocupante.getNombre() + " · " + VistaUtil.fecha(ocupante.getFechaNacimiento()) + " · "
                        + edad + " años al llegar" + (ocupante.pagaEn(entrada) ? "" : " (no paga)"));
            }
        });
        tablaDisponibles.getSelectionModel().selectedItemProperty().addListener((obs, antes, apartamento) -> {
            lblSeleccionado.setText(apartamento == null ? "Ningún apartamento seleccionado"
                    : "Seleccionado: " + apartamento + " · hasta " + apartamento.getCapacidadMaxima() + " personas");
            limpiarCotizacion();
        });
        tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, antes, reserva) -> mostrarDetalle(reserva));
        grupoOrden.selectedToggleProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) {
                antes.setSelected(true);
            } else if (sga != null) {
                recargarReservas();
            }
        });
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.principal = principal;
        this.sga = principal.getSga();
        this.sesion = principal.getSesion();
        boolean esHuesped = sesion.getRol() == Rol.HUESPED;

        dpEntrada.setValue(sga.hoy().plusDays(1));
        dpSalida.setValue(sga.hoy().plusDays(3));
        dpGrupoEntrada.setValue(sga.hoy().plusDays(7));
        dpGrupoSalida.setValue(sga.hoy().plusDays(9));
        dpEntrada.valueProperty().addListener((obs, antes, ahora) -> listaOcupantes.refresh());

        if (esHuesped) {
            Huesped huesped = sesion.getHuesped();
            cbCanal.getItems().setAll(Canal.PORTAL);
            cbGrupoCanal.getItems().setAll(Canal.PORTAL);
            lblTitularHuesped.setText("Titular: " + huesped.getNombre() + " · documento " + huesped.getDocumento()
                    + ". Reservas por el Portal: quedan Pendientes hasta pagar el anticipo del 30 % y vencen a las 24 h.");
            txtGrupoDocumento.setText(huesped.getDocumento());
            txtGrupoDocumento.setDisable(true);
        } else {
            cbCanal.getItems().setAll(Canal.DIRECTA, Canal.PORTAL);
            cbGrupoCanal.getItems().setAll(Canal.DIRECTA, Canal.PORTAL);
        }
        cbCanal.getSelectionModel().selectFirst();
        cbGrupoCanal.getSelectionModel().selectFirst();
        mostrar(lblTitularHuesped, esHuesped);
        mostrar(formTitular, !esHuesped);
        mostrar(btnVencimientos, !esHuesped);
        for (Node boton : new Node[]{btnConfirmar, btnCheckIn, btnCheckOut, btnNoShow, btnFolio}) {
            mostrar(boton, !esHuesped);
        }
        onBuscarDisponibles();
        recargarReservas();
    }

    private static void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    private void configurarColumnas() {
        colDispCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigo()));
        colDispNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colDispUbicacion.setCellValueFactory(d -> texto("Bloque " + d.getValue().getBloque() + " · piso " + d.getValue().getPiso()));
        colDispDormitorios.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumeroDormitorios())));
        colDispCapacidad.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getCapacidadMaxima())));
        colDispTarifa.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getTarifaBaseNoche())));
        colDispReservas.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumeroReservas())));

        colNoche.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getNoche())));
        colTemporada.setCellValueFactory(d -> texto(d.getValue().getTemporada().getDescripcion()));
        colTarifa.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getTarifaPorPersona())));
        colPagan.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getPersonasQuePagan())));
        colSinCobro.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getPersonasSinCobro())));
        colSubtotal.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getSubtotal())));

        colGrupoCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigo()));
        colGrupoNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colGrupoCapacidad.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getCapacidadMaxima())));
        colGrupoTarifa.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getTarifaBaseNoche())));

        colResCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigoReserva()));
        colResTitular.setCellValueFactory(d -> texto(d.getValue().getHuesped().getNombre()));
        colResApto.setCellValueFactory(d -> texto(d.getValue().getApartamento().getCodigo()));
        colResEntrada.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getFechaInicio())));
        colResSalida.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getFechaFin())));
        colResNoches.setCellValueFactory(d -> texto(String.valueOf(d.getValue().obtenerNumeroNoches())));
        colResPersonas.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumeroPersonas())));
        colResCanal.setCellValueFactory(d -> texto(d.getValue().getCanal().getDescripcion()));
        colResEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getEstado()));
        colResEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoReserva estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                } else {
                    Label chip = new Label(estado.getDescripcion());
                    chip.getStyleClass().setAll("chip", VistaUtil.claseEstado(estado));
                    setGraphic(chip);
                }
            }
        });
        colResTotal.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getCostoTotal())));
        colResPagado.setCellValueFactory(d -> texto(VistaUtil.dinero(d.getValue().getFolio().totalPago())));
        colResSaldo.setCellValueFactory(d -> texto(VistaUtil.dinero(sga.saldoDe(d.getValue()))));
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    // ======================================================== disponibilidad

    @FXML
    private void onBuscarDisponibles() {
        if (sga == null) {
            return;
        }
        try {
            tablaDisponibles.setItems(VistaUtil.lista(sga.buscarDisponibles(dpEntrada.getValue(), dpSalida.getValue(),
                    spPersonas.getValue(), orden(cbOrden.getValue()))));
            tablaDisponibles.getSelectionModel().selectFirst();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo consultar la disponibilidad", ex);
        }
    }

    /** Criterio de orden elegido (Comparator de OrdenApartamentos); null = orden natural por código. */
    private Comparator<Apartamento> orden(String criterio) {
        switch (criterio == null ? "" : criterio) {
            case "Precio":
                return OrdenApartamentos.porPrecio();
            case "Capacidad":
                return OrdenApartamentos.porCapacidad();
            case "Dormitorios":
                return OrdenApartamentos.porDormitorios();
            case "Más reservados":
                return OrdenApartamentos.porNumeroDeReservas();
            default:
                return null;
        }
    }

    // =============================================== titular y ocupantes

    @FXML
    private void onBuscarTitular() {
        Huesped huesped = sga.buscarHuesped(txtDocumento.getText());
        if (huesped == null) {
            VistaUtil.info("Huésped nuevo", "No hay un huésped con ese documento. Completa sus datos y se registrará al reservar.");
            return;
        }
        txtNombre.setText(huesped.getNombre());
        dpNacimiento.setValue(huesped.getFechaNacimiento());
        txtTelefono.setText(huesped.getTelefono());
        txtCorreo.setText(huesped.getCorreo());
    }

    @FXML
    private void onAgregarOcupante() {
        try {
            if (ocupantes.size() >= spPersonas.getValue() - 1) {
                spPersonas.getValueFactory().setValue(ocupantes.size() + 2); // titular + acompañantes
            }
            ocupantes.add(new Ocupante(txtNombreOcupante.getText(), dpNacimientoOcupante.getValue()));
            txtNombreOcupante.clear();
            dpNacimientoOcupante.setValue(null);
            limpiarCotizacion();
        } catch (RuntimeException ex) {
            VistaUtil.error("Ocupante inválido", ex);
        }
    }

    @FXML
    private void onQuitarOcupante() {
        ocupantes.remove(listaOcupantes.getSelectionModel().getSelectedItem());
        limpiarCotizacion();
    }

    /** El titular de la sesión (huésped) o el del formulario; si no existe se crea con sus datos. */
    private Huesped titular() {
        if (sesion.getRol() == Rol.HUESPED) {
            return sesion.getHuesped();
        }
        Huesped existente = sga.buscarHuesped(txtDocumento.getText());
        if (existente != null) {
            return existente;
        }
        return new Huesped(texto(txtDocumento), texto(txtNombre), dpNacimiento.getValue(),
                texto(txtTelefono), texto(txtCorreo), null);
    }

    private static String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private ListaDoble<Ocupante> acompanantes() {
        ListaDoble<Ocupante> lista = new ListaDoble<>();
        for (Ocupante ocupante : ocupantes) {
            lista.agregarFinal(ocupante);
        }
        return lista;
    }

    // =========================================================== cotización

    @FXML
    private void onCotizar() {
        Apartamento apartamento = tablaDisponibles.getSelectionModel().getSelectedItem();
        if (apartamento == null) {
            VistaUtil.info("Elige un apartamento", "Selecciona un apartamento disponible para cotizar.");
            return;
        }
        try {
            ListaDoble<Ocupante> conTitular = new ListaDoble<>();
            Huesped titular = titular();
            if (titular.getFechaNacimiento() != null) {
                conTitular.agregarFinal(titular.comoOcupante());
            }
            for (Ocupante ocupante : ocupantes) {
                conTitular.agregarFinal(ocupante);
            }
            Cotizacion cotizacion = sga.cotizar(apartamento.getCodigo(), dpEntrada.getValue(), dpSalida.getValue(),
                    conTitular, spPersonas.getValue());
            tablaDesglose.setItems(VistaUtil.lista(cotizacion.getDesglose()));
            lblTotal.setText(VistaUtil.dinero(cotizacion.getTotal()));
            lblAnticipo.setText(VistaUtil.dinero(cotizacion.getAnticipo()));
            lblResumenCotizacion.setText(cotizacion.getNumeroNoches() + " noches · Baja "
                    + cotizacion.contarNochesEn(Temporada.BAJA) + " / Media " + cotizacion.contarNochesEn(Temporada.MEDIA)
                    + " / Alta " + cotizacion.contarNochesEn(Temporada.ALTA) + " · " + spPersonas.getValue()
                    + " personas · saldo tras el anticipo " + VistaUtil.dinero(cotizacion.getSaldoTrasAnticipo()));
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo cotizar", ex);
        }
    }

    private void limpiarCotizacion() {
        tablaDesglose.getItems().clear();
        lblTotal.setText("—");
        lblAnticipo.setText("—");
        lblResumenCotizacion.setText("");
    }

    @FXML
    private void onCrearReserva() {
        Apartamento apartamento = tablaDisponibles.getSelectionModel().getSelectedItem();
        if (apartamento == null) {
            VistaUtil.info("Elige un apartamento", "Selecciona primero un apartamento disponible.");
            return;
        }
        try {
            Reserva creada = sga.crearReserva(titular(), apartamento.getCodigo(), dpEntrada.getValue(),
                    dpSalida.getValue(), acompanantes(), spPersonas.getValue(), cbCanal.getValue());
            VistaUtil.info("Reserva " + creada.getCodigoReserva() + " creada",
                    creada.getApartamento() + "\n" + VistaUtil.fecha(creada.getFechaInicio()) + " → "
                            + VistaUtil.fecha(creada.getFechaFin()) + " · " + creada.getNumeroPersonas() + " personas"
                            + "\nTotal: " + VistaUtil.dinero(creada.getCostoTotal())
                            + (creada.getEstado() == EstadoReserva.PENDIENTE
                            ? "\nQueda Pendiente: paga el anticipo de " + VistaUtil.dinero(creada.getValorAnticipo())
                            + " antes de " + creada.getFechaVencimiento().toLocalDate() + " " + VistaUtil.hora(creada.getFechaVencimiento().toLocalTime())
                            : "\nQueda " + creada.getEstado().getDescripcion() + "."));
            ocupantes.clear();
            limpiarCotizacion();
            onBuscarDisponibles();
            recargarReservas();
            seleccionar(creada);
            pestanas.getSelectionModel().select(tabRegistradas);
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo crear la reserva", ex);
        }
    }

    // ======================================================= grupos grandes

    @FXML
    private void onBuscarGrupo() {
        try {
            combinacionGrupo = sga.asignarGrupoGrande(dpGrupoEntrada.getValue(), dpGrupoSalida.getValue(),
                    spGrupoPersonas.getValue());
            tablaGrupo.setItems(VistaUtil.lista(combinacionGrupo));
            int capacidad = 0;
            for (Apartamento apartamento : combinacionGrupo) {
                capacidad += apartamento.getCapacidadMaxima();
            }
            lblGrupoResumen.setText(combinacionGrupo.estaVacia()
                    ? "No hay una combinación de apartamentos libres que aloje a " + spGrupoPersonas.getValue() + " personas."
                    : combinacionGrupo.tamanio() + " apartamentos alojan hasta " + capacidad + " personas para un grupo de "
                    + spGrupoPersonas.getValue() + ".");
            btnReservarGrupo.setDisable(combinacionGrupo.estaVacia());
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo calcular la combinación", ex);
        }
    }

    @FXML
    private void onReservarGrupo() {
        if (combinacionGrupo.estaVacia()) {
            VistaUtil.info("Sin combinación", "Primero busca una combinación de apartamentos.");
            return;
        }
        Huesped titular = sesion.getRol() == Rol.HUESPED ? sesion.getHuesped() : sga.buscarHuesped(txtGrupoDocumento.getText());
        if (titular == null) {
            VistaUtil.info("Titular requerido", "Escribe el documento de un huésped registrado.");
            return;
        }
        try {
            ListaDoble<Reserva> creadas = sga.reservarGrupo(titular, combinacionGrupo, dpGrupoEntrada.getValue(),
                    dpGrupoSalida.getValue(), spGrupoPersonas.getValue(), cbGrupoCanal.getValue());
            StringBuilder detalle = new StringBuilder();
            for (Reserva reserva : creadas) {
                detalle.append(reserva.getCodigoReserva()).append(" · ").append(reserva.getApartamento().getCodigo())
                        .append(" · ").append(reserva.getNumeroPersonas()).append(" personas · ")
                        .append(VistaUtil.dinero(reserva.getCostoTotal())).append('\n');
            }
            VistaUtil.info("Grupo reservado", detalle.toString());
            combinacionGrupo = new ListaDoble<>();
            tablaGrupo.getItems().clear();
            lblGrupoResumen.setText("");
            recargarReservas();
            pestanas.getSelectionModel().select(tabRegistradas);
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo reservar el grupo", ex);
        }
    }

    // ================================================= reservas registradas

    private void recargarReservas() {
        Reserva seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        ListaDoble<Reserva> reservas;
        if (sesion.getRol() == Rol.HUESPED) {
            reservas = sga.reservasDe(sesion.getHuesped());
        } else {
            reservas = tgPorCodigo.isSelected() ? sga.reservasOrdenadasPorCodigo() : sga.getReservas();
        }
        tablaReservas.setItems(VistaUtil.lista(reservas));
        if (seleccionada != null) {
            seleccionar(seleccionada);
        }
        mostrarDetalle(tablaReservas.getSelectionModel().getSelectedItem());
    }

    private void seleccionar(Reserva reserva) {
        tablaReservas.getSelectionModel().select(reserva);
        tablaReservas.scrollTo(reserva);
    }

    @FXML
    private void onBuscarReserva() {
        Reserva encontrada = sga.buscarReserva(texto(txtBuscarCodigo));
        if (encontrada == null || !tablaReservas.getItems().contains(encontrada)) {
            VistaUtil.info("Sin resultados", "No hay una reserva con el código «" + texto(txtBuscarCodigo) + "».");
            return;
        }
        seleccionar(encontrada);
    }

    private void mostrarDetalle(Reserva reserva) {
        if (reserva == null) {
            lblDetalleReserva.setText("Selecciona una reserva para ver sus acciones.");
            for (Button boton : new Button[]{btnConfirmar, btnAnticipo, btnCheckIn, btnCheckOut, btnNoShow, btnCancelar, btnFolio}) {
                boton.setDisable(true);
            }
            return;
        }
        EstadoReserva estado = reserva.getEstado();
        StringBuilder detalle = new StringBuilder(reserva.getCodigoReserva() + " · " + reserva.getApartamento()
                + " · " + estado.getDescripcion() + " · ocupantes registrados " + reserva.contarOcupantesRegistrados()
                + " de " + reserva.getNumeroPersonas());
        if (estado == EstadoReserva.PENDIENTE) {
            detalle.append("\nAnticipo 30 %: ").append(VistaUtil.dinero(reserva.getValorAnticipo()))
                    .append(" · vence ").append(VistaUtil.fecha(reserva.getFechaVencimiento().toLocalDate())).append(' ')
                    .append(VistaUtil.hora(reserva.getFechaVencimiento().toLocalTime()));
        }
        if (estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA) {
            detalle.append("\nSi se cancela hoy se devolverían ").append(VistaUtil.dinero(sga.calcularDevolucion(reserva)))
                    .append(" (30 % con más de 48 h de anticipación; nada con menos).");
        }
        lblDetalleReserva.setText(detalle.toString());
        btnConfirmar.setDisable(estado != EstadoReserva.PENDIENTE);
        btnAnticipo.setDisable(estado != EstadoReserva.PENDIENTE);
        btnCheckIn.setDisable(estado != EstadoReserva.CONFIRMADA);
        btnCheckOut.setDisable(estado != EstadoReserva.ENCURSO);
        btnNoShow.setDisable(estado != EstadoReserva.CONFIRMADA);
        btnCancelar.setDisable(estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA);
        btnFolio.setDisable(false);
    }

    private Reserva seleccionada() {
        return tablaReservas.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onConfirmar() {
        Reserva reserva = seleccionada();
        if (reserva != null && VistaUtil.confirmar("Confirmar " + reserva.getCodigoReserva(),
                "La reserva quedará Confirmada sin registrar el anticipo. ¿Continuar?")) {
            ejecutar("No se pudo confirmar", () -> sga.confirmarReserva(reserva));
        }
    }

    @FXML
    private void onPagarAnticipo() {
        Reserva reserva = seleccionada();
        if (reserva == null) {
            return;
        }
        double faltante = Math.max(0, reserva.getValorAnticipo() - reserva.getFolio().totalPago());
        if (VistaUtil.confirmar("Pagar anticipo de " + reserva.getCodigoReserva(),
                "Se registrará un pago de " + VistaUtil.dinero(faltante) + " y la reserva quedará Confirmada.")) {
            ejecutar("No se pudo registrar el anticipo", () -> sga.pagarAnticipo(reserva));
        }
    }

    @FXML
    private void onCheckIn() {
        Reserva reserva = seleccionada();
        if (reserva != null && VistaUtil.confirmar("Check-in de " + reserva.getCodigoReserva(),
                "Solo se permite el día de entrada y con el apartamento Preparado. ¿Registrar la llegada?")) {
            ejecutar("No se pudo hacer el check-in", () -> sga.checkIn(reserva));
        }
    }

    @FXML
    private void onCheckOut() {
        Reserva reserva = seleccionada();
        if (reserva != null && VistaUtil.confirmar("Check-out de " + reserva.getCodigoReserva(),
                "La reserva quedará Finalizada y " + reserva.getApartamento().getCodigo() + " entrará a la cola de preparación.")) {
            ejecutar("No se pudo hacer el check-out", () -> sga.checkOut(reserva));
        }
    }

    @FXML
    private void onNoShow() {
        Reserva reserva = seleccionada();
        if (reserva != null && VistaUtil.confirmar("No-show de " + reserva.getCodigoReserva(),
                "El grupo no llegó. La reserva quedará como No-show y sus noches se liberan.")) {
            ejecutar("No se pudo marcar el no-show", () -> sga.marcarNoShow(reserva));
        }
    }

    @FXML
    private void onCancelar() {
        Reserva reserva = seleccionada();
        if (reserva == null) {
            return;
        }
        try {
            double devolucion = sga.calcularDevolucion(reserva); // se muestra ANTES de cancelar
            if (VistaUtil.confirmar("Cancelar " + reserva.getCodigoReserva(),
                    "Se devolverían " + VistaUtil.dinero(devolucion) + ". Las noches del apartamento quedarán libres. ¿Cancelar?")) {
                double devuelto = sga.cancelarReserva(reserva);
                VistaUtil.info("Reserva cancelada", "Devolución: " + VistaUtil.dinero(devuelto));
                onBuscarDisponibles();
                recargarReservas();
            }
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo cancelar", ex);
        }
    }

    @FXML
    private void onVerFolio() {
        Reserva reserva = seleccionada();
        if (reserva != null) {
            principal.abrirFolio(reserva);
        }
    }

    @FXML
    private void onProcesarVencimientos() {
        try {
            ListaDoble<Reserva> canceladas = sga.procesarVencimientos();
            StringBuilder codigos = new StringBuilder();
            for (Reserva reserva : canceladas) {
                codigos.append(codigos.length() == 0 ? "" : ", ").append(reserva.getCodigoReserva());
            }
            VistaUtil.info("Vencimientos procesados", canceladas.estaVacia()
                    ? "Ninguna reserva Pendiente superó las 24 horas."
                    : "Se cancelaron por vencimiento: " + codigos + ". Sus noches quedaron libres.");
            onBuscarDisponibles();
            recargarReservas();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudieron procesar los vencimientos", ex);
        }
    }

    @FXML
    private void onActualizar() {
        onBuscarDisponibles();
        recargarReservas();
    }

    private void ejecutar(String tituloError, Runnable accion) {
        try {
            accion.run();
            recargarReservas();
        } catch (RuntimeException ex) {
            VistaUtil.error(tituloError, ex);
        }
    }
}
