package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.controller.DatosPrueba;
import co.edu.uniquindio.apartasuitesuq.controller.Sesion;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Pila;
import co.edu.uniquindio.apartasuitesuq.model.Recepcionista;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador de principal.fxml. Muestra en el menú solo las pantallas del rol que inició
 * sesión, carga el FXML de cada pantalla en el centro y guarda el historial de navegación en
 * una {@link Pila} propia para el botón "Atrás".
 */
public class PrincipalViewController {

    @FXML private Label lblSeccionHuesped;
    @FXML private Label lblSeccionOperacion;
    @FXML private Label lblSeccionAdmin;
    @FXML private Button btnMiPanel;
    @FXML private Button btnInicio;
    @FXML private Button btnReservas;
    @FXML private Button btnCanalExterno;
    @FXML private Button btnCalendario;
    @FXML private Button btnPreparacion;
    @FXML private Button btnMapa;
    @FXML private Button btnFolio;
    @FXML private Button btnAdministracion;
    @FXML private Button btnTemporadas;
    @FXML private Button btnEstadisticas;
    @FXML private Button btnCarga;

    @FXML private Button btnAtras;
    @FXML private Label lblTitulo;
    @FXML private Label lblHistorial;
    @FXML private HBox cajaTurno;
    @FXML private Label lblTurno;
    @FXML private Label lblAvatar;
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private StackPane contenedor;

    private final Pila<Pantalla> historial = new Pila<>();
    private Button[] botones;
    private Sesion sesion;
    private Pantalla actual;

    // datos que una pantalla le pasa a la siguiente (por ejemplo, Reservas → Folio)
    private co.edu.uniquindio.apartasuitesuq.model.Reserva reservaParaFolio;
    private String destinoMapa;
    private boolean rutaPreparacionEnMapa;

    /** Carga principal.fxml en la ventana para la sesión indicada. */
    public static void abrir(Stage stage, Sesion sesion) {
        try {
            FXMLLoader cargador = VistaUtil.cargador("principal.fxml");
            Parent raiz = cargador.load();
            PrincipalViewController controlador = cargador.getController();
            controlador.iniciar(sesion);
            stage.setTitle("SGA – Aparta-Suites UQ · " + sesion.getRol().getDescripcion());
            VistaUtil.mostrarEn(stage, raiz);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir la ventana principal: " + e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        botones = new Button[]{btnMiPanel, btnInicio, btnReservas, btnCanalExterno, btnCalendario,
                btnPreparacion, btnMapa, btnFolio, btnAdministracion, btnTemporadas, btnEstadisticas, btnCarga};
    }

    private void iniciar(Sesion sesion) {
        this.sesion = sesion;
        for (Button boton : botones) {
            boolean visible = pantallaDe(boton).permitidaPara(sesion.getRol());
            boton.setVisible(visible);
            boton.setManaged(visible);
        }
        mostrarSeccion(lblSeccionHuesped, btnMiPanel);
        mostrarSeccion(lblSeccionOperacion, btnInicio, btnReservas, btnCanalExterno, btnCalendario,
                btnPreparacion, btnMapa, btnFolio);
        mostrarSeccion(lblSeccionAdmin, btnAdministracion, btnTemporadas, btnEstadisticas, btnCarga);

        lblAvatar.setText(sesion.getIniciales());
        lblUsuario.setText(sesion.getNombre());
        lblRol.setText(sesion.getRol().getDescripcion());
        cajaTurno.setVisible(sesion.esEmpleado());
        cajaTurno.setManaged(sesion.esEmpleado());
        actualizarTurno();
        mostrarPantalla(Pantalla.inicialDe(sesion.getRol()));
    }

    /** Un título de sección del menú solo se ve si alguno de sus botones es visible. */
    private static void mostrarSeccion(Label titulo, Button... botonesSeccion) {
        boolean alguno = false;
        for (Button boton : botonesSeccion) {
            alguno |= boton.isVisible();
        }
        titulo.setVisible(alguno);
        titulo.setManaged(alguno);
    }

    private static Pantalla pantallaDe(Button boton) {
        return Pantalla.valueOf((String) boton.getUserData());
    }

    // =========================================================== navegación

    @FXML
    private void onNavegar(ActionEvent evento) {
        navegar(pantallaDe((Button) evento.getSource()));
    }

    @FXML
    private void onAtras() {
        volver();
    }

    /** Abre una pantalla guardando la actual en la pila del historial. */
    public void navegar(Pantalla pantalla) {
        if (!pantalla.permitidaPara(sesion.getRol())) {
            VistaUtil.info("Sin acceso", "El rol " + sesion.getRol().getDescripcion()
                    + " no puede abrir «" + pantalla.getTitulo() + "».");
            return;
        }
        if (actual != null && actual != pantalla) {
            historial.apilar(actual);
        }
        mostrarPantalla(pantalla);
    }

