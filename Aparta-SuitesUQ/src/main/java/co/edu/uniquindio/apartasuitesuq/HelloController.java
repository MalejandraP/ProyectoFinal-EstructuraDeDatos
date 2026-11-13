package co.edu.uniquindio.apartasuitesuq;

import co.edu.uniquindio.apartasuitesuq.controller.DatosPrueba;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.viewController.PanelUsuarioViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class HelloController {

    @FXML
    private void onAbrirPanelUsuario() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("panel-usuario.fxml"));
        Stage ventana = crearVentana(loader, "Panel de usuario", 900, 700);

        // Se obtiene el controlador DESPUÉS de crear la ventana (que hace el load)
        PanelUsuarioViewController controlador = loader.getController();
        controlador.setHuesped(DatosPrueba.getHuesped());
        controlador.setApartamentos(DatosPrueba.getApartamentos());
        controlador.setReservas(DatosPrueba.getReservas());
        ventana.show();
    }

    @FXML
    private void onAbrirFolio() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("folio.fxml"));
        Stage ventana = crearVentana(loader, "Folio", 1200, 700);
        ventana.show();
    }

    // Carga el FXML, le aplica el CSS y devuelve la ventana lista
    private Stage crearVentana(FXMLLoader loader, String titulo, int ancho, int alto)
            throws IOException {
        Scene escena = new Scene(loader.load(), ancho, alto);
        escena.getStylesheets().add(
                HelloApplication.class.getResource("estilos.css").toExternalForm());
        Stage ventana = new Stage();
        ventana.setTitle(titulo);
        ventana.setScene(escena);
        return ventana;
    }
}