package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Pila;
import co.edu.uniquindio.apartasuitesuq.model.Recepcionista;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.ColoresEstado;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.MapaEdificioView;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.PanelPreparacionView;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.TableroInicioView;
import co.edu.uniquindio.apartasuitesuq.viewController.PanelUsuarioViewController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Ventana principal. El menú lateral muestra solo las pantallas del rol que inició sesión:
 * <ul>
 *   <li><b>Huésped:</b> mi panel y cotizar estancia.</li>
 *   <li><b>Recepcionista:</b> inicio, mapa y preparación, folio y cotizar.</li>
 *   <li><b>Administrador:</b> además, temporadas y tarifas, estadísticas y carga de datos.</li>
 * </ul>
 * El botón "Atrás" usa una {@link Pila} propia con el historial de navegación.
 */
public class VentanaPrincipal {

    /** Pantallas de la aplicación. */
    enum Pantalla {
        INICIO("Inicio"),
        MAPA("Mapa y preparación"),
        MI_PANEL("Mi panel"),
        FOLIO("Folio"),
        COTIZAR("Cotizar estancia"),
        TEMPORADAS("Temporadas y tarifas"),
        ESTADISTICAS("Estadísticas"),
        CARGA("Carga de datos");

        final String titulo;

        Pantalla(String titulo) {
            this.titulo = titulo;
        }
    }

    private static final String FXML_PANEL_USUARIO = "/co/edu/uniquindio/apartasuitesuq/panel-usuario.fxml";
    private static final String FXML_FOLIO = "/co/edu/uniquindio/apartasuitesuq/folio.fxml";

    private final Stage stage;
    private final SGA sga;
    private final Sesion sesion;
    private final Pantalla[] pantallas;
    private final Button[] botones = new Button[Pantalla.values().length];

    private final BorderPane raiz = new BorderPane();
    private final Label tituloPantalla = new Label();
    private final Label turno = new Label();
    private final Button atras = new Button("← Atrás");
    private final Pila<Pantalla> historial = new Pila<>();
    private Pantalla actual;

    public VentanaPrincipal(Stage stage, SGA sga, Sesion sesion) {
        this.stage = stage;
        this.sga = sga;
        this.sesion = sesion;
        this.pantallas = pantallasDe(sesion.getRol());
    }

    /** @return las pantallas permitidas para cada rol, en el orden del menú */
    static Pantalla[] pantallasDe(Rol rol) {
        switch (rol) {
            case HUESPED:
                return new Pantalla[]{Pantalla.MI_PANEL, Pantalla.COTIZAR};
            case RECEPCIONISTA:
                return new Pantalla[]{Pantalla.INICIO, Pantalla.MAPA, Pantalla.FOLIO, Pantalla.COTIZAR};
            default:
                return new Pantalla[]{Pantalla.INICIO, Pantalla.MAPA, Pantalla.COTIZAR, Pantalla.TEMPORADAS,
                        Pantalla.ESTADISTICAS, Pantalla.CARGA, Pantalla.FOLIO};
        }
    }

    public void mostrar() {
        raiz.setLeft(construirMenu());
        raiz.setTop(construirEncabezado());
        mostrarPantalla(pantallas[0]);
        stage.setTitle("SGA – Aparta-Suites UQ · " + sesion.getRol().getDescripcion());
        Vistas.mostrarEn(stage, raiz);
    }

    // ================================================================ estructura

    private Parent construirMenu() {
        VBox menu = new VBox(6);
        menu.getStyleClass().add("sidebar");
        menu.setPrefWidth(230);
        menu.getChildren().addAll(
                Vistas.texto("Aparta-Suites UQ", "marca-titulo"),
                Vistas.texto("SGA · " + sesion.getRol().getDescripcion(), "marca-subtitulo"));
        Region espacio = new Region();
        espacio.setMinHeight(18);
        menu.getChildren().add(espacio);

        for (Pantalla pantalla : pantallas) {
            Button boton = new Button(pantalla.titulo);
            boton.getStyleClass().add("nav-item");
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setOnAction(e -> navegar(pantalla));
            botones[pantalla.ordinal()] = boton;
            menu.getChildren().add(boton);
        }

        Region relleno = new Region();
        VBox.setVgrow(relleno, Priority.ALWAYS);
        Button salir = new Button("Cerrar sesión");
        salir.getStyleClass().add("nav-item");
        salir.setMaxWidth(Double.MAX_VALUE);
        salir.setOnAction(e -> new LoginView(stage, sga).mostrar());
        menu.getChildren().addAll(relleno, Vistas.texto(sesion.getNombre(), "marca-subtitulo"), salir);
        return menu;
    }

