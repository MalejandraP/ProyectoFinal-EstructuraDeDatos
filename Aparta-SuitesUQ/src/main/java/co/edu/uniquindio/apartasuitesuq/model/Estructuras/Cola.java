package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;

public class Cola<T> implements Iterable<T> {
    protected Nodo inicio;
    protected Nodo fin;
    protected int tam;

    public Cola() {
        inicio = null;
        fin = null;
        tam = 0;
    }

    private class Nodo{
        private T dato;
        private Nodo siguiente;

        public Nodo(T dato) {
            this.dato = dato;
            siguiente = null;
        }
    }
    public void encolar(T dato){
        Nodo nuevo = new Nodo(dato);
        if(estaVacia()){
            inicio = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tam++;
    }

    public T desencolar(){
        if (estaVacia()){
            throw new EstructuraVaciaException("La cola está vacía");
        }
        T aux = inicio.dato;
        inicio = inicio.siguiente;
        tam --;
        if(inicio == null){
            fin = null;
        }
        return aux;
    }

    public boolean buscar(T datoBuscar){
        Nodo aux = inicio;
        if (estaVacia()){
            throw new EstructuraVaciaException("La cola está vacía");
        }
        while (aux != null){
            if(datoBuscar.equals(aux.dato)){
                return true;
            }
            aux = aux.siguiente;
        }
        return false;
    }

    public int localizar(T datoBuscar) {
        Nodo aux = inicio;
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
    public String mostrar() {
        String listaString = "[";
        if (inicio == null) {
            listaString += " Null";
        } else {
            Nodo aux = inicio;
            while (aux != null) {
                listaString += " " + aux.dato;
                aux = aux.siguiente;
            }
        }
        listaString += " ]";
        return listaString;
    }

    public boolean estaVacia(){
        if (inicio == null && fin == null && tam == 0){
            return true;
        }
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        return new ColaIterator();
    }
    private class ColaIterator implements Iterator<T> {
        private Nodo actual = inicio;
        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new EstructuraVaciaException("No hay más elementos en la cola");
            }
            T dato = actual.dato;
            actual = actual.siguiente;
            return dato;
        }
    }


}