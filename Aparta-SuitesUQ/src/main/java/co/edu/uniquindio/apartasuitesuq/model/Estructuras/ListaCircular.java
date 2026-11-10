package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.EstructuraVaciaException;

import java.util.Iterator;

/**
 * Clase genérica de una lista circularmente enlazada que implementa Iterable
 * @param <T>
 */
public class ListaCircular<T> implements Iterable<T> {
    private Nodo inicio;
    private int tam;
    private Nodo actual;

    public ListaCircular() {
        inicio = null;
        tam = 0;
    }

    /**
     * Para cumplir con la condición de que no se acceda directamente a los nodos se crea la clase privada Nodo dentro de la lista que lo recorre.
     */
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
            actual = inicio;
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

    /**
     * Método para agregar un elemento a la lista, crea un nuevo nodo con el dato ingresado.
     * Si la lista está vacía, asigna el nodo cómo el inicio y redefine el actual como el nodo ingresado, sino, lo agrega al final de la lista.
     * @param dato genérico
     */
    public void agregarFinal(T dato){
        Nodo nuevoNodo = new Nodo(dato);
        if(estaVacia()){
            inicio = nuevoNodo;
            inicio.siguiente = inicio;
            actual = inicio;
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
            throw new EstructuraVaciaException("La lista esta vacía");
        }
        if(tam == 1){
            inicio = null;
            actual = null;
        } else {
            Nodo aux = inicio;
            while(aux.siguiente != inicio){
                aux = aux.siguiente;
            }
            if(actual == inicio){
                actual = inicio.siguiente;
            }
            inicio = inicio.siguiente;
            aux.siguiente = inicio;
        }
        tam--;
    }
    public void eliminarFinal() {
        if(estaVacia()){
            throw new EstructuraVaciaException("La lista esta vacía");
        }
        if(tam == 1){
            inicio = null;
            actual = null;
        } else {
            Nodo aux = inicio;
            while (aux.siguiente.siguiente != inicio) {
                aux = aux.siguiente;
            }
            if(actual == aux.siguiente){
                actual = inicio;
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
            throw new EstructuraVaciaException("La lista está vacía");

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
        if(estaVacia())
            throw new EstructuraVaciaException("La lista está vacía");

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
     * Retorna el dato del atributo datoActual, dato en el que está parada actualmente la lista.
     * Si la lista está vacía, muestra excepción propia
     * @return T datoActual
     */
    public T datoActual() {
        if(estaVacia()){
            throw new EstructuraVaciaException("La lista está vacia");
        }
        return actual.dato;
    }

    /**
     * Avanza por la lista, mostrando el elemento en el que está actualmente
     * @return T datoActual actualizado
     */
    public T avanzar(){
        if(estaVacia()){
            throw new EstructuraVaciaException("La lista está vacía");
        }
        actual = actual.siguiente;
        return actual.dato;
    }

    /**
     * Elimina el dato ingresado, si la lista está vacía muestra excepción propia
     * @param dato
     * @return boolean está eliminado o no
     */
    public boolean eliminarDato(T dato) {
        if(estaVacia()){
            throw new EstructuraVaciaException("La lista está vacía");
        }
        Nodo aux = inicio;
        while(aux.siguiente != inicio){
            aux = aux.siguiente;
        }
        Nodo eliminado = inicio;
        do {
            if(eliminado.dato.equals(dato)){
                if(tam == 1){
                    inicio = null;
                    actual = null;
                } else {
                    eliminado.siguiente = aux.siguiente;
                    if(eliminado == inicio){
                        inicio = eliminado.siguiente;
                    }
                    if (eliminado == actual){
                        actual = eliminado.siguiente;
                    }
                }
                tam--;
                return true;
            }
            aux = eliminado;
            eliminado = eliminado.siguiente;
        } while(eliminado != inicio);
        return false;
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

    /**
     * Determina si la lista está vacía
     * @return boolean la lista está vacía?
     */
    public boolean estaVacia(){
        if(inicio == null && tam == 0){
            return true;
        }
        return false;
    }

    /**
     * Implementación de Iterable con clase privada para Iterator, se detiene cuando el número de la posición en la que se encuentra sea 1 menor al tamaño.
     * @return T dato
     */
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
                throw new EstructuraVaciaException("La lista no tiene más elementos");
            }
            T valor = actual.dato;
            actual = actual.siguiente;
            i++;
            return valor;
        }

    }

}
