package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** Utilidades comunes de las pantallas: estilos, textos, alertas y conversión de estructuras. */
final class Vistas {

    static final String CSS = "/co/edu/uniquindio/apartasuitesuq/estilos.css";

    private Vistas() {
    }

    static final double ANCHO = 1280;
    static final double ALTO = 800;

    /**
     * Muestra un contenido en la ventana. La escena se crea una sola vez y luego solo se cambia
     * su raíz: así la ventana no cambia de tamaño al navegar (en macOS, redimensionar mientras
     * se cambia de escena hace fallar a JavaFX 17).
     */
    static void mostrarEn(javafx.stage.Stage stage, Parent raiz) {
        if (stage.getScene() == null) {
            stage.setScene(escena(raiz, ANCHO, ALTO));
        } else {
            stage.getScene().setRoot(raiz);
        }
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    /** Crea una escena con la hoja de estilos de la aplicación. */
    static Scene escena(Parent raiz, double ancho, double alto) {
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(Vistas.class.getResource(CSS).toExternalForm());
        return escena;
    }

    /** Página estándar: título, subtítulo y contenido, con desplazamiento vertical. */
    static ScrollPane pagina(String titulo, String subtitulo, Node... contenido) {
        VBox caja = new VBox(16);
        caja.getStyleClass().add("contenido");
        caja.getChildren().add(texto(titulo, "titulo-pagina"));
        if (subtitulo != null) {
            caja.getChildren().add(texto(subtitulo, "texto-suave"));
        }
        caja.getChildren().addAll(contenido);
        ScrollPane scroll = new ScrollPane(caja);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F9F8F6;");
        return scroll;
    }

    static VBox tarjeta(String titulo, Node... contenido) {
        VBox caja = new VBox(12);
        caja.getStyleClass().add("tarjeta");
        if (titulo != null) {
            caja.getChildren().add(texto(titulo, "titulo-seccion"));
        }
        caja.getChildren().addAll(contenido);
        return caja;
    }

    static Label texto(String texto, String claseCss) {
        Label label = new Label(texto);
        label.setWrapText(true);
        if (claseCss != null) {
            label.getStyleClass().add(claseCss);
        }
        return label;
    }

    /** Recuadro informativo para pantallas sin datos. */
    static Label aviso(String texto) {
        Label label = texto(texto, "caja-info");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    static String moneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    static String porcentaje(double valor) {
        return String.format("%.1f %%", valor);
    }

    /** Copia una estructura propia (cualquier Iterable) a la lista observable que usan las tablas. */
    static <T> ObservableList<T> lista(Iterable<T> datos) {
        ObservableList<T> lista = FXCollections.observableArrayList();
        for (T dato : datos) {
            lista.add(dato);
        }
        return lista;
    }

    /** Sufijo de las clases CSS de color (chip-xxx, tile-xxx) según el estado operativo. */
    static String claseEstado(EstadoApartamento estado) {
        switch (estado) {
            case PREPARADO:
                return "preparado";
            case OCUPADO:
                return "ocupado";
            case PENDIENTEDEPREPARACION:
            case ENPREPARACION:
                return "pendiente";
            default:
                return "fuera";
        }
    }

    static void error(String titulo, Throwable causa) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(causa.getMessage() == null ? causa.toString() : causa.getMessage());
        alerta.showAndWait();
    }

    static void info(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
