package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.Empleado;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Pantalla de ingreso. Los empleados (recepcionista o administrador) entran con su id y
 * contraseña; el huésped entra con su documento. Si el sistema está vacío, se puede cargar
 * el escenario de prueba desde aquí.
 */
public class LoginView {

    private final Stage stage;
    private final SGA sga;
    private final Label mensaje = new Label();
    private final Label resumen = new Label();

    public LoginView(Stage stage, SGA sga) {
        this.stage = stage;
        this.sga = sga;
    }

    public void mostrar() {
        stage.setTitle("SGA – Aparta-Suites UQ");
        Vistas.mostrarEn(stage, construir());
    }

    private Parent construir() {
        // Panel de marca
        VBox marca = new VBox(10,
                Vistas.texto("Aparta-Suites UQ", "marca-titulo"),
                Vistas.texto("Sistema de Gestión de Alojamiento · Salento, Quindío", "marca-subtitulo"));
        marca.getStyleClass().add("sidebar");
        marca.setPrefWidth(320);
        marca.setPadding(new Insets(40, 28, 40, 28));
        marca.getChildren().get(0).setStyle("-fx-font-size: 26px;");

        // Selector de tipo de usuario
        ToggleGroup grupo = new ToggleGroup();
        ToggleButton empleado = new ToggleButton("Empleado");
        ToggleButton huesped = new ToggleButton("Huésped");
        empleado.setToggleGroup(grupo);
        huesped.setToggleGroup(grupo);
        empleado.getStyleClass().add("filtro");
        huesped.getStyleClass().add("filtro");
        empleado.setSelected(true);

        TextField usuario = new TextField();
        usuario.setPromptText("Id del empleado");
        usuario.getStyleClass().add("campo-busqueda");
        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText("Contraseña");
        contrasena.getStyleClass().add("campo-busqueda");

        grupo.selectedToggleProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) {
                antes.setSelected(true); // siempre debe haber uno elegido
                return;
            }
            boolean esEmpleado = ahora == empleado;
            usuario.setPromptText(esEmpleado ? "Id del empleado" : "Documento de identidad");
            contrasena.setVisible(esEmpleado);
            contrasena.setManaged(esEmpleado);
            mensaje.setText("");
        });

        Button ingresar = new Button("Ingresar");
        ingresar.getStyleClass().add("boton-primario");
        ingresar.setMaxWidth(Double.MAX_VALUE);
        ingresar.setDefaultButton(true);
        ingresar.setOnAction(e -> ingresar(empleado.isSelected(), usuario.getText(), contrasena.getText()));

        mensaje.setStyle("-fx-text-fill: #B3261E;");
        mensaje.setWrapText(true);

        Button cargar = new Button("Cargar escenario de prueba");
        cargar.getStyleClass().add("boton-secundario");
        cargar.setMaxWidth(Double.MAX_VALUE);
        cargar.setOnAction(e -> cargarEjemplo());
        resumen.getStyleClass().add("texto-suave");
        resumen.setWrapText(true);
        actualizarResumen();

        VBox formulario = new VBox(14,
                Vistas.texto("Iniciar sesión", "titulo-pagina"),
                new HBox(8, empleado, huesped),
                usuario, contrasena, ingresar, mensaje,
                new Separator(),
                cargar, resumen);
        formulario.getStyleClass().add("tarjeta");
        formulario.setMaxWidth(380);
        formulario.setPadding(new Insets(28));

        VBox centro = new VBox(formulario);
        centro.setAlignment(Pos.CENTER);
        centro.getStyleClass().add("contenido");
        HBox.setHgrow(centro, Priority.ALWAYS);
        return new HBox(marca, centro);
    }

    private void ingresar(boolean esEmpleado, String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank()) {
            mensaje.setText("Escriba su " + (esEmpleado ? "id." : "documento."));
            return;
        }
        if (esEmpleado) {
            Empleado empleado = sga.autenticar(usuario.trim(), contrasena);
            if (empleado == null) {
                mensaje.setText("Id o contraseña incorrectos.");
                return;
            }
            new VentanaPrincipal(stage, sga, Sesion.deEmpleado(empleado)).mostrar();
        } else {
            Huesped huesped = sga.buscarHuesped(usuario.trim());
            if (huesped == null) {
                mensaje.setText("No hay un huésped registrado con ese documento.");
                return;
            }
            new VentanaPrincipal(stage, sga, Sesion.deHuesped(huesped)).mostrar();
        }
    }

    private void cargarEjemplo() {
        try {
            sga.cargarEscenarioDeEjemplo();
            mensaje.setText("");
            actualizarResumen();
        } catch (Exception ex) {
            Vistas.error("No se pudo cargar el escenario", ex);
        }
    }

    private void actualizarResumen() {
        if (sga.getEmpleados().estaVacio()) {
            resumen.setText("El sistema está vacío. Cargue el escenario de prueba para ingresar.");
        } else {
            resumen.setText("Datos cargados: " + sga.getApartamentos().getTamano() + " apartamentos, "
                    + sga.getHuespedes().getTamano() + " huéspedes, " + sga.getEmpleados().getTamano()
                    + " empleados.\nUsuarios de prueba: admin / admin123 · R1 / r1 · huésped 1094000001");
        }
    }
}
