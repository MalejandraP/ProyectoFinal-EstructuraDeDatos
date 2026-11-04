module co.edu.uniquindio.apartasuitesuq {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.apartasuitesuq to javafx.fxml;
    exports co.edu.uniquindio.apartasuitesuq;
}