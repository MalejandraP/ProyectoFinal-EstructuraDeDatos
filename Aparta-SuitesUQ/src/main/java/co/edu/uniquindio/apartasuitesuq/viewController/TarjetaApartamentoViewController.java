package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

/**
 * Controlador de tarjeta-apartamento.fxml: datos del apartamento, color del semáforo según su
 * estado, información contextual y acciones (las acciones las resuelve el Inicio).
 */
public class TarjetaApartamentoViewController {

    @FXML private Circle punto;
    @FXML private Label lblCodigo;
    @FXML private Label lblEstado;
    @FXML private Label lblNombre;
    @FXML private Label lblUbicacion;
    @FXML private Label lblCapacidad;
    @FXML private Label lblContexto;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;
    @FXML private Button btnRuta;
    @FXML private Button btnHuesped;

    private Apartamento apartamento;
    private InicioViewController inicio;

    void mostrar(Apartamento apartamento, InicioViewController inicio) {
        this.apartamento = apartamento;
        this.inicio = inicio;
        SGA sga = inicio.getSga();
        EstadoApartamento estado = apartamento.getEstadoOperativo();
        String clase = VistaUtil.claseEstado(estado);

        punto.getStyleClass().setAll("punto-" + clase);
        lblCodigo.setText(apartamento.getCodigo());
        lblEstado.setText(estado.getDescripcion());
        lblEstado.getStyleClass().setAll("chip", "chip-" + clase);
        lblNombre.setText(apartamento.getNombre());
        lblUbicacion.setText("Bloque " + apartamento.getBloque() + " · Piso " + apartamento.getPiso());
        lblCapacidad.setText(apartamento.getNumeroDormitorios()
                + (apartamento.getNumeroDormitorios() == 1 ? " dormitorio" : " dormitorios")
                + " · hasta " + apartamento.getCapacidadMaxima() + " personas");
        lblContexto.setText(contexto(sga, estado));

        // Check-in solo si está Preparado; se habilita si alguien llega hoy
        Reserva entrada = sga.entradaDeHoy(apartamento);
        mostrarBoton(btnCheckIn, estado == EstadoApartamento.PREPARADO);
        btnCheckIn.setDisable(entrada == null);
        if (entrada == null) {
            Tooltip.install(btnCheckIn, new Tooltip("No hay una reserva confirmada que entre hoy"));
        }
        mostrarBoton(btnCheckOut, estado == EstadoApartamento.OCUPADO);
        btnRuta.setDisable(sga.getMapa().getRecepcion() == null
                || !sga.getMapa().getGrafo().contiene(apartamento.getCodigo()));
        btnHuesped.setDisable(inicio.reservaRelacionada(apartamento) == null);
    }

    /** Texto contextual según el estado (entrada de hoy, huésped actual, prioridad, motivo). */
    private String contexto(SGA sga, EstadoApartamento estado) {
        switch (estado) {
            case PREPARADO: {
                Reserva entrada = sga.entradaDeHoy(apartamento);
                if (entrada != null) {
                    return "Entrada hoy a las " + VistaUtil.hora(entrada.getHoraEstimadaLlegada()) + " · "
                            + entrada.getHuesped().getNombre() + " (" + entrada.getCodigoReserva() + ")";
                }
                Reserva proxima = sga.proximaReserva(apartamento);
                return proxima == null ? "Libre, sin reservas próximas"
                        : "Libre · próxima llegada " + VistaUtil.fechaCorta(proxima.getFechaInicio())
                        + " (" + proxima.getCodigoReserva() + ")";
            }
            case OCUPADO: {
                Reserva enCurso = sga.reservaEnCurso(apartamento);
                return enCurso == null ? "Ocupado"
                        : "Huésped: " + enCurso.getHuesped().getNombre() + " · sale el "
                        + VistaUtil.fechaCorta(enCurso.getFechaFin()) + " (" + enCurso.getCodigoReserva() + ")";
            }
            case PENDIENTEDEPREPARACION: {
                int prioridad = sga.prioridadPreparacion(apartamento);
                Reserva entrada = sga.entradaDeHoy(apartamento);
                if (prioridad == 1 && entrada != null) {
                    return "Prioridad 1 en la cola: llegan hoy a las "
                            + VistaUtil.hora(entrada.getHoraEstimadaLlegada()) + " (" + entrada.getCodigoReserva() + ")";
                }
                return prioridad == 0 ? "Pendiente de preparación" : "Prioridad " + prioridad + " en la cola: sin entrada hoy";
            }
            case ENPREPARACION:
                return "El personal lo está preparando ahora";
            default:
                return "Motivo: " + sga.motivoFueraDeServicio(apartamento);
        }
    }

    private static void mostrarBoton(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    @FXML
    private void onDetalle() {
        inicio.verDetalle(apartamento);
    }

    @FXML
    private void onCheckIn() {
        inicio.checkIn(apartamento);
    }

    @FXML
    private void onCheckOut() {
        inicio.checkOut(apartamento);
    }

    @FXML
    private void onVerRuta() {
        inicio.verRuta(apartamento);
    }

    @FXML
    private void onVerHuesped() {
        inicio.verHuesped(apartamento);
    }
}
