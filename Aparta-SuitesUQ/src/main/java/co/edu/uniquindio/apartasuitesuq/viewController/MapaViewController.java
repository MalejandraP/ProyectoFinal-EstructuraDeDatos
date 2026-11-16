package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.NodoMapa;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.TipoNodo;
import co.edu.uniquindio.apartasuitesuq.model.Pasillo;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Controlador de mapa.fxml (F). Dibuja el grafo del edificio con figuras de JavaFX: cada
 * apartamento toma el color de su estado operativo, los pasillos cerrados se ven punteados y la
 * ruta calculada con Dijkstra se resalta junto con su costo.
 */
public class MapaViewController implements ControladorPantalla {

    private static final Color AZUL = Color.web("#28374A");
    private static final Color TERRA = Color.web("#754437");
    private static final Color VERDE_OPACO = Color.web("#6B6751");
    private static final Color AREIA = Color.web("#D3C7AD");
    private static final Color BORDE = Color.web("#C9C2B4");
    private static final Color FUERA = Color.web("#A84A3F");

    @FXML private Pane lienzo;
    @FXML private Label lblSeleccion;
    @FXML private ComboBox<String> cbDestino;
    @FXML private Label lblRuta;
    @FXML private VBox cajaPasillos;
    @FXML private ComboBox<Pasillo> cbPasillo;
    @FXML private Label lblInalcanzables;
    @FXML private VBox cajaAnalisis;
    @FXML private Label lblConectividad;
    @FXML private Label lblAnalisis;

    private SGA sga;
    private Recorrido<String> ruta;
    private Recorrido<String> inalcanzables = new Recorrido<>();
    private String seleccionado;

    @FXML
    private void initialize() {
        lienzo.widthProperty().addListener((obs, antes, ahora) -> dibujar());
        lienzo.heightProperty().addListener((obs, antes, ahora) -> dibujar());
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.sga = principal.getSga();
        boolean esAdmin = principal.getSesion().getRol() == Rol.ADMINISTRADOR;
        cajaPasillos.setVisible(esAdmin);
        cajaPasillos.setManaged(esAdmin);
        cajaAnalisis.setVisible(esAdmin);
        cajaAnalisis.setManaged(esAdmin);
        for (Apartamento apartamento : sga.getApartamentos()) {
            if (sga.getMapa().getGrafo().contiene(apartamento.getCodigo())) {
                cbDestino.getItems().add(apartamento.getCodigo());
            }
        }
        if (sga.getMapa().getRecepcion() == null) {
            lblSeleccion.setText("No hay mapa del edificio. Carga un escenario con la sección [MAPA].");
        }
        actualizarPasillos();

        // Pedido desde otra pantalla: ruta hacia un apartamento o ruta de preparación
        String destino = principal.tomarDestinoMapa();
        if (principal.tomarRutaPreparacion()) {
            onRutaPreparacion();
        } else if (destino != null) {
            cbDestino.setValue(destino);
            onRutaDestino();
        }
        dibujar();
    }

    // ================================================================ dibujo

    private double x(NodoMapa nodo) {
        return 50 + nodo.getX() * Math.max(1, lienzo.getWidth() - 100);
    }

    private double y(NodoMapa nodo) {
        return 40 + nodo.getY() * Math.max(1, lienzo.getHeight() - 80);
    }

