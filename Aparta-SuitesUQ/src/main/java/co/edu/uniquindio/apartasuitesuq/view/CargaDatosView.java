package co.edu.uniquindio.apartasuitesuq.view;

import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Carga de un escenario de prueba (apartamentos, mapa, huéspedes, empleados, temporadas,
 * reservas y solicitudes externas) desde el archivo incluido o desde un archivo plano elegido.
 * Si el archivo tiene un error, el sistema queda como estaba y se informa la línea.
 */
public class CargaDatosView {

    private static final String FORMATO = String.join("\n",
            "Formato: secciones [NOMBRE] con un registro por línea y campos separados por «;».",
            "Las líneas que empiezan con # son comentarios. Las fechas pueden ser AAAA-MM-DD o relativas: HOY, HOY+3, HOY-2.",
            "",
            "[TEMPORADAS]   temporada;inicio;fin",
            "[APARTAMENTOS] codigo;nombre;bloque;piso;dormitorios;capacidad;tarifaBaja;tarifaMedia;tarifaAlta;caracteristicas",
            "[MAPA]         NODO;codigo;tipo;x;y   |   PASILLO;nodoA;nodoB;metros",
            "[EMPLEADOS]    rol;id;nombre;contrasena",
            "[HUESPEDES]    documento;nombre;fechaNacimiento;telefono;correo;rutaFoto",
            "[RESERVAS]     codigo;titular;apartamento;entrada;salida;canal;estado;personas;creacion;horaLlegada",
            "[OCUPANTES]    codigoReserva;nombre;fechaNacimiento",
            "[PAGOS]        codigoReserva;monto|ANTICIPO|TOTAL;descripcion",
            "[ESTADOS]      apartamento;estado;motivo",
            "[SOLICITUDES_EXTERNAS] idExterno;apartamento;entrada;salida;personas;documentoTitular");

    private final SGA sga;
    private final Runnable alCargar;
    private final Label resumen = Vistas.texto("", null);
    private final Node vista;

    /** @param alCargar acción a ejecutar después de una carga exitosa (por ejemplo, refrescar la ventana) */
    public CargaDatosView(SGA sga, Runnable alCargar) {
        this.sga = sga;
        this.alCargar = alCargar;
        this.vista = construir();
    }

    public Node getVista() {
        return vista;
    }

    private Node construir() {
        Button ejemplo = new Button("Cargar escenario de prueba");
        ejemplo.getStyleClass().add("boton-primario");
        ejemplo.setOnAction(e -> cargar(null));

        Button archivo = new Button("Cargar desde archivo…");
        archivo.getStyleClass().add("boton-secundario");
        archivo.setOnAction(e -> {
            FileChooser selector = new FileChooser();
            selector.setTitle("Escenario de prueba");
            selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo plano", "*.txt"));
            File elegido = selector.showOpenDialog(archivo.getScene().getWindow());
            if (elegido != null) {
                cargar(elegido);
            }
        });

        Label formato = Vistas.texto(FORMATO, "caja-info");
        formato.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace; -fx-font-size: 12px;");
        actualizarResumen();
        return Vistas.pagina("Carga de datos", "Reemplaza todos los datos del sistema por el escenario elegido.",
                new HBox(10, ejemplo, archivo),
                Vistas.tarjeta("Datos actuales", resumen),
                Vistas.tarjeta("Formato del archivo", formato));
    }

    private void cargar(File archivo) {
        try {
            if (archivo == null) {
                sga.cargarEscenarioDeEjemplo();
            } else {
                sga.cargarEscenario(archivo.toPath());
            }
            actualizarResumen();
            Vistas.info("Escenario cargado", resumen.getText());
            alCargar.run();
        } catch (Exception ex) {
            Vistas.error("No se pudo cargar el escenario", ex);
        }
    }

    private void actualizarResumen() {
        int periodos = 0;
        for (Object ignored : sga.getCalendario().getPeriodos()) {
            periodos++;
        }
        int reservas = 0;
        for (Object ignored : sga.getReservas()) {
            reservas++;
        }
        resumen.setText(sga.getApartamentos().getTamano() + " apartamentos · "
                + sga.getMapa().getGrafo().tamano() + " nodos en el mapa · "
                + sga.getHuespedes().getTamano() + " huéspedes · "
                + sga.getEmpleados().getTamano() + " empleados · "
                + periodos + " periodos de temporada · "
                + reservas + " reservas · "
                + sga.getSolicitudesExternasPendientes() + " solicitudes externas en cola");
    }
}
