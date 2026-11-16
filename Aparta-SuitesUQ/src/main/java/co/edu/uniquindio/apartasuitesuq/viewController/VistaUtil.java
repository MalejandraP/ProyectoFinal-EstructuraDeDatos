package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Utilidades comunes de los controladores de vista: carga de FXML con la hoja de estilos,
 * formatos en español de Colombia, diálogos y paso de estructuras propias a listas de JavaFX.
 * No contiene reglas de negocio.
 */
public final class VistaUtil {

    public static final String RUTA = "/co/edu/uniquindio/apartasuitesuq/";
    public static final String CSS = RUTA + "estilos.css";
    public static final Locale CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d MMM yyyy", CO);
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("d MMM", CO);
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", CO);
    private static final double ANCHO = 1320;
    private static final double ALTO = 820;

    private VistaUtil() {
    }

    // ================================================================= FXML

    public static URL recurso(String archivo) {
        URL url = VistaUtil.class.getResource(RUTA + archivo);
        if (url == null) {
            throw new IllegalStateException("No se encontró " + RUTA + archivo);
        }
        return url;
    }

    public static FXMLLoader cargador(String archivo) {
        return new FXMLLoader(recurso(archivo));
    }

    /**
     * Muestra un contenido en la ventana. La escena se crea una sola vez y después solo se cambia
     * su raíz, así la ventana no se redimensiona al pasar del ingreso a la ventana principal.
     */
    public static void mostrarEn(Stage stage, Parent raiz) {
        if (stage.getScene() == null) {
            Scene escena = new Scene(raiz, ANCHO, ALTO);
            escena.getStylesheets().add(VistaUtil.class.getResource(CSS).toExternalForm());
            stage.setScene(escena);
        } else {
            stage.getScene().setRoot(raiz);
        }
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    // ============================================================== formatos

    /** @return valor en pesos con formato es-CO, por ejemplo $1.080.000 */
    public static String dinero(double valor) {
        return "$" + String.format(CO, "%,.0f", valor);
    }

    public static String porcentaje(double valor) {
        return String.format(CO, "%.1f %%", valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "—" : FECHA.format(fecha);
    }

    public static String fechaCorta(LocalDate fecha) {
        return fecha == null ? "—" : FECHA_CORTA.format(fecha);
    }

    public static String fechaLarga(LocalDate fecha) {
        String texto = FECHA_LARGA.format(fecha);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    public static String hora(LocalTime hora) {
        return hora == null ? "—" : String.format("%02d:%02d", hora.getHour(), hora.getMinute());
    }

    /** Sufijo de las clases CSS del semáforo (chip-xxx, punto-xxx, tile-xxx) según el estado. */
    public static String claseEstado(EstadoApartamento estado) {
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

    /** Color HEX del semáforo (Status System Design) para dibujar figuras, como el mapa. */
    public static String colorEstado(EstadoApartamento estado) {
        switch (estado) {
            case PREPARADO:
                return "#5A7D60";
            case OCUPADO:
                return "#3D5B78";
            case PENDIENTEDEPREPARACION:
            case ENPREPARACION:
                return "#C99A45";
            default:
                return "#A84A3F";
        }
    }

    /** Clase CSS del semáforo para un estado de reserva (Confirmada verde, En curso azul...). */
    public static String claseEstado(EstadoReserva estado) {
        switch (estado) {
            case CONFIRMADA:
                return "chip-preparado";
            case ENCURSO:
                return "chip-ocupado";
            case PENDIENTE:
                return "chip-pendiente";
            case CANCELADA:
            case NOSHOW:
                return "chip-fuera";
            default:
                return "chip-neutro";
        }
    }

    // ========================================================= estructuras

    /**
     * Copia una estructura propia (cualquier Iterable: lista, árbol, cola...) a una lista
     * observable, solo para mostrarla. Los datos reales siguen en la estructura propia.
     */
    public static <T> ObservableList<T> lista(Iterable<T> datos) {
        ObservableList<T> lista = FXCollections.observableArrayList();
        for (T dato : datos) {
            lista.add(dato);
        }
        return lista;
    }

    // ============================================================ diálogos

    /** Muestra el mensaje de una excepción de negocio (solapamiento, capacidad, fechas...). */
    public static void error(String titulo, Throwable causa) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(causa.getMessage() == null ? causa.toString() : causa.getMessage());
        estilizar(alerta.getDialogPane());
        alerta.showAndWait();
    }

    public static void info(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        estilizar(alerta.getDialogPane());
        alerta.showAndWait();
    }

    /** @return true si el usuario confirma la acción */
    public static boolean confirmar(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        estilizar(alerta.getDialogPane());
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    /** Aplica la hoja de estilos a un diálogo (los diálogos tienen su propia escena). */
    public static void estilizar(DialogPane panel) {
        panel.getStylesheets().add(VistaUtil.class.getResource(CSS).toExternalForm());
    }

    /** Carga un diálogo cuyo FXML tiene como raíz un DialogPane. */
    public static FXMLLoader cargarDialogo(String archivo) {
        FXMLLoader cargador = cargador(archivo);
        try {
            cargador.load();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar " + archivo + ": " + e.getMessage(), e);
        }
        return cargador;
    }
}
