package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.controller.DatosPrueba;
import co.edu.uniquindio.apartasuitesuq.controller.Sesion;
import co.edu.uniquindio.apartasuitesuq.model.Empleado;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/** Controlador de login.fxml: autentica al usuario y abre la ventana principal de su rol. */
public class LoginViewController {

    @FXML private ToggleGroup grupoTipo;
    @FXML private ToggleButton tgEmpleado;
    @FXML private ToggleButton tgHuesped;
    @FXML private Label lblUsuario;
    @FXML private TextField txtUsuario;
    @FXML private VBox cajaContrasena;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblError;
    @FXML private Label lblResumen;

    /** Carga login.fxml en la ventana. */
    public static void mostrar(Stage stage) {
        try {
            FXMLLoader cargador = VistaUtil.cargador("login.fxml");
            Parent raiz = cargador.load();
            stage.setTitle("SGA – Aparta-Suites UQ");
            VistaUtil.mostrarEn(stage, raiz);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir el ingreso: " + e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        grupoTipo.selectedToggleProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) {          // siempre debe quedar un tipo elegido
                antes.setSelected(true);
                return;
            }
            boolean esEmpleado = ahora == tgEmpleado;
            lblUsuario.setText(esEmpleado ? "Id del empleado" : "Documento de identidad");
            txtUsuario.setPromptText(esEmpleado ? "Ej.: admin o R1" : "Ej.: 1094000001");
            cajaContrasena.setVisible(esEmpleado);
            cajaContrasena.setManaged(esEmpleado);
            lblError.setText("");
        });
        actualizarResumen();
    }

    @FXML
    private void onIngresar() {
        String usuario = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        if (usuario.isEmpty()) {
            lblError.setText("Escribe tu " + (tgEmpleado.isSelected() ? "id." : "documento."));
            return;
        }
        Sesion sesion;
        if (tgEmpleado.isSelected()) {
            Empleado empleado = DatosPrueba.sga().autenticar(usuario, txtContrasena.getText());
            if (empleado == null) {
                lblError.setText("Id o contraseña incorrectos.");
                return;
            }
            sesion = Sesion.deEmpleado(empleado);
        } else {
            Huesped huesped = DatosPrueba.sga().buscarHuesped(usuario);
            if (huesped == null) {
                lblError.setText("No hay un huésped registrado con ese documento.");
                return;
            }
            sesion = Sesion.deHuesped(huesped);
        }
        PrincipalViewController.abrir((Stage) txtUsuario.getScene().getWindow(), sesion);
    }

    @FXML
    private void onCargarEscenario() {
        try {
            DatosPrueba.cargarEscenario();
            lblError.setText("");
            actualizarResumen();
        } catch (IOException | RuntimeException ex) {
            VistaUtil.error("No se pudo cargar el escenario", ex);
        }
    }

    private void actualizarResumen() {
        if (!DatosPrueba.hayDatos()) {
            lblResumen.setText("El sistema está vacío: carga el escenario de prueba para ingresar.");
        } else {
            lblResumen.setText("Datos cargados: " + DatosPrueba.resumen()
                    + ".\nUsuarios de prueba: admin / admin123 · R1 / r1 · huésped 1094000001");
        }
    }
}
