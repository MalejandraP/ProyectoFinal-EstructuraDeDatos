package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Administrador;
import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Empleado;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Recepcionista;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.RegistroFueraDeServicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.Consumer;

/**
 * Controlador de administracion.fxml (H): gestión jerárquica de apartamentos, huéspedes y
 * empleados sobre los árboles binarios de búsqueda de la fachada, y cambio del estado operativo.
 */
public class AdministracionViewController implements ControladorPantalla {

    // apartamentos
    @FXML private TextField txtBuscarApto;
    @FXML private Label lblArbolAptos;
    @FXML private TableView<Apartamento> tablaApartamentos;
    @FXML private TableColumn<Apartamento, String> colAptoCodigo;
    @FXML private TableColumn<Apartamento, String> colAptoNombre;
    @FXML private TableColumn<Apartamento, String> colAptoBloque;
    @FXML private TableColumn<Apartamento, String> colAptoPiso;
    @FXML private TableColumn<Apartamento, String> colAptoDormitorios;
    @FXML private TableColumn<Apartamento, String> colAptoCapacidad;
    @FXML private TableColumn<Apartamento, String> colAptoEstado;
    @FXML private TableColumn<Apartamento, String> colAptoReservas;
    @FXML private Label lblRecorrido;
    @FXML private TextField txtAptoCodigo;
    @FXML private TextField txtAptoNombre;
    @FXML private TextField txtAptoBloque;
    @FXML private TextField txtAptoPiso;
    @FXML private TextField txtAptoDormitorios;
    @FXML private TextField txtAptoCapacidad;
    @FXML private TextField txtAptoTarifa;
    @FXML private TextArea txtAptoCaracteristicas;

    // huéspedes
    @FXML private TextField txtBuscarHuesped;
    @FXML private Label lblArbolHuespedes;
    @FXML private TableView<Huesped> tablaHuespedes;
    @FXML private TableColumn<Huesped, String> colHuesDocumento;
    @FXML private TableColumn<Huesped, String> colHuesNombre;
    @FXML private TableColumn<Huesped, String> colHuesNacimiento;
    @FXML private TableColumn<Huesped, String> colHuesTelefono;
    @FXML private TableColumn<Huesped, String> colHuesCorreo;
    @FXML private TableColumn<Huesped, String> colHuesReservas;
    @FXML private TextField txtHuesDocumento;
    @FXML private TextField txtHuesNombre;
    @FXML private DatePicker dpHuesNacimiento;
    @FXML private TextField txtHuesTelefono;
    @FXML private TextField txtHuesCorreo;
    @FXML private TextField txtHuesFoto;

    // empleados
    @FXML private TextField txtBuscarEmpleado;
    @FXML private Label lblArbolEmpleados;
    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colEmpId;
    @FXML private TableColumn<Empleado, String> colEmpNombre;
    @FXML private TableColumn<Empleado, String> colEmpRol;
    @FXML private TextField txtEmpId;
    @FXML private TextField txtEmpNombre;
    @FXML private ComboBox<Rol> cbEmpRol;
    @FXML private PasswordField pfEmpContrasena;

    // estado operativo
    @FXML private TableView<Apartamento> tablaEstados;
    @FXML private TableColumn<Apartamento, String> colEstCodigo;
    @FXML private TableColumn<Apartamento, String> colEstNombre;
    @FXML private TableColumn<Apartamento, EstadoApartamento> colEstEstado;
    @FXML private TableColumn<Apartamento, String> colEstMotivo;
    @FXML private TableView<RegistroFueraDeServicio.Registro> tablaHistorial;
    @FXML private TableColumn<RegistroFueraDeServicio.Registro, String> colHistApto;
    @FXML private TableColumn<RegistroFueraDeServicio.Registro, String> colHistMotivo;
    @FXML private TableColumn<RegistroFueraDeServicio.Registro, String> colHistDesde;
    @FXML private TableColumn<RegistroFueraDeServicio.Registro, String> colHistHasta;
    @FXML private Label lblAptoEstado;
    @FXML private TextArea txtMotivo;

