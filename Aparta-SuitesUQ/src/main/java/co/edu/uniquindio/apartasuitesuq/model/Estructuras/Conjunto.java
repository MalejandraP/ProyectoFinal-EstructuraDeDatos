package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;

/**
 * Implementación genérica de una estructura de datos tipo Conjunto (Set).
 * Almacena una colección de elementos únicos (sin duplicados) y no admite valores {@code null}.
 * Internamente delega el almacenamiento y recorrido a una {@link ListaDoble}.
 * Implementa la interfaz {@link Iterable} para permitir su recorrido mediante bucles {@code for-each}.
 *
 * @param <T> El tipo de elementos almacenados en el conjunto.
 */
public class Conjunto<T> implements Iterable<T> {
    /** Estructura interna encargada de almacenar los elementos únicos del conjunto. */
    private final ListaDoble<T> elementos = new ListaDoble<>();

    public Conjunto() {
    }

    /**
     * Agrega un elemento al conjunto siempre y cuando no sea {@code null} ni exista previamente.
     * Complejidad temporal: O(n), debido a la verificación previa de duplicados.
     *
     * @param elemento El elemento que se desea incorporar al conjunto.
     * @return {@code true} si el elemento fue agregado exitosamente; {@code false} si era {@code null} o ya estaba presente.
     */
    public boolean agregar(T elemento) {
        if(elemento == null || buscar(elemento)){
            return false;
        }
        elementos.agregarFinal(elemento);
        return true;
    }

    /**
     * Elimina el elemento especificado del conjunto si se encuentra presente.
     * Complejidad temporal: O(n).
     *
     * @param elemento El elemento que se desea remover del conjunto.
     * @return {@code true} si el elemento existía y fue eliminado; {@code false} en caso contrario.
     */
    public boolean eliminar(T elemento) {
        return elementos.eliminar(elemento);
    }
    /**
     * Devuelve la cantidad total de elementos únicos almacenados en el conjunto.
     * Complejidad temporal: O(1).
     *
     * @return El tamaño actual del conjunto.
     */
    public int tamanio(){
        return elementos.tamanio();
    }
    /**
     * Comprueba si el conjunto no contiene ningún elemento.
     * Complejidad temporal: O(1).
     *
     * @return {@code true} si el conjunto está vacío; {@code false} si contiene al menos un elemento.
     */
    public boolean estaVacia(){
        return elementos.estaVacia();
    }
    /**
     * Verifica si un elemento específico pertenece al conjunto.
     * Utiliza el iterador interno para recorrer la colección de forma segura incluso si está vacía.
     * Complejidad temporal: O(n).
     *
     * @param elemento El elemento cuya pertenencia se desea comprobar.
     * @return {@code true} si el elemento está contenido en el conjunto; {@code false} en caso contrario.
     */
    public boolean buscar(T elemento) {
        for(T elem : elementos){
            if(elem.equals(elemento)){
                return true;
            }
        }
        return false;
    }
    /**
     * Devuelve un iterador sobre los elementos únicos del conjunto en orden de inserción.
     *
     * @return Una instancia de {@link Iterator} delegada por la lista interna.
     */
    @Override
    public Iterator<T> iterator() {
        return elementos.iterator();
    }
}
