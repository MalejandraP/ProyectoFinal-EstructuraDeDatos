package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;
/**
 * Implementación genérica de una Lista Doblemente Enlazada (Doubly Linked List).
 * Cada nodo mantiene referencias tanto al nodo siguiente como al anterior, permitiendo
 * inserciones y eliminaciones eficientes en ambos extremos (inicio y fin).
 * Implementa la interfaz {@link Iterable} para permitir su recorrido mediante bucles {@code for-each}.
 *
 * @param <T> El tipo de elementos almacenados en la lista.
 */
public class ListaDoble<T> implements Iterable<T> {

    private Nodo inicio;
    private Nodo fin;
    private int tam;

    public ListaDoble(){
        inicio = null;
        fin = null;

        tam = 0;
    }

    /**
     * Devuelve un iterador sobre los elementos de la lista en orden secuencial,
     * desde {@code inicio} hasta {@code fin}.
     *
     * @return Una instancia de {@link Iterator} para recorrer la lista.
     */
    @Override
    public Iterator<T> iterator() {
        return new ListaDobleIterator();
    }

    /**
     * Clase interna que representa un nodo dentro de la lista doblemente enlazada.
     */
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

    /**
     * Inserta un nuevo elemento al principio de la lista.
     * Complejidad temporal: O(1).
     *
     * @param dato El elemento que se desea agregar al inicio.
     */
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

    /**
     * Inserta un nuevo elemento al final de la lista.
     * Complejidad temporal: O(1).
     *
     * @param dato El elemento que se desea agregar al final.
     */
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

    /**
     * Elimina el primer elemento de la lista y actualiza las referencias de {@code inicio}.
     * Complejidad temporal: O(1).
     *
     * @throws EstructuraVaciaException Si se intenta eliminar cuando la lista está vacía.
     */
    public void eliminarInicio(){
        if(estaVacia())
            throw new EstructuraVaciaException("No hay elementos en la lista");

        if(tam == 1){
            inicio = fin = null;
        }else{
            inicio = inicio.siguiente;
            inicio.anterior = null;
        }
        tam--;
    }
    /**
     * Elimina el último elemento de la lista y actualiza las referencias de {@code fin}.
     * Complejidad temporal: O(1).
     *
     * @throws EstructuraVaciaException Si se intenta eliminar cuando la lista está vacía.
     */
    public void eliminarFinal(){
        if(estaVacia())
            throw new EstructuraVaciaException("No hay elemntos en la lista") ;

        if(tam == 1){
            inicio = fin = null;
        }else{
            fin = fin.anterior;
            fin.siguiente = null;

        }
        tam--;
    }
    /**
     * Comprueba si la lista no contiene ningún elemento.
     *
     * @return {@code true} si la lista está vacía; {@code false} si contiene al menos un elemento.
     */
    public boolean estaVacia(){
        if(inicio == null && fin == null && tam == 0){
            return true;
        }
        return false;
    }
    /**
     * Devuelve la cantidad total de elementos almacenados actualmente en la lista.
     *
     * @return El tamaño actual de la lista.
     */
    public int tamanio(){
        return tam;
    }
    /**
     * Crea y devuelve una copia superficial (shallow copy) de la lista actual,
     * conservando el mismo orden de sus elementos.
     * Complejidad temporal: O(n).
     *
     * @return Una nueva instancia de {@link ListaDoble} con los mismos elementos.
     */
    public ListaDoble<T> copiar(){
        ListaDoble<T> copia = new ListaDoble<>();
        for(T dato : this){
            copia.agregarFinal(dato);
        }
        return copia;
    }
    /**
     * Busca y elimina la primera ocurrencia del elemento especificado dentro de la lista.
     * Complejidad temporal: O(n).
     *
     * @param elemento El elemento que se desea eliminar.
     * @return {@code true} si el elemento fue encontrado y eliminado; {@code false} si no existe en la lista.
     */
    public boolean eliminar(T elemento) {
        Nodo actual = inicio;
        while (actual != null) {
            if (actual.dato.equals(elemento)) {
                if (actual == inicio) {
                    eliminarInicio();
                } else if (actual == fin) {
                    eliminarFinal();
                } else {
                    actual.anterior.siguiente = actual.siguiente;
                    actual.siguiente.anterior = actual.anterior;
                    tam--;
                }
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }
    /**
     * Devuelve el elemento almacenado en la posición indicada (índice base 0).
     * Complejidad temporal: O(n).
     *
     * @param posicion El índice del elemento que se desea obtener (empezando en 0).
     * @return El dato ubicado en dicha posición, o {@code null} si la posición está fuera de rango.
     * @throws EstructuraVaciaException Si la lista se encuentra vacía.
     */
    public T obtener(int posicion){
        if (estaVacia()) {
            throw new EstructuraVaciaException("No hay elementos en la lista");
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
    /**
     * Verifica si un elemento específico se encuentra presente dentro de la lista.
     * Complejidad temporal: O(n).
     *
     * @param datoBuscar El elemento que se desea buscar.
     * @return {@code true} si el elemento existe en la lista; {@code false} en caso contrario.
     * @throws EstructuraVaciaException Si la lista se encuentra vacía al realizar la búsqueda.
     */
    public boolean buscar(T datoBuscar){
        if (estaVacia() ) {
            throw new EstructuraVaciaException("No hay elementos en la lista");
        }
        Nodo aux = inicio;
        while (aux != null) {
            if (aux.dato.equals(datoBuscar)) {
                return true;
            }
            aux = aux.siguiente;
        }
        return false;
    }
    /**
     * Implementación privada del iterador para recorrer los nodos de la lista desde el inicio hasta el fin.
     */
    private class ListaDobleIterator implements Iterator<T> {
        private Nodo actual = inicio;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new EstructuraVaciaException("No hay más elementos en la lista");
            }
            T valor = actual.dato;
            actual = actual.siguiente;
            return valor;
        }

    }


}
