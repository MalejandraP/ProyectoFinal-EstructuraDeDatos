package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estancia;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class PanelUsuarioViewController {
    // PERFIL
    @FXML private Label lblIniciales;
    @FXML private Label lblNombre;
    @FXML private Label lblCedula;
    @FXML private Label lblTelefono;
    @FXML private Label lblCorreo;
    @FXML private Label lblNacimiento;
    // RESERVAS
    @FXML private ListView<Reserva> listaReservas;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colCodigo;
    @FXML private TableColumn<Reserva, String> colApartamento;
    @FXML private TableColumn<Reserva, String> colEntrada;
    @FXML private TableColumn<Reserva, String> colSalida;
    @FXML private TableColumn<Reserva, String> colEstado;
    @FXML private TableColumn<Reserva, String> colTotal;
    // FAVORITOS
    @FXML private ListView<Apartamento> listaFavoritos;
    @FXML private ComboBox<Apartamento> cmbApartamentos;
    // HISTORIAL
    @FXML private ListView<Estancia> listaHistorial;
    // HUESPED ACTUAL
    private Huesped huesped;


    @FXML
    private void initialize() {
        configurarListaReservas();
        configurarListaFavoritos();
        listaFavoritos.prefHeightProperty().bind(
                javafx.beans.binding.Bindings.max(72,
                        javafx.beans.binding.Bindings.size(listaFavoritos.getItems()).multiply(72))
        );
        cmbApartamentos.setConverter(new StringConverter<>() {
            @Override public String toString(Apartamento a) {
                return a == null ? "" : a.getCodigo() + " · " + a.getNombre();
            }
            @Override public Apartamento fromString(String s) { return null; }
        });
        listaHistorial.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Estancia e, boolean vacio) {
                super.updateItem(e, vacio);
                setText(vacio || e == null ? null : e.toString());
            }
        });
    }

    public void setReservas(ListaDoble<Reserva> reservas) {
        ObservableList<Reserva> filas = FXCollections.observableArrayList();
        for (Reserva r : reservas) {          // usa el Iterator de tu lista
            filas.add(r);
        }
        listaReservas.setItems(filas);
    }

    public void setHuesped(Huesped huesped) {
        this.huesped = huesped;
        lblNombre.setText(huesped.getNombre());
        lblIniciales.setText(iniciales(huesped.getNombre()));
        lblCedula.setText(huesped.getCedula());
        lblTelefono.setText(huesped.getTelefono());
        lblCorreo.setText(huesped.getCorreo());
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "ES"));
        // Si el usuario tiene fecha de nacimiento (ej. LocalDate):
        String fechaFormateada = huesped.getFechaNacimiento().format(formatoFecha).replace(".", "");
        lblNacimiento.setText(fechaFormateada);
        refrescarFavoritos();
        refrescarHistorial();
    }
    public void setApartamentos(ArbolBinarioBusqueda<Apartamento> catalogo) {
        ObservableList<Apartamento> opciones = FXCollections.observableArrayList();
        for (Apartamento a : catalogo) {      // recorre el árbol con su Iterator
            opciones.add(a);
        }
        cmbApartamentos.setItems(opciones);
    }
    private void refrescarFavoritos() {
        ObservableList<Apartamento> filas = FXCollections.observableArrayList();
        for (Apartamento a : huesped.getFavoritos()) {   // usa el Iterator del Conjunto
            filas.add(a);
        }
        listaFavoritos.setItems(filas);
    }
    private void refrescarHistorial() {
        ObservableList<Estancia> filas = FXCollections.observableArrayList();
        for (Estancia e : huesped.getHistorial()) {
            filas.add(e);
        }
        listaHistorial.setItems(filas);
    }

    @FXML
    private void onAgregarFavorito() {
        Apartamento elegido = cmbApartamentos.getValue();
        if (elegido == null) return;
        huesped.agregarFavorito(elegido);   // el Conjunto ignora los repetidos
        refrescarFavoritos();
    }

    @FXML
    private void onQuitarFavorito() {
        Apartamento elegido = listaFavoritos.getSelectionModel().getSelectedItem();
        if (elegido == null) return;
        huesped.quitarFavorito(elegido);
        refrescarFavoritos();
    }

    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        String resultado = partes[0].substring(0, 1);
        if (partes.length > 1) {
            resultado += partes[1].substring(0, 1);
        }
        return resultado.toUpperCase();
    }
    private void configurarListaFavoritos() {
        listaFavoritos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Apartamento a, boolean vacio) {
                super.updateItem(a, vacio);
                if (vacio || a == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Línea 1: Código en negrita + Nombre (Ej: "A-101 Dúplex")
                    Label lblCodigo = new Label(a.getCodigo());
                    lblCodigo.getStyleClass().add("fav-codigo");

                    Label lblNombre = new Label(" " + a.getNombre());
                    lblNombre.getStyleClass().add("fav-nombre");

                    HBox filaSuperior = new HBox(lblCodigo, lblNombre);
                    filaSuperior.setAlignment(Pos.CENTER_LEFT);

                    // Línea 2: Bloque · Piso · Cap. · desde $85.000 p/p/noche
                    String precioFormateado = String.format(new Locale("es", "CO"), "%,.0f", a.getTarifaBaseNoche());
                    String textoDetalle = String.format("%s · Piso %d · Cap. %d · desde $%s p/p/noche",
                            a.getBloque(),
                            a.getPiso(),
                            a.getCapacidadMaxima(),
                            precioFormateado);

                    Label lblDetalle = new Label(textoDetalle);
                    lblDetalle.getStyleClass().add("fav-detalle");
                    lblDetalle.setWrapText(true);

                    VBox columnaInfo = new VBox(3, filaSuperior, lblDetalle);
                    HBox.setHgrow(columnaInfo, Priority.ALWAYS);

                    // Corazón derecho
                    Label lblCorazon = new Label("♥");
                    lblCorazon.getStyleClass().add("fav-corazon");
                    lblCorazon.setMinWidth(Region.USE_PREF_SIZE);


                    // Contenedor tarjeta
                    HBox tarjeta = new HBox(10, columnaInfo, lblCorazon);
                    tarjeta.setAlignment(Pos.CENTER_LEFT);
                    tarjeta.getStyleClass().add("item-favorito");

                    // 2. Fuerza a la tarjeta a adaptarse al ancho interno de la celda sin scroll horizontal:
                    tarjeta.prefWidthProperty().bind(lv.widthProperty().subtract(18));
                    tarjeta.maxWidthProperty().bind(lv.widthProperty().subtract(18));

                    setText(null);
                    setGraphic(tarjeta);
                }
            }
        });
    }
    private void configurarListaReservas() {
        Locale colombia = new Locale("es", "CO");
        DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("d", colombia);
        DateTimeFormatter fmtMesAnio = DateTimeFormatter.ofPattern("MMM yyyy", colombia);

        listaReservas.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reserva r, boolean vacio) {
                super.updateItem(r, vacio);
                if (vacio || r == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // --- LADO IZQUIERDO ---
                    // 1. Título: Código · Apartamento
                    Label lblCodigo = new Label(r.getCodigoReserva() + " · ");
                    lblCodigo.getStyleClass().add("res-codigo");

                    String textoApto = r.getApartamento().getCodigo() + " " + r.getApartamento().getNombre();
                    Label lblApto = new Label(textoApto);
                    lblApto.getStyleClass().add("res-apto");

                    HBox filaTitulo = new HBox(lblCodigo, lblApto);
                    filaTitulo.setAlignment(Pos.CENTER_LEFT);

                    // 2. Fechas y noches (Ej: "18 - 21 dic 2026 · 3 noches")
                    long noches = ChronoUnit.DAYS.between(r.getFechaInicio(), r.getFechaFin());
                    String mesFin = r.getFechaFin().format(fmtMesAnio).replace(".", "");
                    String textoFechas = String.format("%s - %s %s · %d %s",
                            r.getFechaInicio().format(fmtDia),
                            r.getFechaFin().format(fmtDia),
                            mesFin,
                            noches,
                            noches == 1 ? "noche" : "noches");

                    Label lblFechas = new Label(textoFechas);
                    lblFechas.getStyleClass().add("res-fechas");

                    // 3. Bloque de Monto Total (y Anticipo 30% si está pendiente, como en el diseño)
                    String totalFormateado = "$" + String.format(colombia, "%,.0f", r.getCostoTotal());
                    VBox bloqueTotal = crearBloqueMonto("Total", totalFormateado);

                    HBox filaMontos = new HBox(24, bloqueTotal);
                    String estadoStr = r.getEstado().toString().toUpperCase();

                    if (estadoStr.contains("PENDIENTE")) {
                        double anticipo = r.getCostoTotal() * 0.30;
                        String anticipoFmt = "$" + String.format(colombia, "%,.0f", anticipo);
                        filaMontos.getChildren().add(crearBloqueMonto("Anticipo 30 %", anticipoFmt));
                    }

                    VBox columnaIzquierda = new VBox(6, filaTitulo, lblFechas, filaMontos);
                    HBox.setHgrow(columnaIzquierda, Priority.ALWAYS);

                    // --- LADO DERECHO (Badge de estado + Botones de acción) ---
                    Label badgeEstado = crearBadgeEstado(estadoStr);

                    HBox filaBotones = new HBox(8);
                    filaBotones.setAlignment(Pos.CENTER_RIGHT);

                    if (estadoStr.contains("CONFIRMADA") || estadoStr.contains("PENDIENTE")) {
                        Button btnCancelar = new Button("Cancelar");
                        btnCancelar.getStyleClass().addAll("boton-secundario","boton-sm");
                        btnCancelar.setMinWidth(Region.USE_PREF_SIZE);
                        btnCancelar.setOnAction(e -> onCancelarReserva(r));
                        filaBotones.getChildren().add(btnCancelar);
                    }

                    if (estadoStr.contains("PENDIENTE")) {
                        Button btnPagar = new Button("Pagar anticipo");
                        btnPagar.getStyleClass().addAll("boton-primario","boton-sm");
                        btnPagar.setMinWidth(Region.USE_PREF_SIZE);
                        btnPagar.setOnAction(e -> onPagarAnticipo(r));
                        filaBotones.getChildren().add(btnPagar);
                    } else {
                        Button btnFolio = new Button("Ver folio");
                        btnFolio.getStyleClass().addAll(estadoStr.contains("CONFIRMADA") ? "boton-primario" : "boton-secundario", "boton-sm");
                        btnFolio.setMinWidth(Region.USE_PREF_SIZE);
                        btnFolio.setOnAction(e -> onVerFolio(r));
                        filaBotones.getChildren().add(btnFolio);
                    }

                    VBox columnaDerecha = new VBox(16, badgeEstado, filaBotones);
                    columnaDerecha.setAlignment(Pos.TOP_RIGHT);

                    // Contenedor principal de la tarjeta
                    HBox tarjeta = new HBox(16, columnaIzquierda, columnaDerecha);
                    tarjeta.setAlignment(Pos.CENTER_LEFT);
                    tarjeta.getStyleClass().add("item-reserva");

                    tarjeta.prefWidthProperty().bind(lv.widthProperty().subtract(18));
                    tarjeta.maxWidthProperty().bind(lv.widthProperty().subtract(18));

                    setText(null);
                    setGraphic(tarjeta);
                }
            }
        });
    }

    private VBox crearBloqueMonto(String etiqueta, String valor) {
        Label lblEtiq = new Label(etiqueta);
        lblEtiq.getStyleClass().add("res-etiqueta-monto");
        lblEtiq.setMinWidth(Region.USE_PREF_SIZE);
        Label lblVal = new Label(valor);
        lblVal.getStyleClass().add("res-valor-monto");
        lblVal.setMinWidth(Region.USE_PREF_SIZE);
        VBox box = new VBox(2, lblEtiq, lblVal);
        box.setPadding(new Insets(6, 0, 0, 0));
        return box;
    }

    private Label crearBadgeEstado(String estado) {
        Label badge = new Label();
        badge.getStyleClass().add("badge-estado");

        if (estado.contains("CONFIRMADA")) {
            badge.setText("● Confirmada");
            badge.getStyleClass().add("badge-confirmada");
        } else if (estado.contains("PENDIENTE")) {
            badge.setText("● Pendiente");
            badge.getStyleClass().add("badge-pendiente");
        } else if (estado.contains("CURSO")) {
            badge.setText("● En curso");
            badge.getStyleClass().add("badge-encurso");
        } else {
            badge.setText("● " + estado);
            badge.getStyleClass().add("badge-cancelada");
        }
        return badge;
    }

    // Métodos de acción para conectar luego con tu lógica de negocio
    private void onVerFolio(Reserva r) {
        System.out.println("Ver folio de: " + r.getCodigoReserva());
    }

    private void onCancelarReserva(Reserva r) {
        System.out.println("Cancelar reserva: " + r.getCodigoReserva());
    }

    private void onPagarAnticipo(Reserva r) {
        System.out.println("Pagar anticipo de: " + r.getCodigoReserva());
    }
}
