package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Canal;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Huesped;
import co.edu.uniquindio.apartasuitesuq.model.Ocupante;
import co.edu.uniquindio.apartasuitesuq.model.OrdenApartamentos;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.Rol;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.view.ReservasView;
import co.edu.uniquindio.apartasuitesuq.view.Sesion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;

/**
 * Controlador del panel de reservas: gestiona los eventos de búsqueda ordenada,
 * registro de acompañantes con fecha de nacimiento, creación de reservas, pago de
 * anticipo del 30 %, cancelación con devolución y sincronización de estado en la tabla.
 */
public class ReservasViewController {

    private final ReservasView vista;
    private final SGA sga;
    private final Sesion sesion;

    public ReservasViewController(ReservasView vista, SGA sga, Sesion sesion) {
        this.vista = vista;
        this.sga = sga;
        this.sesion = sesion;
        inicializar();
    }

    private void inicializar() {
        vista.getDpEntrada().setValue(sga.hoy().plusDays(1));
        vista.getDpSalida().setValue(sga.hoy().plusDays(3));
        vista.getDpEntrada().valueProperty().addListener((obs, ant, act) -> vista.getListaAcompanantes().refresh());

        vista.getCbOrden().getItems().setAll(ReservasView.CriterioOrden.values());
        vista.getCbOrden().setValue(ReservasView.CriterioOrden.PRECIO);
        vista.getCbOrden().setOnAction(e -> buscarDisponibles());

        vista.getBtnBuscar().setOnAction(e -> buscarDisponibles());
        vista.getBtnAutocompletar().setOnAction(e -> autocompletarHuesped());
        vista.getBtnAgregarAcompanante().setOnAction(e -> agregarAcompanante());
        vista.getBtnQuitarAcompanante().setOnAction(e -> quitarAcompanante());
        vista.getBtnReservar().setOnAction(e -> crearReserva());
        vista.getBtnAnticipo().setOnAction(e -> pagarAnticipoSeleccionada());
        vista.getBtnCancelar().setOnAction(e -> cancelarSeleccionada());
        vista.getBtnCheckIn().setOnAction(e -> realizarCheckInSeleccionada());
        vista.getBtnCheckOut().setOnAction(e -> realizarCheckOutSeleccionada());
        vista.getBtnRefrescar().setOnAction(e -> {
            buscarDisponibles();
            recargarReservas();
        });

        vista.getTablaDisponibles().getSelectionModel().selectedItemProperty().addListener((obs, ant, sel) -> {
            if (sel == null) {
                vista.getLblSeleccionado().setText("Ningún apartamento seleccionado");
            } else {
                vista.getLblSeleccionado().setText("Seleccionado: " + sel.getCodigo() + " - " + sel.getNombre()
                        + " (Capacidad máx. " + sel.getCapacidadMaxima() + " personas)");
            }
        });

        vista.getTablaReservas().getSelectionModel().selectedItemProperty().addListener((obs, ant, sel) -> {
            actualizarEstadoBotones(sel);
        });

        if (sesion.getRol() == Rol.HUESPED && sesion.getHuesped() != null) {
            Huesped h = sesion.getHuesped();
            vista.getCbCanal().getItems().setAll(Canal.PORTAL);
            vista.getCbCanal().setValue(Canal.PORTAL);
            vista.getCbCanal().setDisable(true);
            vista.mostrarModoHuesped("Titular: " + h.getNombre() + " · Doc: " + h.getDocumento()
                    + " (Canal Portal · anticipo 30 %)");
            vista.getBtnCheckIn().setVisible(false);
            vista.getBtnCheckIn().setManaged(false);
            vista.getBtnCheckOut().setVisible(false);
            vista.getBtnCheckOut().setManaged(false);
        } else {
            vista.getCbCanal().getItems().setAll(Canal.DIRECTA, Canal.PORTAL);
            vista.getCbCanal().setValue(Canal.DIRECTA);
            vista.mostrarModoEmpleado();
        }

        buscarDisponibles();
        recargarReservas();
    }

    public void buscarDisponibles() {
        try {
            LocalDate entrada = vista.getDpEntrada().getValue();
            LocalDate salida = vista.getDpSalida().getValue();
            int personas = vista.getSpPersonas().getValue();
            Comparator<Apartamento> orden = obtenerComparador(vista.getCbOrden().getValue());

            vista.getTablaDisponibles().setItems(aLista(sga.buscarDisponibles(entrada, salida, personas, orden)));
        } catch (RuntimeException ex) {
            mostrarError("No se pudo consultar la disponibilidad", ex);
        }
    }

    private Comparator<Apartamento> obtenerComparador(ReservasView.CriterioOrden criterio) {
        if (criterio == null) {
            return Comparator.naturalOrder();
        }
        switch (criterio) {
            case PRECIO:
                return OrdenApartamentos.porPrecio();
            case CAPACIDAD:
                return OrdenApartamentos.porCapacidad();
            case DORMITORIOS:
                return OrdenApartamentos.porDormitorios();
            case RESERVAS:
                return OrdenApartamentos.porNumeroDeReservas();
            default:
                return Comparator.naturalOrder();
        }
    }

