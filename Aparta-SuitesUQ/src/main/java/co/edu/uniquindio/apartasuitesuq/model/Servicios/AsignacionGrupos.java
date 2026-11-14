package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
/**
 * Clase utilitaria encargada de resolver la asignación óptima de apartamentos para un grupo de personas.
 * Utiliza un algoritmo de búsqueda exhaustiva con retroceso (Backtracking) y poda por tamaño
 * para encontrar la combinación que cubra la capacidad requerida utilizando la menor cantidad
 * posible de apartamentos.
 */
public class AsignacionGrupos {
    private static ListaDoble<Apartamento> mejorAgrupacion = null;
    /**
     * Calcula y devuelve la agrupación óptima de apartamentos disponibles necesaria para alojar
     * a una cantidad específica de personas, minimizando el total de apartamentos utilizados.
     *
     * @param disponibles Lista doblemente enlazada con los apartamentos disponibles para asignar.
     * @param personas Cantidad total de personas que requieren alojamiento.
     * @return Una {@link ListaDoble} con la combinación mínima de apartamentos que cubre la capacidad,
     *         o {@code null} si la suma de capacidades de todos los apartamentos disponibles es insuficiente.
     */
    public static ListaDoble<Apartamento> asignarGrupo(ListaDoble<Apartamento> disponibles,int personas) {
        mejorAgrupacion = null;
        backtrack(disponibles, 0, 0, personas, new ListaDoble<>());
        return mejorAgrupacion;
    }
    /**
     * Método recursivo auxiliar que explora el árbol de combinaciones posibles (incluir o excluir
     * cada apartamento) mediante backtracking para hallar la solución con menos apartamentos.
     *
     * @param disponibles Lista completa de apartamentos evaluables.
     * @param indice Posición actual dentro de la lista {@code disponibles} que se está evaluando.
     * @param capacidad Capacidad acumulada de los apartamentos seleccionados en la rama actual.
     * @param personas Objetivo mínimo de capacidad requerido para alojar al grupo.
     * @param actual Combinación parcial de apartamentos seleccionados en la iteración actual.
     */
    private static void backtrack(ListaDoble<Apartamento> disponibles, int indice ,int capacidad, int personas, ListaDoble<Apartamento> actual) {
        if(capacidad >= personas) {
            if(mejorAgrupacion == null || actual.tamanio() < mejorAgrupacion.tamanio()) {
                mejorAgrupacion = actual.copiar();
            }
            return;
        }
        if (indice == disponibles.tamanio()) {
            return;}
        if(mejorAgrupacion != null && actual.tamanio() >= mejorAgrupacion.tamanio()) {
            return;
        }
        Apartamento apt = disponibles.obtener(indice);
        actual.agregarFinal(apt);
        backtrack(disponibles, indice + 1, capacidad + apt.getCapacidadMaxima(), personas, actual);
        actual.eliminarFinal();

        backtrack(disponibles, indice + 1, capacidad, personas, actual);
    }
}
