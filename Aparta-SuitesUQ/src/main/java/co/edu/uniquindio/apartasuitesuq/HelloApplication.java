package co.edu.uniquindio.apartasuitesuq;

import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.view.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada del SGA. Abre la pantalla de ingreso; desde allí cada rol entra a la
 * ventana principal con su menú. Todas las pantallas comparten la misma fachada {@link SGA}.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setMinWidth(960);
        stage.setMinHeight(620);
        new LoginView(stage, SGA.getInstancia()).mostrar();
    }

    public static void main(String[] args) {
        launch();
    }
}
