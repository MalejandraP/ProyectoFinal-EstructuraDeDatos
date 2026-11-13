module co.edu.uniquindio.apartasuitesuq {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.apartasuitesuq to javafx.fxml;
    exports co.edu.uniquindio.apartasuitesuq;
    exports co.edu.uniquindio.apartasuitesuq.model;
    exports co.edu.uniquindio.apartasuitesuq.model.Servicios;
    exports co.edu.uniquindio.apartasuitesuq.model.Estructuras;
    opens co.edu.uniquindio.apartasuitesuq.model to javafx.fxml;
}