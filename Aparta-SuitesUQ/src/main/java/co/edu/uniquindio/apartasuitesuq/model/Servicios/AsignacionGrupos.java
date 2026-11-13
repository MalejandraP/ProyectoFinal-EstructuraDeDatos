package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;

public class AsignacionGrupos {
    private static ListaDoble<Apartamento> mejorAgrupacion = null;

    public static ListaDoble<Apartamento> asignarGrupo(ListaDoble<Apartamento> disponibles,int personas) {
        mejorAgrupacion = null;
        backtrack(disponibles, 0, 0, personas, new ListaDoble<>());
        return mejorAgrupacion;
    }
    private static void backtrack(ListaDoble<Apartamento> disponibles, int indice ,int capacidad, int personas, ListaDoble<Apartamento> actual) {
        if(capacidad >= personas) {
            if(mejorAgrupacion == null || actual.tamanio() < mejorAgrupacion.tamanio()) {
                mejorAgrupacion = actual.copiar();
            }
            return;
        }
        if (indice == disponibles.tamanio()) {
            return;}
        if(mejorAgrupacion != null && actual.tamanio() <= mejorAgrupacion.tamanio()) {
            return;
        }
        Apartamento apt = disponibles.obtener(indice);
        actual.agregarFinal(apt);
        backtrack(disponibles, indice + 1, capacidad + apt.getCapacidadMaxima(), personas, actual);
        actual.eliminarFinal();

        backtrack(disponibles, indice + 1, capacidad, personas, actual);
    }
}
