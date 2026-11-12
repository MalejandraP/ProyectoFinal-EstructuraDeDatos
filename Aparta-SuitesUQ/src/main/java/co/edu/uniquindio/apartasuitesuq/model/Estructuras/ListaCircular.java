package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;

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
    public void agregarInicio(T dato){
        Nodo nuevoNodo = new Nodo(dato);
        if(estaVacia()){
            inicio = nuevoNodo;
            inicio.siguiente = inicio;
        } else {
            Nodo aux = inicio;
            while(aux.siguiente != inicio){
                aux = aux.siguiente;
            }
            nuevoNodo.siguiente = inicio;
            aux.siguiente = nuevoNodo;
            inicio = nuevoNodo;
        }
        tam++;
    }
    public void agregarFinal(T dato){
        Nodo nuevoNodo = new Nodo(dato);
        if(estaVacia()){
            inicio = nuevoNodo;
            inicio.siguiente = inicio;
        } else {
            Nodo aux = inicio;
            while(aux.siguiente != inicio){
                aux = aux.siguiente;
            }
            aux.siguiente = nuevoNodo;
            nuevoNodo.siguiente = inicio;
        }
        tam++;
    }
    public void eliminarInicio(){
        if(estaVacia()){
            throw new NoSuchElementException();
        }
        if(tam == 1){
            inicio = null;
        } else {
            Nodo aux = inicio;
            while(aux.siguiente != inicio){
                aux = aux.siguiente;
            }
            inicio = inicio.siguiente;
            aux.siguiente = inicio;
        }
        tam--;
    }
    public void eliminarFinal() {
        if(estaVacia()){
            throw new NoSuchElementException();
        }
        if(tam == 1){
            inicio = null;
        } else {
            Nodo aux = inicio;
            while (aux.siguiente.siguiente != inicio) {
                aux = aux.siguiente;
            }
            aux.siguiente = inicio;
        }
        tam--;
    }

    /**
     * Busca un elemento dentro de la lista.
     */
    public boolean buscar(T datoBuscar) {
        if(estaVacia())
            throw new NoSuchElementException();

        Nodo aux = inicio;

        do{
            if (aux.dato.equals(datoBuscar)) {
                return true;
            }
            aux = aux.siguiente;
        } while (aux != inicio);

        return false;
    }

    /**
     * Retorna la posición del elemento dentro de la lista.
     * Si no existe retorna -1.
     */
    public int localizar(T datoBuscar) {
        if(inicio == null && tam == 0)
            return -1;

        Nodo aux = inicio;
        int count = 0;

        do{
            if (aux.dato.equals(datoBuscar)) {
                return count;
            }
            count++;
            aux = aux.siguiente;
        } while (aux != inicio);

        return -1;
    }

    /**
     * Retorna los elementos de la lista como String.
     */
    public String mostrar() {
        String listaString = "[";
        if (inicio == null) {
            listaString += " Null";
        } else {
            Nodo aux = inicio;

            do{
                listaString += " " + aux.dato;
                aux = aux.siguiente;
            } while (aux != inicio);
        }
        listaString += " ]";
        return listaString;
    }

    public boolean estaVacia(){
        if(inicio == null && tam == 0){
            return true;
        }
        return false;
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
