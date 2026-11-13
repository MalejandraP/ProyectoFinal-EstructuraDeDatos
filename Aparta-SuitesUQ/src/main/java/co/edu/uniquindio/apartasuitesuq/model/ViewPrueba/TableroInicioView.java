package co.edu.uniquindio.apartasuitesuq.model.ViewPrueba;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.AnalisisGrafo;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioPreparacion;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class TableroInicioView extends FlowPane {
    private final Iterable<Apartamento> apartamentos;
    private final ServicioPreparacion servicio;
    private final AnalisisGrafo analisis;

    public TableroInicioView(Iterable<Apartamento> apartamentos, ServicioPreparacion servicio, AnalisisGrafo analisis) {
        this.apartamentos = apartamentos;
        this.servicio = servicio;
        this.analisis = analisis;
        setHgap(12);
        setVgap(12);
        setPadding(new javafx.geometry.Insets(12));
        refrescar();
    }

    private VBox tarjeta(String titulo, String valor, String color) {
        Label t = new Label(titulo);
        Label v = new Label(valor);
        v.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");
        t.setStyle("-fx-text-fill: white;");
        VBox caja = new VBox(4, t, v);
        caja.setPadding(new javafx.geometry.Insets(12));
        caja.setPrefWidth(190);
        caja.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");
        return caja;
    }

    private static String hex(EstadoApartamento e) {
        javafx.scene.paint.Color c = ColoresEstado.de(e);
        return String.format("#%02x%02x%02x", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    public void refrescar() {
        EstadoApartamento[] estados = EstadoApartamento.values();
        int[] conteo = new int[estados.length];
        for (Apartamento a : apartamentos) conteo[a.getEstado().ordinal()]++;
        getChildren().clear();
        for (EstadoApartamento e : estados) {
            getChildren().add(tarjeta(e.name(), String.valueOf(conteo[e.ordinal()]), hex(e)));
        }
        getChildren().add(tarjeta("Tiempo medio de preparación", String.format("%.0f min", servicio.tiempoMedioPreparacionMin()), "#5b3a9e"));
        int[] p = analisis.contarPasillos();
        getChildren().add(tarjeta("Pasillos cerrados", p[0] + " de " + p[1], "#666666"));
        getChildren().add(tarjeta("Aislados desde recepción", String.valueOf(analisis.apartamentosAislados().tamano()), "#444444"));
    }
}
