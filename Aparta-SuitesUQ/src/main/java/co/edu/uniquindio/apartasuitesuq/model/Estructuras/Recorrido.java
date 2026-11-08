package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Recorrido <T> implements Iterable<T> {
    private Object[] datos = new Object[8];
    private int tam;
    private double distancia;

    public void agregar(T elemento) {
        if (tam == datos.length) {
            Object[] nuevo = new Object[tam * 2];
            System.arraycopy(datos, 0, nuevo, 0, tam);
            datos = nuevo;
        }
        datos[tam++] = elemento;
    }

    @SuppressWarnings("unchecked")
    public T obtener(int i) {
        if (i < 0 || i >= tam) throw new IndexOutOfBoundsException("Posición " + i);
        return (T) datos[i];
    }

    public int tamano() { return tam; }

    public boolean estaVacio() { return tam == 0; }

    public double getDistancia() { return distancia; }

    public void setDistancia(double distancia) { this.distancia = distancia; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i = 0;

            @Override
            public boolean hasNext() { return i < tam; }

            @Override
            public T next() {
                if (i >= tam) throw new NoSuchElementException();
                return obtener(i++);
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tam; i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(datos[i]);
        }
        return sb + " (Distancia: " + distancia + ")";
    }
}
