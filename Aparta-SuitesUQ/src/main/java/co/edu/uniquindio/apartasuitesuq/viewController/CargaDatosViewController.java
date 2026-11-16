package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.controller.DatosPrueba;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/** Controlador de carga.fxml (J): carga el escenario incluido o un archivo plano elegido. */
public class CargaDatosViewController implements ControladorPantalla {

    private static final String FORMATO = String.join("\n",
            "# Secciones [NOMBRE]; un registro por línea; campos separados por ';'. '#' = comentario.",
            "# Fechas AAAA-MM-DD o relativas: HOY, HOY+3, HOY-2. Fecha y hora: HOY-1 09:30",
            "[TEMPORADAS]   temporada;inicio;fin",
            "[APARTAMENTOS] codigo;nombre;bloque;piso;dormitorios;capacidad;tarifaBaja;tarifaMedia;tarifaAlta;caracteristicas",
            "[MAPA]         NODO;codigo;tipo;x;y        (tipo: RECEPCION, ESCALERA, ZONA_ASEO, APARTAMENTO)",
            "               PASILLO;nodoA;nodoB;metros",
            "[EMPLEADOS]    rol;id;nombre;contrasena    (rol: RECEPCIONISTA o ADMINISTRADOR)",
            "[HUESPEDES]    documento;nombre;fechaNacimiento;telefono;correo;rutaFoto(opcional)",
            "[RESERVAS]     codigo;titular;apartamento;entrada;salida;canal;estado;personas;creacion;horaLlegada",
            "[OCUPANTES]    codigoReserva;nombre;fechaNacimiento",
            "[PAGOS]        codigoReserva;monto|ANTICIPO|TOTAL;descripcion",
            "[ESTADOS]      apartamento;estado;motivo",
            "[SOLICITUDES_EXTERNAS] idExterno;apartamento;entrada;salida;personas;documentoTitular");

    @FXML private Label lblResultado;
    @FXML private Label lblResumen;
    @FXML private TextArea txtFormato;

    private PrincipalViewController principal;

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.principal = principal;
        txtFormato.setText(FORMATO);
        lblResultado.setText("Sin cargas en esta sesión.");
        lblResumen.setText(DatosPrueba.resumen());
    }

    @FXML
    private void onCargarEjemplo() {
        try {
            DatosPrueba.cargarEscenario();
            terminarCarga("Escenario de prueba cargado.");
        } catch (IOException | RuntimeException ex) {
            fallo(ex);
        }
    }

    @FXML
    private void onCargarArchivo() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Escenario de prueba");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo plano", "*.txt"));
        File archivo = selector.showOpenDialog(lblResultado.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            DatosPrueba.cargarEscenario(archivo.toPath());
            terminarCarga("Escenario «" + archivo.getName() + "» cargado.");
        } catch (IOException | RuntimeException ex) {
            fallo(ex);
        }
    }

    private void terminarCarga(String mensaje) {
        lblResultado.setText(mensaje + "\n" + DatosPrueba.resumen());
        lblResumen.setText(DatosPrueba.resumen());
        VistaUtil.info("Carga terminada", mensaje);
        principal.alCargarDatos();
    }

    private void fallo(Exception ex) {
        lblResultado.setText("No se cargó nada (los datos siguen como estaban):\n" + ex.getMessage());
        VistaUtil.error("No se pudo cargar el escenario", ex);
    }
}