    private Parent construirEncabezado() {
        atras.getStyleClass().add("boton-contorno");
        atras.setOnAction(e -> volver());
        tituloPantalla.getStyleClass().add("titulo-seccion");

        Region relleno = new Region();
        HBox.setHgrow(relleno, Priority.ALWAYS);
        HBox encabezado = new HBox(14, atras, tituloPantalla, relleno);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.setPadding(new Insets(12, 24, 12, 24));
        encabezado.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #E6E2DB transparent;");

        if (sesion.getEmpleado() != null) {
            Button pasar = new Button("Pasar turno");
            pasar.getStyleClass().add("boton-secundario");
            pasar.setOnAction(e -> {
                if (sga.getTurnos().hayRecepcionistas()) {
                    sga.pasarTurno();
                    actualizarTurno();
                }
            });
            turno.getStyleClass().add("texto-suave");
            actualizarTurno();
            encabezado.getChildren().addAll(turno, pasar);
        }
        return encabezado;
    }

    private void actualizarTurno() {
        Recepcionista enTurno = sga.getTurnos().hayRecepcionistas() ? sga.turnoActual() : null;
        turno.setText(enTurno == null ? "Sin recepcionistas en turno" : "En turno: " + enTurno.getNombre());
    }

    // ================================================================ navegación

    /** Abre una pantalla guardando la actual en la pila del historial. */
    void navegar(Pantalla pantalla) {
        if (actual != null && actual != pantalla) {
            historial.apilar(actual);
        }
        mostrarPantalla(pantalla);
    }

    /** Vuelve a la pantalla anterior desapilando el historial. */
    void volver() {
        if (!historial.estaVacia()) {
            mostrarPantalla(historial.desapilar());
        }
    }

    private void mostrarPantalla(Pantalla pantalla) {
        actual = pantalla;
        tituloPantalla.setText(pantalla.titulo);
        for (Pantalla p : pantallas) {
            Button boton = botones[p.ordinal()];
            boton.getStyleClass().remove("nav-item-activo");
            if (p == pantalla) {
                boton.getStyleClass().add("nav-item-activo");
            }
        }
        atras.setDisable(historial.estaVacia());
        try {
            raiz.setCenter(crear(pantalla));
        } catch (Exception ex) {
            raiz.setCenter(Vistas.pagina(pantalla.titulo, null,
                    Vistas.aviso("No se pudo abrir la pantalla: " + ex.getMessage())));
        }
    }

    /** Cada pantalla se construye de nuevo al abrirla, así siempre muestra los datos actuales. */
    private Node crear(Pantalla pantalla) throws IOException {
        switch (pantalla) {
            case INICIO:
                return crearInicio();
            case MAPA:
                return crearMapa();
            case MI_PANEL:
                return crearPanelUsuario();
            case FOLIO:
                return new FXMLLoader(VentanaPrincipal.class.getResource(FXML_FOLIO)).load();
            case COTIZAR:
                return new CotizacionView(sga, sesion).getVista();
            case TEMPORADAS:
                return new TemporadasTarifasView(sga).getVista();
            case ESTADISTICAS:
                return new EstadisticasView(sga).getVista();
            case CARGA:
                return new CargaDatosView(sga, this::alCargarDatos).getVista();
            default:
                throw new IllegalArgumentException("Pantalla desconocida: " + pantalla);
        }
    }

    /** Tras cargar un escenario nuevo, si el usuario ya no existe se cierra la sesión. */
    private void alCargarDatos() {
        if (sesion.getEmpleado() != null && sga.buscarEmpleado(sesion.getEmpleado().getId()) == null) {
            Vistas.info("Sesión cerrada", "Su usuario no existe en el escenario cargado. Ingrese de nuevo.");
            new LoginView(stage, sga).mostrar();
            return;
        }
        actualizarTurno();
    }

    // ============================================================ pantallas

