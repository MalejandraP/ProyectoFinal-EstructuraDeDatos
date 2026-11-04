package co.edu.uniquindio.apartasuitesuq;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListaCircular<T> implements Iterable<T> {
    private Nodo inicio;
    private int tam;

    public ListaCircular() {
        inicio = null;
        tam = 0;
    }

    private class Nodo{
        private T dato;
        private Nodo siguiente;

        public Nodo(T dato){
            this.dato = dato;
            this.siguiente = null;
        }
    }


    @Override
    public Iterator<T> iterator() {
        return new ListaCircularIterator();
    }
    private class ListaCircularIterator implements Iterator<T> {
        private Nodo actual = inicio;
        private int i = 0;

        @Override
        public boolean hasNext() {
            return i < tam && actual != null;
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            T valor = actual.dato;
            actual = actual.siguiente;
            i++;
            return valor;
        }

    }

    public T getInicio() {
        return inicio.dato;
    }

    public void setInicio(T inicio) {
        this.inicio.dato = inicio;
    }

    public int getTam() {
        return tam;
    }

    public void setTam(int tam) {
        this.tam = tam;
    }
}
