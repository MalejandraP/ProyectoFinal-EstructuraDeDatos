package co.edu.uniquindio.apartasuitesuq.model;

/**
 * Una fila de un reporte estadístico: una etiqueta (apartamento, temporada, mes, canal...)
 * y su valor numérico. Las pantallas de estadísticas la muestran como barra de un gráfico
 * o como fila de una tabla.
 */
public class Indicador {

    private final String etiqueta;
    private final double valor;

    public Indicador(String etiqueta, double valor) {
        this.etiqueta = etiqueta;
        this.valor = valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return etiqueta + ": " + valor;
    }
}