    /** Inicio: resumen por estado (P3) y tablero con cada apartamento coloreado por su estado. */
    private Node crearInicio() {
        if (sga.getApartamentos().estaVacio()) {
            return Vistas.pagina("Inicio", null,
                    Vistas.aviso("No hay apartamentos. El administrador puede cargar el escenario en «Carga de datos»."));
        }
        FlowPane tablero = new FlowPane(12, 12);
        for (Apartamento apartamento : sga.getApartamentos()) {
            String clase = Vistas.claseEstado(apartamento.getEstado());
            Label estado = Vistas.texto(apartamento.getEstado().getDescripcion(), "chip");
            estado.getStyleClass().add("chip-" + clase);
            VBox tile = new VBox(6,
                    Vistas.texto(apartamento.getCodigo(), "titulo-seccion"),
                    Vistas.texto(apartamento.getNombre(), "texto-suave"),
                    Vistas.texto("Bloque " + apartamento.getBloque() + " · piso " + apartamento.getPiso()
                            + " · " + apartamento.getCapacidadMaxima() + " personas", "texto-suave"),
                    estado);
            tile.getStyleClass().addAll("tile-resumen", "tile-" + clase);
            tile.setPrefWidth(210);
            String motivo = sga.motivoFueraDeServicio(apartamento);
            if (motivo != null && apartamento.estaFueraDeServicio()) {
                Tooltip.install(tile, new Tooltip("Motivo: " + motivo));
            }
            tablero.getChildren().add(tile);
        }

        VBox contenido = new VBox(16);
        try {
            contenido.getChildren().add(new TableroInicioView(sga.getApartamentos(),
                    sga.getServicioPreparacion(), sga.getAnalisisGrafo()));
        } catch (RuntimeException ex) {
            // sin mapa del edificio el resumen del grafo no se puede calcular; el tablero sí
        }
        contenido.getChildren().addAll(Vistas.texto("Apartamentos", "titulo-seccion"), tablero);
        return Vistas.pagina("Inicio", "Estado operativo de cada apartamento: verde Preparado, azul Ocupado, "
                + "amarillo Pendiente o En preparación, rojo Fuera de servicio.", contenido);
    }

    /** Mapa del edificio y panel de preparación (vistas de P3) alimentados por la fachada. */
    private Node crearMapa() {
        if (sga.getMapa().getRecepcion() == null) {
            return Vistas.pagina("Mapa y preparación", null,
                    Vistas.aviso("No hay mapa del edificio. Cargue un escenario con la sección [MAPA]."));
        }
        MapaEdificioView mapa = new MapaEdificioView(sga.getMapa(), codigo -> {
            Apartamento apartamento = sga.buscarApartamento(codigo);
            return apartamento == null ? null : ColoresEstado.de(apartamento.getEstado());
        });
        Label detalle = Vistas.texto("Haga clic en un nodo del mapa", "texto-suave");
        mapa.setAlSeleccionar(codigo -> {
            Apartamento apartamento = sga.buscarApartamento(codigo);
            if (apartamento != null) {
                String motivo = apartamento.estaFueraDeServicio() ? " — " + sga.motivoFueraDeServicio(apartamento) : "";
                detalle.setText(apartamento + ": " + apartamento.getEstado().getDescripcion() + motivo);
            } else {
                detalle.setText(codigo + ": " + sga.getMapa().buscar(codigo).getTipo() + " ("
                        + sga.getAnalisisGrafo().distanciaDesdeRecepcion(codigo) + " m desde recepción)");
            }
        });
        PanelPreparacionView panel = new PanelPreparacionView(sga.getServicioPreparacion(),
                sga.getColaPreparacion(), sga.getApartamentos(), mapa::redibujar);

        Button ruta = new Button("Mostrar ruta de preparación");
        ruta.getStyleClass().add("boton-primario");
        ruta.setOnAction(e -> {
            try {
                mapa.mostrarRuta(sga.rutaPreparacion());
            } catch (RuntimeException ex) {
                Vistas.error("No se pudo calcular la ruta", ex);
            }
        });

        HBox barra = new HBox(12, ruta, detalle);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(12));
        BorderPane contenedor = new BorderPane(mapa);
        contenedor.setTop(barra);
        contenedor.setRight(panel);
        contenedor.setStyle("-fx-background-color: white;");
        Platform.runLater(mapa::redibujar);
        return contenedor;
    }

    /** Panel de usuario del huésped (pantalla FXML del equipo) con sus reservas reales. */
    private Node crearPanelUsuario() throws IOException {
        FXMLLoader loader = new FXMLLoader(VentanaPrincipal.class.getResource(FXML_PANEL_USUARIO));
        Parent vista = loader.load();
        PanelUsuarioViewController controlador = loader.getController();
        controlador.setHuesped(sesion.getHuesped());
        controlador.setReservas(sga.reservasDe(sesion.getHuesped()));
        return vista;
    }
}
