package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.controller.DatosPrueba;
import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

/**
 * Controlador de inicio.fxml (A. Inicio): resumen del día, filtros por estado/bloque/piso,
 * búsqueda por código (árbol de apartamentos) y por huésped, y una tarjeta por apartamento
 * con sus acciones. Toda la información sale de la fachada; aquí solo se presenta.
 */
public class InicioViewController implements ControladorPantalla {

    private static final String TODOS = "Todos";

    @FXML private Label lblFecha;
    @FXML private Button btnCargarDatos;
    @FXML private Label lblOcupacion;
    @FXML private Label lblOcupacionDetalle;
    @FXML private Label lblCheckins;
    @FXML private Label lblCheckinsDetalle;
    @FXML private Label lblPreparados;
    @FXML private Label lblOcupados;
    @FXML private Label lblPendientes;
    @FXML private Label lblFuera;
    @FXML private ToggleGroup grupoEstado;
    @FXML private ToggleButton tgTodos;
    @FXML private ToggleButton tgPreparado;
    @FXML private ToggleButton tgOcupado;
    @FXML private ToggleButton tgPendiente;
    @FXML private ToggleButton tgFuera;
    @FXML private ComboBox<String> cbBloque;
    @FXML private ComboBox<String> cbPiso;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtHuesped;
    @FXML private Label lblResultado;
    @FXML private Label lblVacio;
    @FXML private FlowPane panelTarjetas;

    private PrincipalViewController principal;
    private SGA sga;
    private boolean ignorarEventos;