    private PrincipalViewController principal;
    private SGA sga;

    @FXML
    private void initialize() {
        colAptoCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigo()));
        colAptoNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colAptoBloque.setCellValueFactory(d -> texto(d.getValue().getBloque()));
        colAptoPiso.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getPiso())));
        colAptoDormitorios.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumeroDormitorios())));
        colAptoCapacidad.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getCapacidadMaxima())));
        colAptoEstado.setCellValueFactory(d -> texto(d.getValue().getEstadoOperativo().getDescripcion()));
        colAptoReservas.setCellValueFactory(d -> texto(String.valueOf(d.getValue().getNumeroReservas())));
        tablaApartamentos.getSelectionModel().selectedItemProperty().addListener((o, a, apto) -> llenarApto(apto));

        colHuesDocumento.setCellValueFactory(d -> texto(d.getValue().getDocumento()));
        colHuesNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colHuesNacimiento.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getFechaNacimiento())));
        colHuesTelefono.setCellValueFactory(d -> texto(d.getValue().getTelefono()));
        colHuesCorreo.setCellValueFactory(d -> texto(d.getValue().getCorreo()));
        colHuesReservas.setCellValueFactory(d -> texto(String.valueOf(sga.reservasDe(d.getValue()).tamanio())));
        tablaHuespedes.getSelectionModel().selectedItemProperty().addListener((o, a, huesped) -> llenarHuesped(huesped));

        colEmpId.setCellValueFactory(d -> texto(d.getValue().getId()));
        colEmpNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colEmpRol.setCellValueFactory(d -> texto(d.getValue().getRol().getDescripcion()));
        tablaEmpleados.getSelectionModel().selectedItemProperty().addListener((o, a, empleado) -> llenarEmpleado(empleado));
        cbEmpRol.getItems().setAll(Rol.RECEPCIONISTA, Rol.ADMINISTRADOR);
        cbEmpRol.getSelectionModel().selectFirst();

        colEstCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigo()));
        colEstNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colEstEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getEstadoOperativo()));
        colEstEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoApartamento estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(estado.getDescripcion());
                chip.getStyleClass().setAll("chip", "chip-" + VistaUtil.claseEstado(estado));
                setGraphic(chip);
            }
        });
        colEstMotivo.setCellValueFactory(d -> texto(d.getValue().estaFueraDeServicio()
                ? sga.motivoFueraDeServicio(d.getValue()) : "—"));
        tablaEstados.getSelectionModel().selectedItemProperty().addListener((o, a, apto) ->
                lblAptoEstado.setText(apto == null ? "Selecciona un apartamento"
                        : apto + " · " + apto.getEstadoOperativo().getDescripcion()));
        colHistApto.setCellValueFactory(d -> texto(d.getValue().getCodigoApartamento()));
        colHistMotivo.setCellValueFactory(d -> texto(d.getValue().getMotivo()));
        colHistDesde.setCellValueFactory(d -> texto(VistaUtil.fecha(d.getValue().getDesde().toLocalDate())
                + " " + VistaUtil.hora(d.getValue().getDesde().toLocalTime())));
        colHistHasta.setCellValueFactory(d -> texto(d.getValue().estaAbierto() ? "Vigente"
                : VistaUtil.fecha(d.getValue().getHasta().toLocalDate()) + " " + VistaUtil.hora(d.getValue().getHasta().toLocalTime())));
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.principal = principal;
        this.sga = principal.getSga();
        refrescarTodo();
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    private static String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private static int entero(TextField campo, String nombre) {
        try {
            return Integer.parseInt(texto(campo));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("«" + nombre + "» debe ser un número entero.");
        }
    }

    private void refrescarTodo() {
        tablaApartamentos.setItems(VistaUtil.lista(sga.getApartamentos()));   // recorrido inorden
        tablaHuespedes.setItems(VistaUtil.lista(sga.getHuespedes()));
        tablaEmpleados.setItems(VistaUtil.lista(sga.getEmpleados()));
        tablaEstados.setItems(VistaUtil.lista(sga.getApartamentos()));
        tablaHistorial.setItems(VistaUtil.lista(sga.historialFueraDeServicio()));
        lblArbolAptos.setText(infoArbol(sga.getApartamentos()));
        lblArbolHuespedes.setText(infoArbol(sga.getHuespedes()));
        lblArbolEmpleados.setText(infoArbol(sga.getEmpleados()));
    }

    private static String infoArbol(ArbolBinarioBusqueda<?> arbol) {
        return "Árbol: " + arbol.getTamano() + " nodos · altura " + arbol.obtenerAltura();
    }

    // ============================================================ apartamentos

    @FXML
    private void onBuscarApto() {
        Apartamento encontrado = sga.buscarApartamento(texto(txtBuscarApto));
        if (encontrado == null) {
            VistaUtil.info("Sin resultados", "No hay un apartamento con código «" + texto(txtBuscarApto) + "».");
            return;
        }
        tablaApartamentos.getSelectionModel().select(encontrado);
        tablaApartamentos.scrollTo(encontrado);
    }

    @FXML
    private void onInorden() {
        mostrarRecorrido("Inorden (ordenado por código)", sga.getApartamentos()::recorridoInorden);
    }

    @FXML
    private void onPreorden() {
        mostrarRecorrido("Preorden (raíz, izquierdo, derecho)", sga.getApartamentos()::recorridoPreorden);
    }

    @FXML
    private void onPostorden() {
        mostrarRecorrido("Postorden (izquierdo, derecho, raíz)", sga.getApartamentos()::recorridoPostorden);
    }

    private void mostrarRecorrido(String titulo, Consumer<Consumer<Apartamento>> recorrido) {
        StringBuilder codigos = new StringBuilder();
        recorrido.accept(apto -> codigos.append(codigos.length() == 0 ? "" : " · ").append(apto.getCodigo()));
        lblRecorrido.setText(titulo + ":\n" + codigos);
    }

    private void llenarApto(Apartamento apto) {
        if (apto == null) {
            return;
        }
        txtAptoCodigo.setText(apto.getCodigo());
        txtAptoCodigo.setDisable(true);   // el código es la clave del árbol
        txtAptoNombre.setText(apto.getNombre());
        txtAptoBloque.setText(apto.getBloque());
        txtAptoPiso.setText(String.valueOf(apto.getPiso()));
        txtAptoDormitorios.setText(String.valueOf(apto.getNumeroDormitorios()));
        txtAptoCapacidad.setText(String.valueOf(apto.getCapacidadMaxima()));
        txtAptoTarifa.setText(String.format("%.0f", apto.getTarifaBaseNoche()));
        txtAptoTarifa.setDisable(true);
        txtAptoCaracteristicas.setText(apto.getCaracteristicas());
    }

    @FXML
    private void onNuevoApto() {
        tablaApartamentos.getSelectionModel().clearSelection();
        for (TextField campo : new TextField[]{txtAptoCodigo, txtAptoNombre, txtAptoBloque, txtAptoPiso,
                txtAptoDormitorios, txtAptoCapacidad, txtAptoTarifa}) {
            campo.clear();
            campo.setDisable(false);
        }
        txtAptoCaracteristicas.clear();
    }

    @FXML
    private void onGuardarApto() {
        try {
            String codigo = texto(txtAptoCodigo);
            if (sga.buscarApartamento(codigo) == null) {
                double tarifa;
                try {
                    tarifa = Double.parseDouble(texto(txtAptoTarifa));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("La tarifa base debe ser un número.");
                }
                sga.registrarApartamento(new Apartamento(codigo, texto(txtAptoNombre), texto(txtAptoBloque),
                        entero(txtAptoPiso, "Piso"), entero(txtAptoDormitorios, "Dormitorios"),
                        entero(txtAptoCapacidad, "Capacidad"), tarifa, txtAptoCaracteristicas.getText()));
                VistaUtil.info("Apartamento registrado", codigo + " se insertó en el árbol del catálogo.");
            } else {
                sga.modificarApartamento(codigo, texto(txtAptoNombre), texto(txtAptoBloque), entero(txtAptoPiso, "Piso"),
                        entero(txtAptoDormitorios, "Dormitorios"), entero(txtAptoCapacidad, "Capacidad"),
                        txtAptoCaracteristicas.getText());
                VistaUtil.info("Apartamento actualizado", codigo + " quedó con los nuevos datos.");
            }
            refrescarTodo();
            tablaApartamentos.getSelectionModel().select(sga.buscarApartamento(codigo));
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo guardar el apartamento", ex);
        }
    }

    @FXML
    private void onEliminarApto() {
        Apartamento apto = tablaApartamentos.getSelectionModel().getSelectedItem();
        if (apto == null || !VistaUtil.confirmar("Eliminar " + apto.getCodigo(),
                "Se eliminará del árbol del catálogo. ¿Continuar?")) {
            return;
        }
        try {
            sga.eliminarApartamento(apto.getCodigo());
            onNuevoApto();
            refrescarTodo();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo eliminar", ex);
        }
    }

    // =============================================================== huéspedes

    @FXML
    private void onBuscarHuesped() {
        Huesped encontrado = sga.buscarHuesped(texto(txtBuscarHuesped));
        if (encontrado == null) {
            VistaUtil.info("Sin resultados", "No hay un huésped con documento «" + texto(txtBuscarHuesped) + "».");
            return;
        }
        tablaHuespedes.getSelectionModel().select(encontrado);
        tablaHuespedes.scrollTo(encontrado);
    }

    private void llenarHuesped(Huesped huesped) {
        if (huesped == null) {
            return;
        }
        txtHuesDocumento.setText(huesped.getDocumento());
        txtHuesDocumento.setDisable(true);   // el documento es la clave del árbol
        txtHuesNombre.setText(huesped.getNombre());
        dpHuesNacimiento.setValue(huesped.getFechaNacimiento());
        txtHuesTelefono.setText(huesped.getTelefono());
        txtHuesCorreo.setText(huesped.getCorreo());
        txtHuesFoto.setText(huesped.getRutaFoto() == null ? "" : huesped.getRutaFoto());
    }

    @FXML
    private void onNuevoHuesped() {
        tablaHuespedes.getSelectionModel().clearSelection();
        for (TextField campo : new TextField[]{txtHuesDocumento, txtHuesNombre, txtHuesTelefono, txtHuesCorreo, txtHuesFoto}) {
            campo.clear();
        }
        txtHuesDocumento.setDisable(false);
        dpHuesNacimiento.setValue(null);
    }

    @FXML
    private void onElegirFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Fotografía del huésped");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(txtHuesFoto.getScene().getWindow());
        if (archivo != null) {
            txtHuesFoto.setText(archivo.getAbsolutePath());
        }
    }

    @FXML
    private void onGuardarHuesped() {
        try {
            String documento = texto(txtHuesDocumento);
            if (sga.buscarHuesped(documento) == null) {
                sga.registrarHuesped(new Huesped(documento, texto(txtHuesNombre), dpHuesNacimiento.getValue(),
                        texto(txtHuesTelefono), texto(txtHuesCorreo), texto(txtHuesFoto)));
                VistaUtil.info("Huésped registrado", documento + " se insertó en el árbol de huéspedes.");
            } else {
                sga.modificarHuesped(documento, texto(txtHuesNombre), dpHuesNacimiento.getValue(),
                        texto(txtHuesTelefono), texto(txtHuesCorreo), texto(txtHuesFoto));
                VistaUtil.info("Huésped actualizado", "El perfil de " + documento + " quedó actualizado.");
            }
            refrescarTodo();
            tablaHuespedes.getSelectionModel().select(sga.buscarHuesped(documento));
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo guardar el huésped", ex);
        }
    }

    @FXML
    private void onEliminarHuesped() {
        Huesped huesped = tablaHuespedes.getSelectionModel().getSelectedItem();
        if (huesped == null || !VistaUtil.confirmar("Eliminar " + huesped.getNombre(),
                "Se eliminará del árbol de huéspedes. ¿Continuar?")) {
            return;
        }
        try {
            sga.eliminarHuesped(huesped.getDocumento());
            onNuevoHuesped();
            refrescarTodo();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo eliminar", ex);
        }
    }

    // =============================================================== empleados

    @FXML
    private void onBuscarEmpleado() {
        Empleado encontrado = sga.buscarEmpleado(texto(txtBuscarEmpleado));
        if (encontrado == null) {
            VistaUtil.info("Sin resultados", "No hay un empleado con id «" + texto(txtBuscarEmpleado) + "».");
            return;
        }
        tablaEmpleados.getSelectionModel().select(encontrado);
    }

    private void llenarEmpleado(Empleado empleado) {
        if (empleado == null) {
            return;
        }
        txtEmpId.setText(empleado.getId());
        txtEmpId.setDisable(true);
        txtEmpNombre.setText(empleado.getNombre());
        cbEmpRol.setValue(empleado.getRol());
        cbEmpRol.setDisable(true);   // el rol define la clase (Recepcionista o Administrador)
        pfEmpContrasena.clear();
    }

    @FXML
    private void onNuevoEmpleado() {
        tablaEmpleados.getSelectionModel().clearSelection();
        txtEmpId.clear();
        txtEmpId.setDisable(false);
        txtEmpNombre.clear();
        pfEmpContrasena.clear();
        cbEmpRol.setDisable(false);
        cbEmpRol.getSelectionModel().selectFirst();
    }

    @FXML
    private void onGuardarEmpleado() {
        try {
            String id = texto(txtEmpId);
            if (sga.buscarEmpleado(id) == null) {
                Empleado nuevo = cbEmpRol.getValue() == Rol.ADMINISTRADOR
                        ? new Administrador(texto(txtEmpNombre), id, pfEmpContrasena.getText())
                        : new Recepcionista(texto(txtEmpNombre), id, pfEmpContrasena.getText());
                sga.registrarEmpleado(nuevo);
                VistaUtil.info("Empleado registrado", nuevo + " se insertó en el árbol de empleados.");
            } else {
                sga.modificarEmpleado(id, texto(txtEmpNombre), pfEmpContrasena.getText());
                VistaUtil.info("Empleado actualizado", "Los datos de " + id + " quedaron actualizados.");
            }
            refrescarTodo();
            principal.refrescarTurno();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo guardar el empleado", ex);
        }
    }

    @FXML
    private void onEliminarEmpleado() {
        Empleado empleado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleado == null) {
            return;
        }
        if (empleado.equals(principal.getSesion().getEmpleado())) {
            VistaUtil.info("No permitido", "No puedes eliminar el usuario con el que iniciaste sesión.");
            return;
        }
        if (VistaUtil.confirmar("Eliminar " + empleado.getNombre(), "Se eliminará del árbol de empleados"
                + (empleado instanceof Recepcionista ? " y de la rotación de turnos" : "") + ". ¿Continuar?")) {
            sga.eliminarEmpleado(empleado.getId());
            onNuevoEmpleado();
            refrescarTodo();
            principal.refrescarTurno();
        }
    }

    // ========================================================= estado operativo

    @FXML
    private void onFueraDeServicio() {
        Apartamento apto = tablaEstados.getSelectionModel().getSelectedItem();
        if (apto == null) {
            VistaUtil.info("Elige un apartamento", "Selecciona un apartamento de la tabla.");
            return;
        }
        try {
            sga.ponerFueraDeServicio(apto.getCodigo(), txtMotivo.getText());
            txtMotivo.clear();
            refrescarTodo();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo poner fuera de servicio", ex);
        }
    }

    @FXML
    private void onVolverAServicio() {
        Apartamento apto = tablaEstados.getSelectionModel().getSelectedItem();
        if (apto == null) {
            VistaUtil.info("Elige un apartamento", "Selecciona un apartamento de la tabla.");
            return;
        }
        try {
            sga.volverAServicio(apto.getCodigo());
            refrescarTodo();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo volver a servicio", ex);
        }
    }
}
