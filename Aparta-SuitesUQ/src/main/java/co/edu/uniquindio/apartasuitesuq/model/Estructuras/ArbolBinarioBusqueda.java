package co.edu.uniquindio.apartasuitesuq.model.Estructuras;


import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Estructura de datos propia: Árbol Binario de Búsqueda (BST)
 * @param <T> Tipo de elemento que debe implementar Comparable para definir su orden natural.
 */

public class ArbolBinarioBusqueda <T extends Comparable<T>> implements Iterable<T>{


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

    @Override
    public Iterator<T> iterator() {
        return new IteradorInorden();
    }

    private class IteradorInorden implements Iterator<T> {
        private NodoArbol<T>[] pila;
        private int tope;

        @SuppressWarnings("unchecked")
        public IteradorInorden() {
            this.pila = (NodoArbol<T>[]) new NodoArbol[tamano + 1];
            this.tope = -1;
            empujarIzquierdos(raiz);
        }

        private void empujarIzquierdos(NodoArbol<T> nodo) {
            while (nodo != null) {
                pila[++tope] = nodo;
                nodo = nodo.izquierdo;
            }
        }

        @Override
        public boolean hasNext() {
            return tope >= 0;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No hay más elementos en el árbol.");
            }
            NodoArbol<T> nodoActual = pila[tope--];
            if (nodoActual.derecho != null) {
                empujarIzquierdos(nodoActual.derecho);
            }
            return nodoActual.elemento;
        }
    }

    public void insertar(T elemento) {
        if (elemento == null)
            return;
        raiz = insertarRecursivo(raiz, elemento);
    }

    private NodoArbol<T> insertarRecursivo(NodoArbol<T> actual, T elemento) {
        if (actual == null) {
            tamano++;
            return new NodoArbol<>(elemento);
        }

        int comparacion = elemento.compareTo(actual.elemento);

        if (comparacion < 0) {
            actual.izquierdo = insertarRecursivo(actual.izquierdo, elemento);
        } else if (comparacion > 0) {
            actual.derecho = insertarRecursivo(actual.derecho, elemento);
        }
        // Si comparacion == 0, se ignora el duplicado

        return actual;
    }


    public T buscar(T elemento) {
        if (elemento == null)
            return null;
        return buscarRecursivo(raiz, elemento);
    }

    private T buscarRecursivo(NodoArbol<T> actual, T elemento) {
        if (actual == null) {
            return null;
        }

        int comparacion = elemento.compareTo(actual.elemento);

        if (comparacion == 0) {
            return actual.elemento;
        } else if (comparacion < 0) {
            return buscarRecursivo(actual.izquierdo, elemento);
        } else {
            return buscarRecursivo(actual.derecho, elemento);
        }
    }

    public boolean contiene(T elemento) {
        return buscar(elemento) != null;
    }
}