    /** Vuelve a la pantalla anterior desapilando el historial. */
    public void volver() {
        if (!historial.estaVacia()) {
            mostrarPantalla(historial.desapilar());
        }
    }

    /** Vuelve a cargar la pantalla actual (después de cambiar datos desde otra parte). */
    public void refrescar() {
        mostrarPantalla(actual);
    }

    private void mostrarPantalla(Pantalla pantalla) {
        actual = pantalla;
        lblTitulo.setText(pantalla.getTitulo());
        int anteriores = 0;
        for (Pantalla ignored : historial) { // recorre la pila con su iterador
            anteriores++;
        }
        lblHistorial.setText(anteriores == 0 ? "Sin pantallas anteriores"
                : anteriores + (anteriores == 1 ? " pantalla" : " pantallas") + " en el historial");
        btnAtras.setDisable(historial.estaVacia());
        for (Button boton : botones) {
            boton.getStyleClass().remove("nav-item-activo");
            if (pantallaDe(boton) == pantalla) {
                boton.getStyleClass().add("nav-item-activo");
            }
        }
        try {
            contenedor.getChildren().setAll(crear(pantalla));
        } catch (IOException | RuntimeException ex) {
            Label aviso = new Label("No se pudo abrir «" + pantalla.getTitulo() + "»: " + ex.getMessage());
            aviso.getStyleClass().add("caja-info");
            aviso.setWrapText(true);
            contenedor.getChildren().setAll(aviso);
        }
    }

    /** Cada pantalla se construye de nuevo al abrirla, así siempre muestra los datos actuales. */
    private Node crear(Pantalla pantalla) throws IOException {
        FXMLLoader cargador = VistaUtil.cargador(pantalla.getFxml());
        Parent vista = cargador.load();
        ((ControladorPantalla) cargador.getController()).inicializar(this);
        return vista;
    }

    // ================================================= atajos entre pantallas

    /** Abre el folio de una reserva (desde Reservas). */
    public void abrirFolio(co.edu.uniquindio.apartasuitesuq.model.Reserva reserva) {
        reservaParaFolio = reserva;
        navegar(Pantalla.FOLIO);
    }

    /** @return la reserva que el folio debe mostrar al abrirse, o null */
    public co.edu.uniquindio.apartasuitesuq.model.Reserva tomarReservaParaFolio() {
        co.edu.uniquindio.apartasuitesuq.model.Reserva reserva = reservaParaFolio;
        reservaParaFolio = null;
        return reserva;
    }

    /**
     * Abre el mapa mostrando una ruta: hacia un apartamento (desde Inicio) o la ruta de
     * preparación (desde Preparación).
     */
    public void abrirMapa(String codigoDestino, boolean rutaPreparacion) {
        destinoMapa = codigoDestino;
        rutaPreparacionEnMapa = rutaPreparacion;
        navegar(Pantalla.MAPA);
    }

    public String tomarDestinoMapa() {
        String destino = destinoMapa;
        destinoMapa = null;
        return destino;
    }

    public boolean tomarRutaPreparacion() {
        boolean pedida = rutaPreparacionEnMapa;
        rutaPreparacionEnMapa = false;
        return pedida;
    }

    // ============================================================ encabezado

    @FXML
    private void onPasarTurno() {
        if (getSga().getTurnos().hayRecepcionistas()) {
            getSga().pasarTurno();
            actualizarTurno();
        }
    }

    /** Vuelve a leer el turno actual (lo usan las pantallas que cambian los recepcionistas). */
    public void refrescarTurno() {
        actualizarTurno();
    }

    private void actualizarTurno() {
        Recepcionista enTurno = getSga().getTurnos().hayRecepcionistas() ? getSga().turnoActual() : null;
        lblTurno.setText(enTurno == null ? "Sin recepcionistas en turno" : "En turno: " + enTurno.getNombre());
    }

    @FXML
    private void onCerrarSesion() {
        LoginViewController.mostrar((Stage) contenedor.getScene().getWindow());
    }

    /**
     * Tras cargar un escenario nuevo: si el usuario ya no existe se cierra la sesión;
     * si existe, se actualiza el encabezado.
     */
    public void alCargarDatos() {
        if (sesion.esEmpleado() && getSga().buscarEmpleado(sesion.getEmpleado().getId()) == null) {
            VistaUtil.info("Sesión cerrada", "Tu usuario no existe en el escenario cargado. Ingresa de nuevo.");
            onCerrarSesion();
            return;
        }
        actualizarTurno();
    }

    // ============================================================== acceso

    public SGA getSga() {
        return DatosPrueba.sga();
    }

    public Sesion getSesion() {
        return sesion;
    }

    public Pantalla getActual() {
        return actual;
    }
}
