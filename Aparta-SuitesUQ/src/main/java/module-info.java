module co.edu.uniquindio.apartasuitesuq {
    requires javafx.controls;
    requires javafx.fxml;

    exports co.edu.uniquindio.apartasuitesuq;
    opens co.edu.uniquindio.apartasuitesuq to javafx.fxml;

    // Controladores de vista: el FXMLLoader los crea e inyecta los campos @FXML
    exports co.edu.uniquindio.apartasuitesuq.viewController;
    opens co.edu.uniquindio.apartasuitesuq.viewController to javafx.fxml;
    exports co.edu.uniquindio.apartasuitesuq.controller;

    // Modelo: las tablas de JavaFX (javafx.base) leen sus objetos
    exports co.edu.uniquindio.apartasuitesuq.model;
    opens co.edu.uniquindio.apartasuitesuq.model to javafx.fxml, javafx.base;
    exports co.edu.uniquindio.apartasuitesuq.model.Estructuras;
    opens co.edu.uniquindio.apartasuitesuq.model.Estructuras to javafx.fxml, javafx.base;
    exports co.edu.uniquindio.apartasuitesuq.model.Servicios;
    opens co.edu.uniquindio.apartasuitesuq.model.Servicios to javafx.fxml, javafx.base;
    exports co.edu.uniquindio.apartasuitesuq.model.Excepciones;
    opens co.edu.uniquindio.apartasuitesuq.model.Excepciones to javafx.base, javafx.fxml;
}
