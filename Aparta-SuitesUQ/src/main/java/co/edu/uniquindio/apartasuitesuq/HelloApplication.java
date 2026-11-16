package co.edu.uniquindio.apartasuitesuq;

import co.edu.uniquindio.apartasuitesuq.viewController.LoginViewController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * Punto de entrada del SGA. Abre login.fxml; desde allí cada rol entra a principal.fxml con su
 * menú. Todas las pantallas comparten la misma fachada (ver {@code controller.DatosPrueba}).
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        Locale.setDefault(Locale.forLanguageTag("es-CO")); // fechas y textos de los controles en español
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        LoginViewController.mostrar(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