    private void autocompletarHuesped() {
        String doc = txtOrEmpty(vista.getTxtDocumento().getText());
        if (doc.isBlank()) {
            mostrarInfo("Documento requerido", "Ingrese un número de documento para buscar en el catálogo.");
            return;
        }
        Huesped encontrado = sga.buscarHuesped(doc);
        if (encontrado == null) {
            mostrarInfo("Huésped nuevo", "No existe un huésped con documento " + doc
                    + ". Complete sus datos para registrarlo al reservar.");
            return;
        }
        vista.getTxtNombre().setText(encontrado.getNombre());
        vista.getTxtTelefono().setText(encontrado.getTelefono());
        vista.getTxtCorreo().setText(encontrado.getCorreo());
        if (encontrado.getFechaNacimiento() != null) {
            vista.getDpNacimiento().setValue(encontrado.getFechaNacimiento());
        }
    }

    private void agregarAcompanante() {
        String nombre = txtOrEmpty(vista.getTxtNombreAcompanante().getText());
        LocalDate nacimiento = vista.getDpNacimientoAcompanante().getValue();
        int maxAcompanantes = Math.max(0, vista.getSpPersonas().getValue() - 1);

        if (nombre.isBlank()) {
            mostrarInfo("Nombre requerido", "Ingrese el nombre completo del acompañante.");
            return;
        }
        if (nacimiento == null || nacimiento.isAfter(sga.hoy())) {
            mostrarInfo("Fecha inválida", "Seleccione una fecha de nacimiento válida para el acompañante.");
            return;
        }
        if (vista.getListaAcompanantes().getItems().size() >= maxAcompanantes) {
            mostrarInfo("Límite de personas alcanzado",
                    "El grupo tiene " + vista.getSpPersonas().getValue()
                            + " persona(s) en total (titular + máximo " + maxAcompanantes + " acompañante(s)).");
            return;
        }

        vista.getListaAcompanantes().getItems().add(new Ocupante(nombre, nacimiento));
        vista.getTxtNombreAcompanante().clear();
    }

    private void quitarAcompanante() {
        Ocupante sel = vista.getListaAcompanantes().getSelectionModel().getSelectedItem();
        if (sel != null) {
            vista.getListaAcompanantes().getItems().remove(sel);
        }
    }

    private void crearReserva() {
        Apartamento seleccionado = vista.getTablaDisponibles().getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarInfo("Seleccione un apartamento", "Elija primero un apartamento disponible en la tabla superior.");
            return;
        }
        int totalPersonas = vista.getSpPersonas().getValue();
        ObservableList<Ocupante> acompanantesUI = vista.getListaAcompanantes().getItems();
        if (acompanantesUI.size() > totalPersonas - 1) {
            mostrarInfo("Cantidad de acompañantes inválida",
                    "Registró " + acompanantesUI.size() + " acompañantes, pero el total de personas es " + totalPersonas + ".");
            return;
        }

