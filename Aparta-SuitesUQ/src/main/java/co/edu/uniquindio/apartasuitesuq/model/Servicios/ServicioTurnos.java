package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaCircular;
import co.edu.uniquindio.apartasuitesuq.model.Recepcionista;

public class ServicioTurnos {
    private ListaCircular<Recepcionista> turnos = new ListaCircular<>();

    public boolean registrar(Recepcionista r)  {
        if(!turnos.estaVacia() && turnos.buscar(r)){
            return false;
        }
        turnos.agregarFinal(r);
        return true;
    }
    public boolean retirar(Recepcionista r) {
        return turnos.eliminarDato(r);
    }
    public Recepcionista turnoActual() {
        return turnos.datoActual();
    }
    public Recepcionista pasarTurno() {
        return turnos.avanzar();
    }
    public boolean hayRecepcionistas() {
        return !turnos.estaVacia();
    }
    public ListaCircular<Recepcionista> getTurnos() {
        return turnos;
    }
}
