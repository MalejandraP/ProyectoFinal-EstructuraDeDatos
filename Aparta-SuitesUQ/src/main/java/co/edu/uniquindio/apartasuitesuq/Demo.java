package co.edu.uniquindio.apartasuitesuq;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.*;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ColaPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.ColoresEstado;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.MapaEdificioView;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.PanelPreparacionView;
import co.edu.uniquindio.apartasuitesuq.model.ViewPrueba.TableroInicioView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class Demo extends Application {
    @Override
    public void start(Stage stage) {
        MapaEdificio mapa = MapaEdificio.ejemplo();
        ListaDoble<Apartamento> apartamentos = new ListaDoble<>();
        for (NodoMapa n : mapa) {
            if (n.getTipo() == TipoNodo.APARTAMENTO) {
                apartamentos.agregarFinal(new Apartamento(n.getCodigo(), "Suite " + n.getCodigo(),
                        n.getCodigo().substring(0, 1), 1, 2, 4, 120000));
            }
        }
        ColaPreparacion cola = new ColaPreparacion(a -> null);
        ServicioPreparacion servicio = new ServicioPreparacion(mapa.getGrafo(), mapa.getRecepcion(), cola);
        AnalisisGrafo analisis = new AnalisisGrafo(mapa);

        // Un par de estados para que el mapa tenga colores desde el inicio
        apartamentos.obtener(0).setEstado(co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento.OCUPADO);
        servicio.checkOut(apartamentos.obtener(0), LocalDateTime.now());
        servicio.ponerFueraDeServicio(apartamentos.obtener(1), "Fuga de agua en el baño");

        Label detalle = new Label("Haga clic en un nodo del mapa");
        MapaEdificioView vista = new MapaEdificioView(mapa, codigo -> {
            for (Apartamento a : apartamentos) {
                if (a.getCodigo().equals(codigo)) return ColoresEstado.de(a.getEstado());
            }
            return null;
        });
        TableroInicioView tablero = new TableroInicioView(apartamentos, servicio, analisis);
        PanelPreparacionView panel = new PanelPreparacionView(servicio, cola, apartamentos, () -> {
            vista.redibujar();
            tablero.refrescar();
        });
        vista.setAlSeleccionar(codigo -> {
            String motivo = null;
            for (Apartamento a : apartamentos) {
                if (a.getCodigo().equals(codigo)) {
                    motivo = servicio.motivoFueraDeServicio(a);
                    detalle.setText(codigo + ": " + a.getEstado() + (motivo == null ? "" : " — " + motivo));
                    return;
                }
            }
            detalle.setText(codigo + ": " + mapa.buscar(codigo).getTipo()
                    + " (" + analisis.distanciaDesdeRecepcion(codigo) + " m desde recepción)");
        });
        Button ruta = new Button("Mostrar ruta de preparación");
        ruta.setOnAction(e -> vista.mostrarRuta(servicio.rutaSugerida()));
        Button cerrar = new Button("Cerrar/abrir pasillo ESC_A–ESC_B");
        cerrar.setOnAction(e -> {
            if (mapa.getGrafo().pasilloCerrado("ESC_A", "ESC_B")) mapa.getGrafo().abrirPasillo("ESC_A", "ESC_B");
            else mapa.getGrafo().cerrarPasillo("ESC_A", "ESC_B");
            vista.redibujar();
            tablero.refrescar();
        });

        BorderPane mapaTab = new BorderPane(vista);
        mapaTab.setTop(new HBox(8, ruta, cerrar, detalle));
        mapaTab.setRight(panel);

        TabPane tabs = new TabPane(new Tab("Inicio", tablero), new Tab("Mapa y preparación", mapaTab));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        stage.setTitle("SGA – P3: grafo y preparación");
        stage.setScene(new Scene(tabs, 1000, 560));
        stage.show();
        vista.redibujar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
