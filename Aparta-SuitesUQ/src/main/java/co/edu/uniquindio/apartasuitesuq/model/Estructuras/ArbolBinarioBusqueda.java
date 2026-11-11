package co.edu.uniquindio.apartasuitesuq.model.Estructuras;


import java.util.Iterator;

/**
 * Estructura de datos propia: Árbol Binario de Búsqueda (BST)
 * @param <T> Tipo de elemento que debe implementar Comparable para definir su orden natural.
 */

public class ArbolBinarioBusqueda <T extends Comparable<T>> {
    
    // Clase interna para los nodos del árbol
    private static class NodoArbol<T> {
        T elemento;
        NodoArbol<T> izquierdo;
        NodoArbol<T> derecho;

        NodoArbol(T elemento) {
            this.elemento = elemento;
            this.izquierdo = null;
            this.derecho = null;
        }
    }

    private NodoArbol<T> raiz;
    private int tamano;

    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.tamano = 0;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public int getTamano() {
        return tamano;
    }

}
