package co.edu.uniquindio.apartasuitesuq.model.Estructuras;

import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;

import java.util.Iterator;

public class Conjunto<T> implements Iterable<T> {
    private final ListaDoble<T> elementos = new ListaDoble<>();

    public Conjunto() {
    }

    public boolean agregar(T elemento) {
        if(elemento == null || buscar(elemento)){
            return false;
        }
        elementos.agregarFinal(elemento);
        return true;
    }

    public boolean eliminar(T elemento) {
        return elementos.eliminar(elemento);
    }
    public int tamanio(){
        return elementos.tamanio();
    }
    public boolean estaVacia(){
        return elementos.estaVacia();
    }
    public boolean buscar(T elemento) {
        for(T elem : elementos){
            if(elem.equals(elemento)){
                return true;
            }
        }
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        return elementos.iterator();
    }
}
