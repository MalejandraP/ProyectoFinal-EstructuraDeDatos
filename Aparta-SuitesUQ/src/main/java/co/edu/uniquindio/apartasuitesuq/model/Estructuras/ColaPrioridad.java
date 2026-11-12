package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ColaPrioridad <T> implements Iterable<T> {
    private Object[] heap = new Object[16];
    private int tam;
    private final Comparator<? super T> comparador;

    public ColaPrioridad(Comparator<? super T> comparador) {
        if (comparador == null) throw new IllegalArgumentException("El comparador no puede ser null");
        this.comparador = comparador;
    }

    public void encolar(T elemento) {
        if (elemento == null) throw new IllegalArgumentException("No se admite null");
        if (tam == heap.length) {
            Object[] nuevo = new Object[tam * 2];
            System.arraycopy(heap, 0, nuevo, 0, tam);
            heap = nuevo;
        }
        heap[tam] = elemento;
        subir(tam);
        tam++;
    }

    public T desencolar() {
        if (tam == 0) throw new EstructuraVaciaException("La cola de prioridad está vacía");
        T raiz = get(0);
        tam--;
        heap[0] = heap[tam];
        heap[tam] = null;
        if (tam > 0) bajar(0);
        return raiz;
    }

    public T ver() {
        if (tam == 0) throw new EstructuraVaciaException("La cola de prioridad está vacía");
        return get(0);
    }

    public boolean estaVacia() { return tam == 0; }

    public int tamano() { return tam; }

    @SuppressWarnings("unchecked")
    private T get(int i) { return (T) heap[i]; }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (comparador.compare(get(i), get(padre)) >= 0) break;
            intercambiar(i, padre);
            i = padre;
        }
    }

    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1, der = izq + 1, menor = i;
            if (izq < tam && comparador.compare(get(izq), get(menor)) < 0) menor = izq;
            if (der < tam && comparador.compare(get(der), get(menor)) < 0) menor = der;
            if (menor == i) break;
            intercambiar(i, menor);
            i = menor;
        }
    }

    private void intercambiar(int a, int b) {
        Object t = heap[a];
        heap[a] = heap[b];
        heap[b] = t;
    }
    
    @Override
    public Iterator<T> iterator() {
        ColaPrioridad<T> copia = new ColaPrioridad<>(comparador);
        copia.heap = new Object[Math.max(16, tam)];
        System.arraycopy(heap, 0, copia.heap, 0, tam);
        copia.tam = tam;
        return new Iterator<T>() {
            @Override
            public boolean hasNext() { return !copia.estaVacia(); }

            @Override
            public T next() {
                if (copia.estaVacia()) throw new NoSuchElementException();
                return copia.desencolar();
            }
        };
    }
}
