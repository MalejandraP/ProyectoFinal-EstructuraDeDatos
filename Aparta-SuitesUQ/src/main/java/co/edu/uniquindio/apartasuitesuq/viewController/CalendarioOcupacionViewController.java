package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoReserva;
import co.edu.uniquindio.apartasuitesuq.model.Reserva;
import co.edu.uniquindio.apartasuitesuq.model.SGA;
import co.edu.uniquindio.apartasuitesuq.view.CalendarioOcupacionView;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Controlador del calendario de ocupación mensual (P2): consulta los apartamentos
 * y sus árboles de reservas para pintar cada día como Libre, Ocupado/Reservado o Bloqueado.
 */
public class CalendarioOcupacionViewController {

    private final CalendarioOcupacionView vista;
    private final SGA sga;

    public CalendarioOcupacionViewController(CalendarioOcupacionView vista, SGA sga) {
        this.vista = vista;
        this.sga = sga;
        inicializar();
    }

    private void inicializar() {
        LocalDate hoy = sga.hoy();
        seleccionarMes(hoy.getMonthValue());
        vista.getSpAnio().getValueFactory().setValue(hoy.getYear());

        vista.getCbMes().setOnAction(e -> renderizar());
        vista.getSpAnio().valueProperty().addListener((obs, ant, act) -> renderizar());
        vista.getBtnActualizar().setOnAction(e -> renderizar());
        vista.getBtnMesAnterior().setOnAction(e -> cambiarMes(-1));
        vista.getBtnMesSiguiente().setOnAction(e -> cambiarMes(1));

        renderizar();
    }

    private void cambiarMes(int delta) {
        YearMonth actual = obtenerYearMonth();
        YearMonth nuevo = actual.plusMonths(delta);
        vista.getSpAnio().getValueFactory().setValue(nuevo.getYear());
        seleccionarMes(nuevo.getMonthValue());
        renderizar();
    }

    private void seleccionarMes(int numeroMes) {
        for (CalendarioOcupacionView.MesItem item : vista.getCbMes().getItems()) {
            if (item.getMes().getValue() == numeroMes) {
                vista.getCbMes().setValue(item);
                break;
            }
        }
    }

    private YearMonth obtenerYearMonth() {
        CalendarioOcupacionView.MesItem item = vista.getCbMes().getValue();
        int mes = item == null ? sga.hoy().getMonthValue() : item.getMes().getValue();
        Integer anio = vista.getSpAnio().getValue();
        return YearMonth.of(anio == null ? sga.hoy().getYear() : anio, mes);
    }

    public void renderizar() {
        GridPane grid = vista.getCuadricula();
        grid.getChildren().clear();

        if (sga.getApartamentos().estaVacio()) {
            vista.getLblResumen().setText("No hay apartamentos registrados en el catálogo.");
            return;
        }

        YearMonth ym = obtenerYearMonth();
        int diasMes = ym.lengthOfMonth();

        // Encabezado columna 0: Apartamento
        Label cabeceraApto = crearCelda("Apartamento", 185, 34, "#F3F1ED", "#D6D1C9", "#2C2A29", true);
        grid.add(cabeceraApto, 0, 0);

        // Encabezados columnas 1..diasMes: días del mes
        for (int dia = 1; dia <= diasMes; dia++) {
            Label cabeceraDia = crearCelda(String.valueOf(dia), 34, 34, "#F3F1ED", "#D6D1C9", "#2C2A29", true);
            grid.add(cabeceraDia, dia, 0);
        }

        int fila = 1;
        int totalNochesOcupadas = 0;
        int totalNochesDisponibles = 0;

        for (Apartamento apto : sga.getApartamentos()) {
            Label celdaNombre = crearCelda(
                    apto.getCodigo() + " · " + apto.getNombre(),
                    185, 32, "#FFFFFF", "#E6E2DB", "#2C2A29", false
            );
            celdaNombre.setAlignment(Pos.CENTER_LEFT);
            celdaNombre.setStyle(celdaNombre.getStyle() + " -fx-padding: 0 8 0 8;");
            grid.add(celdaNombre, 0, fila);

            boolean bloqueado = apto.estaFueraDeServicio();
            String motivoBloqueo = bloqueado ? sga.motivoFueraDeServicio(apto) : null;

            for (int dia = 1; dia <= diasMes; dia++) {
                LocalDate fecha = ym.atDay(dia);
                Label celda;

                if (bloqueado) {
                    celda = crearCelda("X", 34, 32, "#FFEBEE", "#EF9A9A", "#C62828", true);
                    Tooltip.install(celda, new Tooltip(apto.getCodigo() + " · " + fecha
                            + "\nBLOQUEADO (Fuera de servicio)"
                            + (motivoBloqueo != null ? "\nMotivo: " + motivoBloqueo : "")));
                } else {
                    Reserva reservaActiva = buscarReservaEnFecha(apto, fecha);
                    if (reservaActiva != null) {
                        totalNochesOcupadas++;
                        celda = crearCelda("R", 34, 32, "#E3F2FD", "#90CAF9", "#1565C0", true);
                        Tooltip.install(celda, new Tooltip(
                                apto.getCodigo() + " · " + fecha
                                        + "\nOCUPADO / RESERVADO (" + reservaActiva.getCodigoReserva() + ")"
                                        + "\nTitular: " + reservaActiva.getTitular().getNombre()
                                        + "\nEstado: " + reservaActiva.getEstado()
                                        + " (" + reservaActiva.getFechaInicio() + " → "
                                        + reservaActiva.getEstancia().getSalida() + ")"
                        ));
                    } else {
                        totalNochesDisponibles++;
                        celda = crearCelda("·", 34, 32, "#E8F5E9", "#A5D6A7", "#2E7D32", false);
                        Tooltip.install(celda, new Tooltip(apto.getCodigo() + " · " + fecha + "\nLIBRE"));
                    }
                }
                grid.add(celda, dia, fila);
            }
            fila++;
        }

        int totalOperativas = totalNochesOcupadas + totalNochesDisponibles;
        double ocupacionPct = totalOperativas == 0 ? 0.0 : (totalNochesOcupadas * 100.0) / totalOperativas;
        vista.getLblResumen().setText(String.format(
                "Apartamentos: %d · Noches reservadas en el mes: %d · Noches libres: %d · Ocupación operativa: %.1f %%",
                fila - 1, totalNochesOcupadas, totalNochesDisponibles, ocupacionPct));
    }

    private Reserva buscarReservaEnFecha(Apartamento apto, LocalDate fecha) {
        for (Reserva r : apto.getReservas()) {
            if (r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.NOSHOW) {
                LocalDate inicio = r.getFechaInicio();
                LocalDate salida = r.getEstancia().getSalida();
                if (!fecha.isBefore(inicio) && fecha.isBefore(salida)) {
                    return r;
                }
            }
        }
        return null;
    }

    private Label crearCelda(String texto, double ancho, double alto,
                             String fondo, String borde, String colorTexto, boolean negrita) {
        Label label = new Label(texto);
        label.setMinSize(ancho, alto);
        label.setPrefSize(ancho, alto);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: " + fondo
                + "; -fx-border-color: " + borde
                + "; -fx-border-radius: 4; -fx-background-radius: 4;"
                + " -fx-text-fill: " + colorTexto + ";"
                + (negrita ? " -fx-font-weight: bold;" : ""));
        return label;
    }
}