        try {
            Huesped titular = obtenerOTitularNuevo();
            ListaDoble<Ocupante> listaOcupantes = new ListaDoble<>();
            for (Ocupante acompanante : acompanantesUI) {
                listaOcupantes.agregarFinal(acompanante);
            }

            Reserva creada = sga.crearReserva(
                    titular,
                    seleccionado.getCodigo(),
                    vista.getDpEntrada().getValue(),
                    vista.getDpSalida().getValue(),
                    listaOcupantes,
                    totalPersonas,
                    vista.getCbCanal().getValue()
            );

            int pagantesPrimeraNoche = creada.contarPagantesEn(creada.getFechaInicio());
            int exentos = totalPersonas - pagantesPrimeraNoche;

            String detalleAnticipo = creada.getCanal() == Canal.PORTAL
                    ? "\nEstado: PENDIENTE. Anticipo requerido (30 %): " + moneda(creada.getValorAnticipo())
                    : "\nEstado: CONFIRMADA directamente en recepción.";

            mostrarInfo("Reserva registrada",
                    "Código: " + creada.getCodigoReserva()
                            + "\nApartamento: " + seleccionado.getCodigo() + " - " + seleccionado.getNombre()
                            + "\nOcupantes: " + totalPersonas + " (" + pagantesPrimeraNoche + " pagante(s), "
                            + exentos + " menor(es) de 6 años exento(s))"
                            + "\nTotal estancia: " + moneda(creada.getCostoTotal())
                            + detalleAnticipo);

            vista.getListaAcompanantes().getItems().clear();
            buscarDisponibles();
            recargarReservas();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo crear la reserva", ex);
        }
    }

    private Huesped obtenerOTitularNuevo() {
        if (sesion.getRol() == Rol.HUESPED && sesion.getHuesped() != null) {
            return sesion.getHuesped();
        }
        String doc = txtOrEmpty(vista.getTxtDocumento().getText());
        Huesped existente = sga.buscarHuesped(doc);
        if (existente != null) {
            return existente;
        }
        return new Huesped(
                doc,
                vista.getTxtNombre().getText(),
                vista.getTxtTelefono().getText(),
                vista.getTxtCorreo().getText(),
                vista.getDpNacimiento().getValue()
        );
    }

    private void pagarAnticipoSeleccionada() {
        Reserva seleccionada = vista.getTablaReservas().getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarInfo("Seleccione una reserva", "Elija una reserva de la tabla para registrar el anticipo.");
            return;
        }
        try {
            double anticipo = seleccionada.getValorAnticipo();
            sga.pagarAnticipo(seleccionada);
            mostrarInfo("Anticipo registrado",
                    "Se registró el pago de " + moneda(anticipo)
                            + " y la reserva " + seleccionada.getCodigoReserva() + " pasó a CONFIRMADA.");
            recargarReservas();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo registrar el anticipo", ex);
        }
    }

    private void cancelarSeleccionada() {
        Reserva seleccionada = vista.getTablaReservas().getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarInfo("Seleccione una reserva", "Elija una reserva de la tabla para cancelarla.");
            return;
        }
        try {
            double devolucionEstimada = sga.calcularDevolucion(seleccionada);
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar cancelación");
            confirmacion.setHeaderText("¿Desea cancelar la reserva " + seleccionada.getCodigoReserva() + "?");
            confirmacion.setContentText("Valor a devolver según política actual: "
                    + moneda(devolucionEstimada)
                    + "\nEsta acción liberará las noches del apartamento.");

            Optional<ButtonType> respuesta = confirmacion.showAndWait();
            if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
                double devuelto = sga.cancelarReserva(seleccionada);
                mostrarInfo("Reserva cancelada",
                        "La reserva " + seleccionada.getCodigoReserva() + " ha sido cancelada."
                                + "\nDevolución procesada: " + moneda(devuelto));
                buscarDisponibles();
                recargarReservas();
            }
        } catch (RuntimeException ex) {
            mostrarError("No se pudo cancelar la reserva", ex);
        }
    }

    public void recargarReservas() {
        Reserva seleccionada = vista.getTablaReservas().getSelectionModel().getSelectedItem();
        if (sesion.getRol() == Rol.HUESPED && sesion.getHuesped() != null) {
            vista.getTablaReservas().setItems(aLista(sga.reservasDe(sesion.getHuesped())));
        } else {
            vista.getTablaReservas().setItems(aLista(sga.getReservas()));
        }
        vista.getTablaReservas().refresh();
        actualizarEstadoBotones(seleccionada);
    }

    private void actualizarEstadoBotones(Reserva sel) {
        if (sel == null) {
            vista.getBtnAnticipo().setDisable(true);
            vista.getBtnCheckIn().setDisable(true);
            vista.getBtnCheckOut().setDisable(true);
            vista.getBtnCancelar().setDisable(true);
            return;
        }
        EstadoReserva estado = sel.getEstado();
        vista.getBtnAnticipo().setDisable(estado != EstadoReserva.PENDIENTE);
        vista.getBtnCheckIn().setDisable(estado != EstadoReserva.CONFIRMADA);
        vista.getBtnCheckOut().setDisable(estado != EstadoReserva.ENCURSO);
        vista.getBtnCancelar().setDisable(
                estado == EstadoReserva.CANCELADA
                        || estado == EstadoReserva.NOSHOW
                        || estado == EstadoReserva.FINALIZADA
        );
    }

    private static String txtOrEmpty(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static <T> ObservableList<T> aLista(Iterable<T> datos) {
        ObservableList<T> lista = FXCollections.observableArrayList();
        for (T dato : datos) {
            lista.add(dato);
        }
        return lista;
    }

    private static String moneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    private static void mostrarError(String titulo, Throwable causa) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(causa.getMessage() == null ? causa.toString() : causa.getMessage());
        alerta.showAndWait();
    }

    private static void mostrarInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void realizarCheckInSeleccionada() {
        Reserva seleccionada = vista.getTablaReservas().getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarInfo("Seleccione una reserva", "Elija una reserva confirmada para registrar el check-in.");
            return;
        }
        try {
            sga.checkIn(seleccionada);
            mostrarInfo("Check-in registrado",
                    "La reserva " + seleccionada.getCodigoReserva() + " pasó a EN CURSO.\nApartamento "
                            + seleccionada.getApartamento().getCodigo() + " ahora está OCUPADO.");
            recargarReservas();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo realizar el check-in", ex);
        }
    }

    private void realizarCheckOutSeleccionada() {
        Reserva seleccionada = vista.getTablaReservas().getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarInfo("Seleccione una reserva", "Elija una reserva en curso para registrar el check-out.");
            return;
        }
        try {
            sga.checkOut(seleccionada);
            mostrarInfo("Check-out registrado",
                    "La reserva " + seleccionada.getCodigoReserva() + " pasó a FINALIZADA.\nEl apartamento "
                            + seleccionada.getApartamento().getCodigo() + " ingresó a la cola de preparación.");
            buscarDisponibles();
            recargarReservas();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo realizar el check-out", ex);
        }
    }
}