module co.edu.uniquindio.apartasuitesuq {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.apartasuitesuq to javafx.fxml;
    exports co.edu.uniquindio.apartasuitesuq;

    // NUEVO: permite que el FXMLLoader cree FolioViewController
    opens co.edu.uniquindio.apartasuitesuq.viewController to javafx.fxml;

    exports co.edu.uniquindio.apartasuitesuq.model;
    // CAMBIO: se agrega javafx.base para que la tabla pueda leer los Movimiento
    opens co.edu.uniquindio.apartasuitesuq.model to javafx.fxml, javafx.base;

    exports co.edu.uniquindio.apartasuitesuq.model.Estructuras;
    opens co.edu.uniquindio.apartasuitesuq.model.Estructuras to javafx.fxml;
    exports co.edu.uniquindio.apartasuitesuq.model.Servicios;
    opens co.edu.uniquindio.apartasuitesuq.model.Servicios to javafx.fxml;
    exports co.edu.uniquindio.apartasuitesuq.model.Excepciones;
    opens co.edu.uniquindio.apartasuitesuq.model.Excepciones to javafx.base, javafx.fxml;
}