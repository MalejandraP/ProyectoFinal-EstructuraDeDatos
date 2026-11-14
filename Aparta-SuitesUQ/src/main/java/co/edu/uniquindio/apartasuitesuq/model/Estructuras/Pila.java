package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;
/**
 * Implementación genérica de una estructura de datos tipo Pila (Stack) basada en nodos enlazados.
 * Sigue el principio LIFO (Last-In, First-Out), donde el último elemento en entrar es el primero en salir.
 * Implementa la interfaz {@link Iterable} para permitir su recorrido mediante bucles {@code for-each}.
 *
 * @param <T> El tipo de elementos almacenados en la pila.
 */
public class Pila<T> implements Iterable<T> {
    private Nodo cima;
    private int tam;

    /**
     * Devuelve un iterador sobre los elementos de la pila en orden secuencial,
     * comenzando desde la {@code cima} hacia el fondo.
     *
     * @return Una instancia de {@link Iterator} para recorrer la pila.
     */
    @Override
    public Iterator<T> iterator() {
        return new PilaIterator();
    }

    /**
     * Clase interna que representa un nodo individual dentro de la pila enlazada.
     */
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
     * Inserta un nuevo elemento en la cima de la pila (operación push).
     * Complejidad temporal: O(1).
     *
     * @param dato El elemento que se desea apilar.
     */
    public void apilar(T dato) {
        Nodo nuevoNodo = new Nodo(dato);
        nuevoNodo.siguiente = cima;
        cima = nuevoNodo;
        tam++;
    }

    /**
     * Elimina y devuelve el elemento ubicado en la cima de la pila (operación pop).
     * Complejidad temporal: O(1).
     *
     * @return El elemento que se encontraba en la cima de la pila.
     * @throws EstructuraVaciaException Si se intenta desapilar cuando la pila está vacía.
     */
    public T desapilar() {
        if (estaVacia()){
            throw new EstructuraVaciaException("La pila está vacía");
        }
        T dato = cima.dato;
        cima = cima.siguiente;
        tam--;
        return dato;
    }

    /**
     * Verifica si un elemento específico se encuentra presente dentro de la pila.
     * Complejidad temporal: O(n).
     *
     * @param datoBuscar El elemento que se desea buscar.
     * @return {@code true} si el elemento existe en la pila; {@code false} en caso contrario.
     * @throws EstructuraVaciaException Si la pila se encuentra vacía al momento de realizar la búsqueda.
     */
    public boolean buscar(T datoBuscar) {
        if (estaVacia()){
            throw new EstructuraVaciaException("La pila está vacía");
        }
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
     * Busca un elemento en la pila y devuelve su distancia o posición relativa (índice base 0) desde la cima.
     * Complejidad temporal: O(n).
     *
     * @param datoBuscar El elemento cuya posición se desea conocer.
     * @return El índice del elemento (empezando en 0 para {@code cima}), o {@code -1} si no se encuentra.
     * @throws EstructuraVaciaException Si la pila se encuentra vacía al momento de realizar la búsqueda.
     */
    public int localizar(T datoBuscar) {
        if (estaVacia()){
            throw new EstructuraVaciaException("La pila está vacía");
        }
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
        if (estaVacia()) {
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

    /**
     * Comprueba si la pila no contiene ningún elemento.
     *
     * @return {@code true} si la pila está vacía; {@code false} si contiene al menos un elemento.
     */
    public boolean estaVacia(){
        if (cima == null && tam == 0){
            return true;
        }
        return false;
    }


    /**
     * Sobrescribe el dato almacenado actualmente en el nodo cima de la pila.
     *
     * @param cima El nuevo valor que reemplazará al dato en la cima.
     * @throws NullPointerException Si la pila está vacía ({@code cima == null}).
     */
    public void setCima(T cima) {
        this.cima.dato = cima;
    }



    /**
     * Implementación privada del iterador para recorrer los nodos de la pila sin modificar su estructura.
     */
    private class PilaIterator implements Iterator<T> {
        Nodo actual = cima;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new EstructuraVaciaException("No hay más elementos en la pila");
            }
            T valor = actual.dato;
            actual = actual.siguiente;
            return valor;
        }
    }

}
