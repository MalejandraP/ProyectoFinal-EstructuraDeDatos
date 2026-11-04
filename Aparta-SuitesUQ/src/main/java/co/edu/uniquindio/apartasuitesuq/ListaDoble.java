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

        if(estaVacia()){
            inicio = nuevo;
            fin = nuevo;
        }else{
            nuevo.siguiente = inicio;
            inicio.anterior = nuevo;
            inicio = nuevo;
        }
        tam++;
    }

    //Agregar un elemento al final
    public void agregarFinal(T dato){
        Nodo nuevo = new Nodo(dato);

        if(estaVacia()){
            inicio = fin = nuevo;
        }else{
            fin.siguiente = nuevo;
            nuevo.anterior = fin;

            fin = nuevo;
        }
        tam++;
    }

    public void eliminarInicio(){
        if(estaVacia())
            throw new NoSuchElementException();

        if(tam == 1){
            inicio = fin = null;
        }else{
            inicio = inicio.siguiente;
            inicio.anterior = null;
        }
        tam--;
    }

    public void eliminarFinal(){
        if(estaVacia())
            throw new NoSuchElementException() ;

        if(tam == 1){
            inicio = fin = null;
        }else{
            fin = fin.anterior;
            fin.siguiente = null;

        }
        tam--;
    }

    public boolean estaVacia(){
        if(inicio == null && fin == null && tam == 0){
            return true;
        }
        return false;
    }
    public T obtener(int posicion){ //FUNCIONA IGUAL???
        if (estaVacia()) {
            throw new NoSuchElementException("No hay elementos en la lista");
        }
        Nodo aux = inicio;
        int contador = 0;
        while (aux != null && contador <= posicion) {
            if(contador == posicion){
                return aux.dato;
            }
            contador++;
            aux = aux.siguiente;
        }
        return null;
    }

    public boolean buscar(T datoBuscar){
        if (estaVacia() ) {
            throw new NoSuchElementException("No hay elementos en la lista");
        }
        Nodo aux = inicio;
        do{
            if (aux.dato.equals(datoBuscar)){
                return true;
            }
            aux = aux.siguiente;
        } while (aux != inicio);

        return false;
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