    private void dibujar() {
        lienzo.getChildren().clear();
        if (sga == null || sga.getMapa().getRecepcion() == null || lienzo.getWidth() <= 0) {
            return;
        }
        // Pasillos (aristas)
        for (Pasillo pasillo : sga.getPasillos()) {
            NodoMapa a = sga.getMapa().buscar(pasillo.getNodoA());
            NodoMapa b = sga.getMapa().buscar(pasillo.getNodoB());
            Line linea = new Line(x(a), y(a), x(b), y(b));
            boolean enRuta = enRuta(pasillo.getNodoA(), pasillo.getNodoB());
            linea.setStroke(pasillo.isCerrado() ? FUERA : enRuta ? TERRA : BORDE);
            linea.setStrokeWidth(enRuta ? 6 : 2.5);
            if (pasillo.isCerrado()) {
                linea.getStrokeDashArray().addAll(9.0, 7.0);
            }
            Text metros = new Text((x(a) + x(b)) / 2 + 4, (y(a) + y(b)) / 2 - 4, Math.round(pasillo.getMetros()) + " m");
            metros.setFill(pasillo.isCerrado() ? FUERA : VERDE_OPACO);
            metros.setFont(Font.font(11));
            lienzo.getChildren().addAll(linea, metros);
        }
        // Nodos
        for (NodoMapa nodo : sga.getMapa()) {
            Apartamento apartamento = nodo.getTipo() == TipoNodo.APARTAMENTO ? sga.buscarApartamento(nodo.getCodigo()) : null;
            double radio = apartamento != null ? 24 : 19;
            Circle circulo = new Circle(x(nodo), y(nodo), radio, colorDe(nodo, apartamento));
            boolean aislado = contiene(inalcanzables, nodo.getCodigo());
            circulo.setStroke(nodo.getCodigo().equals(seleccionado) ? TERRA : aislado ? FUERA : Color.WHITE);
            circulo.setStrokeWidth(nodo.getCodigo().equals(seleccionado) ? 4 : 2.5);
            if (aislado) {
                circulo.getStrokeDashArray().addAll(5.0, 4.0);
            }
            Text etiqueta = new Text(nodo.getCodigo());
            etiqueta.setFont(Font.font("System", FontWeight.BOLD, apartamento != null ? 12 : 10));
            etiqueta.setFill(nodo.getTipo() == TipoNodo.ZONA_ASEO ? AZUL : Color.WHITE);
            etiqueta.setX(x(nodo) - etiqueta.getLayoutBounds().getWidth() / 2);
            etiqueta.setY(y(nodo) + 4);
            etiqueta.setMouseTransparent(true);
            circulo.setCursor(Cursor.HAND);
            circulo.setOnMouseClicked(e -> seleccionar(nodo, apartamento));
            lienzo.getChildren().addAll(circulo, etiqueta);
        }
    }

    private Color colorDe(NodoMapa nodo, Apartamento apartamento) {
        if (apartamento != null) {
            return Color.web(VistaUtil.colorEstado(apartamento.getEstadoOperativo()));
        }
        switch (nodo.getTipo()) {
            case RECEPCION:
                return AZUL;
            case ESCALERA:
                return VERDE_OPACO;
            default:
                return AREIA;
        }
    }

    /** @return true si los dos nodos son consecutivos en la ruta calculada */
    private boolean enRuta(String a, String b) {
        if (ruta == null) {
            return false;
        }
        String anterior = null;
        for (String nodo : ruta) {
            if (anterior != null && ((anterior.equals(a) && nodo.equals(b)) || (anterior.equals(b) && nodo.equals(a)))) {
                return true;
            }
            anterior = nodo;
        }
        return false;
    }

