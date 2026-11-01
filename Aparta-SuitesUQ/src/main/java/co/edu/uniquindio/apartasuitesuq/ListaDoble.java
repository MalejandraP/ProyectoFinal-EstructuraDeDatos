package co.edu.uniquindio.apartasuitesuq;


import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListaDoble<T> implements Iterable<T> {

    private Nodo inicio;
    private Nodo fin;
    private int tam;

    public ListaDoble(){
        inicio = null;
        fin = null;

        tam = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListaDobleIterator();
    }

    private class Nodo {

        private T dato;
        private Nodo siguiente;
        private Nodo anterior;

        public Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

    }

    //Agregar un elemento al inicio
    public void agregarInicio(T dato){
        Nodo nuevo = new Nodo(dato);

        if(inicio == null && fin == null && tam == 0){
            inicio = nuevo;
            fin = nuevo;
        }else{
            nuevo.siguiente = inicio;
            inicio.anterior = nuevo; //?????????
            inicio = nuevo;
        }
        tam++;
    }

    //Agregar un elemento al final
    public void agregarFinal(T dato){
        Nodo nuevo = new Nodo(dato);

        if(inicio == null && fin == null && tam == 0){
            inicio = fin = nuevo;
        }else{
            fin.siguiente = nuevo;
            nuevo.anterior = fin;

            fin = nuevo;
        }
        tam++;
    }

    public void eliminarInicio(){
        if(inicio == null && fin == null && tam == 0)
            return;

        if(tam == 1){
            inicio = fin = null;
        }else{
            inicio = inicio.siguiente;
            inicio.anterior = null;
        }
        tam--;
    }

    public void eliminarFinal(){
        if(inicio == null && fin == null && tam == 0)
            return;

        if(tam == 1){
            inicio = fin = null;
        }else{
            fin = fin.anterior;
            fin.siguiente = null;

        }
        tam--;
    }
    private class ListaDobleIterator implements Iterator<T> {
        private Nodo actual = inicio;

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
