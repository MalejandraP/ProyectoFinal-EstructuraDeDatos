package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;

/**
 * Implementación genérica de una estructura de datos tipo Cola (Queue) basada en nodos enlazados.
 * Sigue el principio FIFO (First-In, First-Out), donde el primer elemento en entrar es el primero en salir.
 * Implementa la interfaz {@link Iterable} para permitir su recorrido mediante bucles {@code for-each}.
 *
 * @param <T> El tipo de elementos almacenados en la cola.
 */
public class Cola<T> implements Iterable<T> {
    protected Nodo inicio;
    protected Nodo fin;
    protected int tam;

    /**
     * Construye una cola vacía inicializando sus punteros en {@code null} y su tamaño en {@code 0}.
     */
    public Cola() {
        inicio = null;
        fin = null;
        tam = 0;
    }

    /**
     * Clase interna que representa un nodo individual dentro de la cola enlazada.
     */
    private class Nodo {
        private T dato;
        private Nodo siguiente;

        public Nodo(T dato) {
            this.dato = dato;
            siguiente = null;
        }
    }

    /**
     * Inserta un nuevo elemento al final de la cola (operación enqueue).
     * Complejidad temporal: O(1).
     *
     * @param dato El elemento que se desea agregar a la cola.
     */
    public void encolar(T dato) {
        Nodo nuevo = new Nodo(dato);
        if (estaVacia()) {
            inicio = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tam++;
    }

    /**
     * Elimina y devuelve el elemento ubicado al frente de la cola (operación dequeue).
     * Complejidad temporal: O(1).
     *
     * @return El elemento que se encontraba al inicio de la cola.
     * @throws EstructuraVaciaException Si se intenta desencolar cuando la cola está vacía.
     */
    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }
        T aux = inicio.dato;
        inicio = inicio.siguiente;
        tam--;
        if (inicio == null) {
            fin = null;
        }
        return aux;
    }

    /**
     * Verifica si un elemento específico se encuentra presente dentro de la cola.
     * Complejidad temporal: O(n).
     *
     * @param datoBuscar El elemento que se desea buscar.
     * @return {@code true} si el elemento existe en la cola; {@code false} en caso contrario.
     * @throws EstructuraVaciaException Si la cola se encuentra vacía al momento de realizar la búsqueda.
     */
    public boolean buscar(T datoBuscar) {
        Nodo aux = inicio;
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola está vacía");
        }
        while (aux != null) {
            if (datoBuscar.equals(aux.dato)) {
                return true;
            }
            aux = aux.siguiente;
        }
        return false;
    }

    /**
     * Busca un elemento en la cola y devuelve su posición relativa (índice base 0) desde el frente.
     * Complejidad temporal: O(n).
     *
     * @param datoBuscar El elemento cuya posición se desea conocer.
     * @return El índice del elemento (empezando en 0 para {@code inicio}), o {@code -1} si no se encuentra o la cola está vacía.
     */
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

    /**
     * Genera una representación en cadena de texto de los elementos de la cola desde el inicio hasta el fin.
     * Si la cola está vacía, retorna {@code "[ Null ]"}.
     *
     * @return Una cadena con el formato {@code "[ elem1 elem2 ... ]"}.
     */
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

    /**
     * Comprueba si la cola no contiene ningún elemento.
     *
     * @return {@code true} si la cola está vacía; {@code false} si contiene al menos un elemento.
     */
    public boolean estaVacia() {
        if (inicio == null && fin == null && tam == 0) {
            return true;
        }
        return false;
    }

    /**
     * Devuelve un iterador sobre los elementos de la cola en orden secuencial (desde {@code inicio} hasta {@code fin}).
     *
     * @return Una instancia de {@link Iterator} para recorrer la cola.
     */
    @Override
    public Iterator<T> iterator() {
        return new ColaIterator();
    }

    /**
     * Implementación privada del iterador para recorrer los nodos de la cola sin modificar su estructura.
     */
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