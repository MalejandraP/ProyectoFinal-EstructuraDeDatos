package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de calendario.fxml (C). Arma la cuadrícula del mes: una fila por apartamento
 * (recorrido inorden del árbol) y una columna por noche. La fachada dice qué reserva ocupa cada
 * noche y si el apartamento estaba bloqueado; aquí solo se pinta.
 */
public class CalendarioViewController implements ControladorPantalla {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM yyyy", VistaUtil.CO);
    private static final String[] DIAS = {"L", "M", "X", "J", "V", "S", "D"};

    @FXML private Label lblMes;
    @FXML private Label lblResumen;
    @FXML private GridPane cuadricula;
    @FXML private javafx.scene.control.ScrollPane desplazamiento;
    @FXML private Label lblDetalle;

    private SGA sga;
    private YearMonth mes;
    private Label seleccionada;

    @Override
    public void inicializar(PrincipalViewController principal) {
        this.sga = principal.getSga();
        this.mes = YearMonth.from(sga.hoy());
        // el desplazamiento solo es horizontal: su alto sigue al de la cuadrícula (+ barra)
        desplazamiento.prefViewportHeightProperty().bind(cuadricula.heightProperty());
        desplazamiento.minViewportHeightProperty().bind(cuadricula.heightProperty());
        dibujar();
    }

    @FXML
    private void onMesAnterior() {
        mes = mes.minusMonths(1);
        dibujar();
    }

    @FXML
    private void onMesSiguiente() {
        mes = mes.plusMonths(1);
        dibujar();
    }

    @FXML
    private void onHoy() {
        mes = YearMonth.from(sga.hoy());
        dibujar();
    }

    private void dibujar() {
        String titulo = MES.format(mes);
        lblMes.setText(Character.toUpperCase(titulo.charAt(0)) + titulo.substring(1));
        cuadricula.getChildren().clear();
        seleccionada = null;
        lblDetalle.setText("Haz clic en una celda para ver la reserva y el huésped.");

        cuadricula.add(celda("Apartamento", "celda-cabecera", "celda-apto"), 0, 0);
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            LocalDate fecha = mes.atDay(dia);
            Label cabecera = celda(DIAS[fecha.getDayOfWeek().getValue() - 1] + "\n" + dia, "celda-cabecera");
            if (fecha.equals(sga.hoy())) {
                cabecera.getStyleClass().add("celda-hoy");
            }
            cuadricula.add(cabecera, dia, 0);
        }

        int fila = 1;
        int ocupadas = 0;
        int bloqueadas = 0;
        int libres = 0;
        for (Apartamento apartamento : sga.getApartamentos()) {
            cuadricula.add(celda(apartamento.getCodigo() + " · " + apartamento.getNombre(), "celda-apto"), 0, fila);
            for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
                LocalDate noche = mes.atDay(dia);
                Label celda;
                if (sga.bloqueadaEn(apartamento, noche)) {
                    celda = celda("✕", "celda-bloqueada");
                    bloqueadas++;
                    String motivo = sga.motivoFueraDeServicio(apartamento);
                    String texto = apartamento.getCodigo() + " · " + VistaUtil.fecha(noche)
                            + ": bloqueada (fuera de servicio)" + (motivo == null ? "" : ". Motivo: " + motivo);
                    configurar(celda, texto);
                } else {
                    Reserva reserva = sga.ocupacionEn(apartamento, noche);
                    if (reserva != null) {
                        celda = celda(reserva.ocupaNoche(noche) && noche.equals(reserva.getFechaInicio()) ? "▸" : "", "celda-ocupada");
                        ocupadas++;
                        configurar(celda, apartamento.getCodigo() + " · " + VistaUtil.fecha(noche) + ": "
                                + reserva.getCodigoReserva() + " (" + reserva.getEstado().getDescripcion() + ") · huésped "
                                + reserva.getHuesped().getNombre() + " · doc. " + reserva.getHuesped().getDocumento()
                                + " · tel. " + reserva.getHuesped().getTelefono() + " · " + reserva.getNumeroPersonas()
                                + " personas · " + VistaUtil.fecha(reserva.getFechaInicio()) + " → "
                                + VistaUtil.fecha(reserva.getFechaFin()) + " · canal " + reserva.getCanal().getDescripcion());
                    } else {
                        celda = celda("", "celda-libre");
                        libres++;
                        configurar(celda, apartamento.getCodigo() + " · " + VistaUtil.fecha(noche) + ": libre.");
                    }
                }
                if (noche.equals(sga.hoy())) {
                    celda.getStyleClass().add("celda-hoy");
                }
                cuadricula.add(celda, dia, fila);
            }
            fila++;
        }
        int operativas = ocupadas + libres;
        lblResumen.setText(ocupadas + " noches ocupadas · " + libres + " libres · " + bloqueadas + " bloqueadas · ocupación "
                + VistaUtil.porcentaje(operativas == 0 ? 0 : ocupadas * 100.0 / operativas));
    }

    private static Label celda(String texto, String... clases) {
        Label celda = new Label(texto);
        celda.getStyleClass().add("celda");
        celda.getStyleClass().addAll(clases);
        return celda;
    }

    /** Al hacer clic se muestra el detalle (reserva y huésped) y se resalta la celda. */
    private void configurar(Label celda, String detalle) {
        Tooltip.install(celda, new Tooltip(detalle));
        celda.setOnMouseClicked(e -> {
            if (seleccionada != null) {
                seleccionada.getStyleClass().remove("celda-seleccionada");
            }
            seleccionada = celda;
            celda.getStyleClass().add("celda-seleccionada");
            lblDetalle.setText(detalle);
        });
    }
}
