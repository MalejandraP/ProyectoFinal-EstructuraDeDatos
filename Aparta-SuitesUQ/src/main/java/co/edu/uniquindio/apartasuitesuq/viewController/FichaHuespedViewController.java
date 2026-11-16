package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

/** Controlador de ficha-huesped.fxml: perfil del huésped (foto opcional) y la reserva relacionada. */
public class FichaHuespedViewController {

    @FXML private Label lblIniciales;
    @FXML private ImageView imgFoto;
    @FXML private Label lblNombre;
    @FXML private Label lblDocumento;
    @FXML private Label lblNacimiento;
    @FXML private Label lblTelefono;
    @FXML private Label lblCorreo;
    @FXML private Label lblFavoritos;
    @FXML private Label lblReserva;

    void mostrar(Huesped huesped, Reserva reserva) {
        lblNombre.setText(huesped.getNombre());
        lblIniciales.setText(iniciales(huesped.getNombre()));
        lblDocumento.setText(huesped.getDocumento());
        lblNacimiento.setText(huesped.getFechaNacimiento() == null ? "—"
                : VistaUtil.fecha(huesped.getFechaNacimiento()) + " (" + huesped.getEdad() + " años)");
        lblTelefono.setText(huesped.getTelefono());
        lblCorreo.setText(huesped.getCorreo());

        StringBuilder favoritos = new StringBuilder();
        for (Apartamento favorito : huesped.getFavoritos()) { // Conjunto: sin repetidos
            if (favoritos.length() > 0) {
                favoritos.append(", ");
            }
            favoritos.append(favorito.getCodigo());
        }
        lblFavoritos.setText(favoritos.length() == 0 ? "Sin favoritos" : favoritos.toString());

        // Fotografía opcional: si no hay archivo se muestran las iniciales
        boolean conFoto = false;
        if (huesped.tieneFoto()) {
            File archivo = new File(huesped.getRutaFoto());
            if (archivo.isFile()) {
                imgFoto.setImage(new Image(archivo.toURI().toString(), true));
                conFoto = true;
            }
        }
        imgFoto.setVisible(conFoto);
        lblIniciales.setVisible(!conFoto);

        lblReserva.setText(reserva == null ? "—" : reserva.getCodigoReserva() + " · " + reserva.getApartamento().getCodigo()
                + " · " + VistaUtil.fecha(reserva.getFechaInicio()) + " → " + VistaUtil.fecha(reserva.getFechaFin())
                + " · " + reserva.getEstado().getDescripcion() + " · " + reserva.getNumeroPersonas() + " personas");
    }

    private static String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        String resultado = partes[0].substring(0, 1);
        if (partes.length > 1) {
            resultado += partes[1].substring(0, 1);
        }
        return resultado.toUpperCase();
    }
}
