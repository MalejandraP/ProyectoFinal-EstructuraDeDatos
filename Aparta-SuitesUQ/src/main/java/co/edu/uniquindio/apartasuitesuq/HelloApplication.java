package co.edu.uniquindio.apartasuitesuq;

import co.edu.uniquindio.apartasuitesuq.controller.DatosPrueba;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatosPrueba.cargar();
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 350);
        scene.getStylesheets().add(
                HelloApplication.class.getResource("estilos.css").toExternalForm());
        stage.setTitle("Aparta-Suites UQ");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}