    private static boolean contiene(Recorrido<String> recorrido, String codigo) {
        for (String nodo : recorrido) {
            if (nodo.equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    private void seleccionar(NodoMapa nodo, Apartamento apartamento) {
        seleccionado = nodo.getCodigo();
        String distancia = contiene(inalcanzables, nodo.getCodigo()) ? "inalcanzable desde recepción"
                : String.format("%.0f m desde recepción", sga.getAnalisisGrafo().distanciaDesdeRecepcion(nodo.getCodigo()));
        if (apartamento == null) {
            lblSeleccion.setText(nodo.getCodigo() + " · " + descripcion(nodo.getTipo()) + " · " + distancia);
        } else {
            EstadoApartamento estado = apartamento.getEstadoOperativo();
            StringBuilder texto = new StringBuilder(apartamento + "\nEstado: " + estado.getDescripcion() + " · " + distancia);
            if (estado == EstadoApartamento.FUERADESERVICIO) {
                texto.append("\nMotivo: ").append(sga.motivoFueraDeServicio(apartamento));
            }
            Reserva enCurso = sga.reservaEnCurso(apartamento);
            if (enCurso != null) {
                texto.append("\nHuésped: ").append(enCurso.getHuesped().getNombre()).append(" (").append(enCurso.getCodigoReserva()).append(')');
            }
            lblSeleccion.setText(texto.toString());
            cbDestino.setValue(apartamento.getCodigo());
        }
        dibujar();
    }

    private static String descripcion(TipoNodo tipo) {
        switch (tipo) {
            case RECEPCION:
                return "Recepción";
            case ESCALERA:
                return "Escalera";
            case ZONA_ASEO:
                return "Cuarto de aseo";
            default:
                return "Apartamento";
        }
    }

    // ================================================================ rutas

    @FXML
    private void onRutaDestino() {
        if (cbDestino.getValue() == null) {
            VistaUtil.info("Elige un destino", "Selecciona un apartamento en la lista o en el mapa.");
            return;
        }
        try {
            mostrarRuta(sga.rutaHacia(cbDestino.getValue()), "Ruta desde recepción hasta " + cbDestino.getValue());
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo calcular la ruta", ex);
        }
    }

    @FXML
    private void onRutaPreparacion() {
        try {
            mostrarRuta(sga.rutaPreparacion(), "Ruta de preparación (pendientes en orden de la cola)");
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo calcular la ruta", ex);
        }
    }

    private void mostrarRuta(Recorrido<String> calculada, String titulo) {
        if (calculada.estaVacio()) {
            ruta = null;
            lblRuta.setText(titulo + ": el destino es inalcanzable con los pasillos cerrados.");
        } else {
            ruta = calculada;
            StringBuilder camino = new StringBuilder();
            for (String nodo : calculada) {
                camino.append(camino.length() == 0 ? "" : " → ").append(nodo);
            }
            lblRuta.setText(titulo + ":\n" + camino + "\nCosto: " + String.format("%.0f m", calculada.getDistancia()));
        }
        dibujar();
    }

    @FXML
    private void onQuitarRuta() {
        ruta = null;
        lblRuta.setText("");
        dibujar();
    }

    // ============================================================= pasillos

    private void actualizarPasillos() {
        Pasillo elegido = cbPasillo.getValue();
        cbPasillo.setItems(VistaUtil.lista(sga.getPasillos()));
        if (elegido != null) {
            for (Pasillo pasillo : cbPasillo.getItems()) {
                if (pasillo.getNodoA().equals(elegido.getNodoA()) && pasillo.getNodoB().equals(elegido.getNodoB())) {
                    cbPasillo.setValue(pasillo);
                }
            }
        }
        inalcanzables = sga.inalcanzables();
        StringBuilder lista = new StringBuilder();
        for (String nodo : inalcanzables) {
            lista.append(lista.length() == 0 ? "" : ", ").append(nodo);
        }
        lblInalcanzables.setText(inalcanzables.estaVacio() ? "Todo el edificio es alcanzable desde recepción."
                : "Inalcanzables desde recepción: " + lista);
        if (sga.getMapa().getRecepcion() != null) {
            int[] pasillos = sga.getAnalisisGrafo().contarPasillos();
            lblConectividad.setText(sga.getAnalisisGrafo().componentesConexas() + " componente(s) conexa(s) · "
                    + pasillos[0] + " de " + pasillos[1] + " pasillos cerrados");
        }
    }

    @FXML
    private void onCerrarPasillo() {
        cambiarPasillo(true);
    }

    @FXML
    private void onAbrirPasillo() {
        cambiarPasillo(false);
    }

    private void cambiarPasillo(boolean cerrar) {
        Pasillo pasillo = cbPasillo.getValue();
        if (pasillo == null) {
            VistaUtil.info("Elige un pasillo", "Selecciona el pasillo que quieres cerrar o abrir.");
            return;
        }
        if (cerrar) {
            sga.cerrarPasillo(pasillo.getNodoA(), pasillo.getNodoB());
        } else {
            sga.abrirPasillo(pasillo.getNodoA(), pasillo.getNodoB());
        }
        ruta = null;
        lblRuta.setText("");
        actualizarPasillos();
        dibujar();
    }

    // ============================================================= análisis

    @FXML
    private void onBfs() {
        lblAnalisis.setText("BFS desde recepción (por niveles):\n" + unir(sga.recorridoBfs()));
    }

    @FXML
    private void onDfs() {
        lblAnalisis.setText("DFS desde recepción (en profundidad, recursivo):\n" + unir(sga.recorridoDfs()));
    }

    @FXML
    private void onAgruparBloques() {
        StringBuilder grupos = new StringBuilder("Apartamentos por bloque (BFS desde recepción):");
        for (String bloque : sga.bloques()) {
            Recorrido<String> recorrido = sga.recorridoBloque(bloque);
            grupos.append("\nBloque ").append(bloque).append(": ")
                    .append(recorrido.estaVacio() ? "ninguno alcanzable" : unir(recorrido));
        }
        lblAnalisis.setText(grupos.toString());
    }

    private static String unir(Recorrido<String> recorrido) {
        StringBuilder texto = new StringBuilder();
        for (String nodo : recorrido) {
            texto.append(texto.length() == 0 ? "" : " · ").append(nodo);
        }
        return texto.toString();
    }

    @FXML
    private void onActualizar() {
        actualizarPasillos();
        dibujar();
    }
}
