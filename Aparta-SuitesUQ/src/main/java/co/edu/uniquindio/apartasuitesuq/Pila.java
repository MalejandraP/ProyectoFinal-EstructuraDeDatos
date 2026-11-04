package co.edu.uniquindio.apartasuitesuq;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Pila<T> implements Iterable<T> {
    private Nodo cima;
    private int tam;

    @Override
    public Iterator<T> iterator() {
        return new PilaIterator();
    }

    private class Nodo{
        private T dato;
        private Nodo siguiente;

        public Nodo(T dato){
            this.dato = dato;
        }
    }
    public Pila() {
        cima = null;
        tam = 0;
    }

    /**
     * Agrega en la cima de la pila
     * Push
     */
    public void apilar(T dato) {
        Nodo nuevoNodo = new Nodo(dato);
        nuevoNodo.siguiente = cima;
        cima = nuevoNodo;
        tam++;
    }

    /**
     * Elimina la cima de la pila
     * pop
     */
    public T desapilar() {
        if (cima == null){
            throw new IllegalStateException("La pila está vacía");
        }
        T dato = cima.dato;
        cima = cima.siguiente;
        tam--;
        return dato;
    }

    /**
     * Busca un elemento dentro de la lista.
     */
    public boolean buscar(T datoBuscar) {
        Nodo aux = cima;
        while (aux != null) {

            if (aux.dato.equals(datoBuscar)) {
                return true;
            }
            aux = aux.siguiente;
        }

        return false;
    }

    /**
     * Retorna la posición del elemento dentro de la lista.
     * Si no existe retorna -1.
     */
    public int localizar(T datoBuscar) {
        Nodo aux = cima;
        int count = 0;
        while (aux != null) {
            if (aux.dato.equals(datoBuscar)) {
                return count;
            }
            count++;
            aux = aux.siguiente;
        }
        return -1;
    }

    /**
     * Retorna los elementos de la lista como String.
     */
    public String mostrar() {
        String listaString = "[";
        if (cima == null) {
            listaString += " Null";
        } else {
            Nodo aux = cima;
            while (aux != null) {
                listaString += " " + aux.dato;
                aux = aux.siguiente;
            }
        }
        listaString += " ]";
        return listaString;
    }

    public T getCima() {
        return cima.dato;
    }

    public void setCima(T cima) {
        this.cima.dato = cima;//???
    }

    public int getTam() {
        return tam;
    }

    public void setTam(int tam) {
        this.tam = tam;
    }

    private class PilaIterator implements Iterator<T> {
        Nodo actual = cima;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            T valor = actual.dato;
            actual = actual.siguiente;
            return valor;
        }
    }

}
