package co.edu.uniquindio.apartasuitesuq.controller;

import co.edu.uniquindio.apartasuitesuq.model.SGA;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Punto único de acceso a los datos de la aplicación: todas las pantallas comparten la misma
 * instancia de la fachada {@link SGA}. Los datos de prueba vienen del archivo plano
 * {@code datos/escenario.txt}, cuyas fechas son relativas al día de hoy (HOY, HOY+3...), así
 * que las reservas de ejemplo siempre quedan vigentes.
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    /** @return la fachada compartida por todas las pantallas */
    public static SGA sga() {
        return SGA.getInstancia();
    }

    /** @return true si ya hay un escenario cargado (al menos un empleado para ingresar) */
    public static boolean hayDatos() {
        return !sga().getEmpleados().estaVacio();
    }

    /** Reemplaza los datos actuales por el escenario de prueba incluido en la aplicación. */
    public static void cargarEscenario() throws IOException {
        sga().cargarEscenarioDeEjemplo();
    }

    /** Reemplaza los datos actuales por un escenario elegido por el usuario. */
    public static void cargarEscenario(Path archivo) throws IOException {
        sga().cargarEscenario(archivo);
    }

    /** @return resumen de lo cargado, para mostrar después de una carga */
    public static String resumen() {
        SGA sga = sga();
        int reservas = 0;
        for (Object ignored : sga.getReservas()) {
            reservas++;
        }
        return sga.getApartamentos().getTamano() + " apartamentos · "
                + sga.getHuespedes().getTamano() + " huéspedes · "
                + sga.getEmpleados().getTamano() + " empleados · "
                + reservas + " reservas · "
                + sga.getMapa().getGrafo().tamano() + " nodos en el mapa";
    }
}
