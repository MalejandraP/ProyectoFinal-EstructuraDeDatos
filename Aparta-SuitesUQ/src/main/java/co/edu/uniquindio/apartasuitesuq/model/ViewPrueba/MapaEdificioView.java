package co.edu.uniquindio.apartasuitesuq.model.ViewPrueba;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.MapaEdificio;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.NodoMapa;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.TipoNodo;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.canvas.GraphicsContext;

import java.util.function.Consumer;
import java.util.function.Function;

public class MapaEdificioView extends Pane {
    private static final double RADIO = 20;

    private final MapaEdificio mapa;
    private final Function<String, Color> colorDe;
    private final Canvas canvas = new Canvas();
    private Recorrido<String> ruta;
    private String seleccionado;
    private Consumer<String> alSeleccionar = c -> { };

    /** colorDe recibe el código del nodo y devuelve su color (null = color por tipo). */
    public MapaEdificioView(MapaEdificio mapa, Function<String, Color> colorDe) {
        this.mapa = mapa;
        this.colorDe = colorDe;
        getChildren().add(canvas);
        setMinSize(300, 240);
        setPrefSize(640, 420);
        widthProperty().addListener((o, a, b) -> redibujar());
        heightProperty().addListener((o, a, b) -> redibujar());
        canvas.setOnMouseClicked(e -> {
            String c = nodoEn(e.getX(), e.getY());
            if (c != null) {
                seleccionado = c;
                alSeleccionar.accept(c);
                redibujar();
            }
        });
    }

    public void setAlSeleccionar(Consumer<String> accion) { this.alSeleccionar = accion; }

    public void mostrarRuta(Recorrido<String> ruta) {
        this.ruta = ruta;
        redibujar();
    }

    private double px(NodoMapa n) { return 40 + n.getX() * (getWidth() - 80); }

    private double py(NodoMapa n) { return 30 + n.getY() * (getHeight() - 60); }

    private String nodoEn(double x, double y) {
        for (NodoMapa n : mapa) {
            double dx = x - px(n), dy = y - py(n);
            if (dx * dx + dy * dy <= RADIO * RADIO) return n.getCodigo();
        }
        return null;
    }

    private boolean enRuta(String a, String b) {
        if (ruta == null) return false;
        for (int i = 0; i + 1 < ruta.tamano(); i++) {
            String p = ruta.obtener(i), q = ruta.obtener(i + 1);
            if ((p.equals(a) && q.equals(b)) || (p.equals(b) && q.equals(a))) return true;
        }
        return false;
    }

    public void redibujar() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#f6f4ef"));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setTextAlign(TextAlignment.CENTER);

        mapa.getGrafo().recorrerPasillos((a, b, metros, cerrado) -> {
            NodoMapa na = mapa.buscar(a), nb = mapa.buscar(b);
            boolean r = enRuta(a, b);
            g.setLineDashes(cerrado ? new double[]{8, 6} : null);
            g.setStroke(cerrado ? Color.web("#d03a3a") : r ? Color.web("#7a3fd1") : Color.web("#8a8a8a"));
            g.setLineWidth(r ? 5 : 2);
            g.strokeLine(px(na), py(na), px(nb), py(nb));
            g.setLineDashes(null);
            g.setFill(Color.web("#444444"));
            g.fillText(((int) metros) + " m", (px(na) + px(nb)) / 2, (py(na) + py(nb)) / 2 - 4);
        });

        for (NodoMapa n : mapa) {
            Color c = colorDe.apply(n.getCodigo());
            if (c == null) c = n.getTipo() == TipoNodo.RECEPCION ? Color.web("#5b3a9e")
                    : n.getTipo() == TipoNodo.ESCALERA ? Color.web("#777777") : Color.web("#4aa3a3");
            g.setFill(c);
            g.fillOval(px(n) - RADIO, py(n) - RADIO, RADIO * 2, RADIO * 2);
            if (n.getCodigo().equals(seleccionado)) {
                g.setStroke(Color.BLACK);
                g.setLineWidth(3);
                g.strokeOval(px(n) - RADIO, py(n) - RADIO, RADIO * 2, RADIO * 2);
            }
            g.setFill(Color.BLACK);
            g.fillText(n.getCodigo(), px(n), py(n) + RADIO + 14);
        }
    }
}
