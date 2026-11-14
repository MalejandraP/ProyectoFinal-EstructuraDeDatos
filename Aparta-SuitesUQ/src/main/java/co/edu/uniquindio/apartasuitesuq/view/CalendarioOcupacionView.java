package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.viewController.CalendarioOcupacionViewController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Vista pura del calendario mensual de ocupación por apartamento.
 * Delega la carga de datos y pintado de celdas a {@link CalendarioOcupacionViewController}.
 */
public class CalendarioOcupacionView {

    public static final class MesItem {
        private final Month mes;

        public MesItem(Month mes) {
            this.mes = mes;
        }

        public Month getMes() {
            return mes;
        }

        @Override
        public String toString() {
            String nombre = mes.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CO"));
            return nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
        }
    }

    private final SGA sga;

    private final ComboBox<MesItem> cbMes = new ComboBox<>();
    private final Spinner<Integer> spAnio = new Spinner<>(2020, 2040, 2026);
    private final Button btnMesAnterior = new Button("◀ Mes anterior");
    private final Button btnMesSiguiente = new Button("Mes siguiente ▶");
    private final Button btnActualizar = new Button("Actualizar");
    private final Label lblResumen = Vistas.texto("", "texto-suave");
    private final GridPane cuadricula = new GridPane();

    public CalendarioOcupacionView(SGA sga) {
        this.sga = sga;
    }

    public Node getVista() {
        configurarControles();

        ScrollPane scrollCuadricula = new ScrollPane(cuadricula);
        scrollCuadricula.setFitToHeight(true);
        scrollCuadricula.setPannable(true);
        scrollCuadricula.setStyle("-fx-background-color: transparent; -fx-background: white;");

        VBox tarjetaCalendario = Vistas.tarjeta(
                "Ocupación mensual por apartamento",
                construirBarraSuperior(),
                construirLeyenda(),
                lblResumen,
                scrollCuadricula
        );

        new CalendarioOcupacionViewController(this, sga);

        return Vistas.pagina(
                "Calendario de ocupación",
                "Visualice día a día qué apartamentos están libres, reservados/ocupados o bloqueados fuera de servicio.",
                tarjetaCalendario
        );
    }

    private void configurarControles() {
        for (Month m : Month.values()) {
            cbMes.getItems().add(new MesItem(m));
        }
        spAnio.setEditable(true);
        spAnio.setPrefWidth(95);

        btnMesAnterior.getStyleClass().add("boton-contorno");
        btnMesSiguiente.getStyleClass().add("boton-contorno");
        btnActualizar.getStyleClass().add("boton-primario");

        cuadricula.setHgap(3);
        cuadricula.setVgap(3);
        cuadricula.setPadding(new Insets(8));
    }

    private Node construirBarraSuperior() {
        HBox barra = new HBox(12,
                btnMesAnterior,
                new VBox(4, Vistas.texto("Mes:", "texto-suave"), cbMes),
                new VBox(4, Vistas.texto("Año:", "texto-suave"), spAnio),
                btnMesSiguiente,
                btnActualizar
        );
        barra.setAlignment(Pos.BOTTOM_LEFT);
        return barra;
    }

    private Node construirLeyenda() {
        HBox leyenda = new HBox(18,
                itemLeyenda("#E8F5E9", "#2E7D32", "Libre"),
                itemLeyenda("#E3F2FD", "#1565C0", "Ocupado / Reservado"),
                itemLeyenda("#FFEBEE", "#C62828", "Bloqueado (Fuera de servicio)")
        );
        leyenda.setAlignment(Pos.CENTER_LEFT);
        leyenda.setPadding(new Insets(4, 0, 4, 0));
        return leyenda;
    }

    private HBox itemLeyenda(String fondo, String borde, String texto) {
        Label cuadro = new Label("  ");
        cuadro.setMinSize(18, 18);
        cuadro.setStyle("-fx-background-color: " + fondo + "; -fx-border-color: " + borde
                + "; -fx-border-radius: 4; -fx-background-radius: 4;");
        HBox item = new HBox(6, cuadro, Vistas.texto(texto, "texto-suave"));
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    // ================================================================ getters para el controlador

    public ComboBox<MesItem> getCbMes() { return cbMes; }
    public Spinner<Integer> getSpAnio() { return spAnio; }
    public Button getBtnMesAnterior() { return btnMesAnterior; }
    public Button getBtnMesSiguiente() { return btnMesSiguiente; }
    public Button getBtnActualizar() { return btnActualizar; }
    public Label getLblResumen() { return lblResumen; }
    public GridPane getCuadricula() { return cuadricula; }
}