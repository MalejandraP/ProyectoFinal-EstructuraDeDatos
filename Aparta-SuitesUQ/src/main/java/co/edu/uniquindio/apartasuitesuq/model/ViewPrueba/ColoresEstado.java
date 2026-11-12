package co.edu.uniquindio.apartasuitesuq.model.ViewPrueba;

import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import javafx.scene.paint.Color;

public class ColoresEstado {
    private ColoresEstado() {}

    public static Color de(EstadoApartamento e) {
        switch (e) {
            case PREPARADO: return Color.web("#2e9e5b");
            case OCUPADO: return Color.web("#2f6fd0");
            case PENDIENTEDEPREPARACION:
            case ENPREPARACION: return Color.web("#e0a21a");
            default: return Color.web("#d03a3a");
        }
    }
}