    @FXML
    private void initialize() {
        grupoEstado.selectedToggleProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) {
                antes.setSelected(true); // siempre hay un filtro de estado elegido
            } else if (!ignorarEventos && sga != null) {
                actualizar();
            }
        });
    }

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.principal = principal;
        this.sga = principal.getSga();
        boolean esAdmin = principal.getSesion().getRol() == Rol.ADMINISTRADOR;
        btnCargarDatos.setVisible(esAdmin);
        btnCargarDatos.setManaged(esAdmin);
        lblFecha.setText(VistaUtil.fechaLarga(sga.hoy()));
        llenarCombos();
        actualizar();
    }

    // ================================================================ datos

    private void llenarCombos() {
        ignorarEventos = true;
        cbBloque.getItems().setAll(TODOS);
        for (String bloque : sga.bloques()) {
            cbBloque.getItems().add(bloque);
        }
        cbPiso.getItems().setAll(TODOS);
        for (Integer piso : sga.pisos()) {
            cbPiso.getItems().add(String.valueOf(piso));
        }
        cbBloque.setValue(TODOS);
        cbPiso.setValue(TODOS);
        ignorarEventos = false;
    }

    /** Vuelve a pedir los datos a la fachada y redibuja resumen y tarjetas con los filtros actuales. */
    private void actualizar() {
        mostrarResumen();
        String bloque = TODOS.equals(cbBloque.getValue()) ? null : cbBloque.getValue();
        Integer piso = cbPiso.getValue() == null || TODOS.equals(cbPiso.getValue()) ? null
                : Integer.valueOf(cbPiso.getValue());
        mostrar(sga.filtrarApartamentos(estadosElegidos(), bloque, piso), "filtros aplicados");
    }

    private void mostrarResumen() {
        int[] conteo = sga.contarPorEstado();
        int total = sga.getApartamentos().getTamano();
        lblOcupacion.setText(VistaUtil.porcentaje(sga.ocupacionActual()));
        lblOcupacionDetalle.setText(conteo[EstadoApartamento.OCUPADO.ordinal()] + " de " + total + " apartamentos");
        lblPreparados.setText(String.valueOf(conteo[EstadoApartamento.PREPARADO.ordinal()]));
        lblOcupados.setText(String.valueOf(conteo[EstadoApartamento.OCUPADO.ordinal()]));
        lblPendientes.setText(String.valueOf(conteo[EstadoApartamento.PENDIENTEDEPREPARACION.ordinal()]
                + conteo[EstadoApartamento.ENPREPARACION.ordinal()]));
        lblFuera.setText(String.valueOf(conteo[EstadoApartamento.FUERADESERVICIO.ordinal()]));

        int entradas = 0;
        StringBuilder detalle = new StringBuilder();
        for (Reserva reserva : sga.entradasDeHoy()) {
            entradas++;
            if (detalle.length() > 0) {
                detalle.append(" · ");
            }
            detalle.append(reserva.getApartamento().getCodigo()).append(' ')
                    .append(VistaUtil.hora(reserva.getHoraEstimadaLlegada()));
        }
        lblCheckins.setText(String.valueOf(entradas));
        lblCheckinsDetalle.setText(entradas == 0 ? "Sin llegadas confirmadas hoy" : detalle.toString());
    }

    private EstadoApartamento[] estadosElegidos() {
        if (tgPreparado.isSelected()) {
            return new EstadoApartamento[]{EstadoApartamento.PREPARADO};
        }
        if (tgOcupado.isSelected()) {
            return new EstadoApartamento[]{EstadoApartamento.OCUPADO};
        }
        if (tgPendiente.isSelected()) {
            return new EstadoApartamento[]{EstadoApartamento.PENDIENTEDEPREPARACION, EstadoApartamento.ENPREPARACION};
        }
        if (tgFuera.isSelected()) {
            return new EstadoApartamento[]{EstadoApartamento.FUERADESERVICIO};
        }
        return null;
    }

    /** Dibuja una tarjeta (tarjeta-apartamento.fxml) por cada apartamento de la estructura. */
    private void mostrar(Iterable<Apartamento> apartamentos, String descripcion) {
        panelTarjetas.getChildren().clear();
        int cantidad = 0;
        for (Apartamento apartamento : apartamentos) {
            try {
                FXMLLoader cargador = VistaUtil.cargador("tarjeta-apartamento.fxml");
                Parent tarjeta = cargador.load();
                TarjetaApartamentoViewController controlador = cargador.getController();
                controlador.mostrar(apartamento, this);
                panelTarjetas.getChildren().add(tarjeta);
                cantidad++;
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo cargar la tarjeta: " + e.getMessage(), e);
            }
        }
        lblResultado.setText("Mostrando " + cantidad + (cantidad == 1 ? " apartamento" : " apartamentos")
                + " · " + descripcion);
        lblVacio.setVisible(cantidad == 0);
        lblVacio.setManaged(cantidad == 0);
    }

    // ============================================================== eventos

    @FXML
    private void onFiltrar() {
        if (!ignorarEventos && sga != null) {
            actualizar();
        }
    }

    @FXML
    private void onActualizar() {
        llenarCombos();
        actualizar();
    }

    /** Búsqueda recursiva en el árbol binario de apartamentos por código. */
    @FXML
    private void onBuscarCodigo() {
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            actualizar();
            return;
        }
        Apartamento encontrado = sga.buscarApartamento(codigo);
        ListaDoble<Apartamento> resultado = new ListaDoble<>();
        if (encontrado != null) {
            resultado.agregarFinal(encontrado);
        }
        mostrarResumen();
        mostrar(resultado, "búsqueda por código en el árbol: «" + codigo + "»");
    }

    @FXML
    private void onBuscarHuesped() {
        String documento = txtHuesped.getText() == null ? "" : txtHuesped.getText().trim();
        if (documento.isEmpty()) {
            actualizar();
            return;
        }
        Huesped huesped = sga.buscarHuesped(documento);
        mostrarResumen();
        mostrar(sga.apartamentosDeHuesped(documento), huesped == null
                ? "no hay un huésped con documento «" + documento + "»"
                : "reservas activas de " + huesped.getNombre());
    }

    @FXML
    private void onLimpiar() {
        ignorarEventos = true;
        tgTodos.setSelected(true);
        cbBloque.setValue(TODOS);
        cbPiso.setValue(TODOS);
        txtCodigo.clear();
        txtHuesped.clear();
        ignorarEventos = false;
        actualizar();
    }

    @FXML
    private void onCargarDatos() {
        try {
            DatosPrueba.cargarEscenario();
            principal.alCargarDatos();
            VistaUtil.info("Escenario cargado", DatosPrueba.resumen());
            llenarCombos();
            actualizar();
        } catch (IOException | RuntimeException ex) {
            VistaUtil.error("No se pudo cargar el escenario", ex);
        }
    }

    // =========================================== acciones de las tarjetas

    void checkIn(Apartamento apartamento) {
        Reserva reserva = sga.entradaDeHoy(apartamento);
        String quien = reserva == null ? "" : " de " + reserva.getCodigoReserva() + " (" + reserva.getHuesped().getNombre() + ")";
        if (!VistaUtil.confirmar("Check-in en " + apartamento.getCodigo(),
                "¿Registrar la llegada" + quien + "? El apartamento pasará a Ocupado.")) {
            return;
        }
        try {
            Reserva enCurso = sga.checkInDeHoy(apartamento.getCodigo());
            VistaUtil.info("Check-in registrado", enCurso.getCodigoReserva() + " está en curso en "
                    + apartamento.getCodigo() + ".");
            actualizar();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo hacer el check-in", ex);
        }
    }

    void checkOut(Apartamento apartamento) {
        Reserva reserva = sga.reservaEnCurso(apartamento);
        String quien = reserva == null ? "" : " de " + reserva.getCodigoReserva() + " (" + reserva.getHuesped().getNombre() + ")";
        if (!VistaUtil.confirmar("Check-out en " + apartamento.getCodigo(),
                "¿Registrar la salida" + quien + "? El apartamento entrará a la cola de preparación.")) {
            return;
        }
        try {
            Reserva finalizada = sga.checkOutApartamento(apartamento.getCodigo());
            VistaUtil.info("Check-out registrado", finalizada.getCodigoReserva() + " quedó Finalizada. "
                    + apartamento.getCodigo() + " entró a la cola de preparación con prioridad "
                    + sga.prioridadPreparacion(apartamento) + ".");
            actualizar();
        } catch (RuntimeException ex) {
            VistaUtil.error("No se pudo hacer el check-out", ex);
        }
    }

    /** Ruta más corta (Dijkstra) desde recepción: se abre el mapa con la ruta dibujada. */
    void verRuta(Apartamento apartamento) {
        principal.abrirMapa(apartamento.getCodigo(), false);
    }

    void verDetalle(Apartamento apartamento) {
        FXMLLoader cargador = VistaUtil.cargarDialogo("detalle-apartamento.fxml");
        DetalleApartamentoViewController controlador = cargador.getController();
        controlador.mostrar(apartamento, sga);
        mostrarDialogo(cargador.getRoot(), "Detalle de " + apartamento.getCodigo());
    }

    void verHuesped(Apartamento apartamento) {
        Reserva reserva = reservaRelacionada(apartamento);
        if (reserva == null) {
            VistaUtil.info("Sin huésped", apartamento.getCodigo() + " no tiene un huésped actual ni próximo.");
            return;
        }
        FXMLLoader cargador = VistaUtil.cargarDialogo("ficha-huesped.fxml");
        FichaHuespedViewController controlador = cargador.getController();
        controlador.mostrar(reserva.getHuesped(), reserva);
        mostrarDialogo(cargador.getRoot(), "Ficha de " + reserva.getHuesped().getNombre());
    }

    /** Huésped a mostrar: el alojado ahora, si no el que llega hoy, si no el de la próxima reserva. */
    Reserva reservaRelacionada(Apartamento apartamento) {
        Reserva reserva = sga.reservaEnCurso(apartamento);
        if (reserva == null) {
            reserva = sga.entradaDeHoy(apartamento);
        }
        if (reserva == null) {
            reserva = sga.proximaReserva(apartamento);
        }
        return reserva;
    }

    private static void mostrarDialogo(DialogPane panel, String titulo) {
        VistaUtil.estilizar(panel);
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.setDialogPane(panel);
        dialogo.showAndWait();
    }

    SGA getSga() {
        return sga;
    }
}
