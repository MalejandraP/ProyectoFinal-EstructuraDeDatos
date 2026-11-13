package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Cola;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.ReservaSolapadaException;

public class ServicioCanalExterno {
    private ServicioReservas servicioReservas;
    private Cola<SolicitudExterna> cola = new Cola<>();
    private ListaDoble<ConflictoCanal> conflictos = new ListaDoble<>();

    public ServicioCanalExterno(ServicioReservas servicioReservas) {
        this.servicioReservas = servicioReservas;
    }

    public void recibirSolicitud(SolicitudExterna solicitud){
        cola.encolar(solicitud);
    }

    public Reserva procesarSiguiente(){
        SolicitudExterna s = cola.desencolar();
        try{
            Reserva reserva = servicioReservas.crearReserva(s.getTitular(),s.getApartamento(),s.getEntrada(), s.getSalida(), s.getNumPersonas(), Canal.EXTERNO);
            s.marcarAceptada();
            return reserva;
        } catch (ReservaSolapadaException e) {
            rechazar(s, e.getMessage());
        } catch (FechasInvalidasException | CapacidadExcedidaException e){
            s.marcarRechazada(e.getMessage());
        }
        return null;
    }
    private void rechazar(SolicitudExterna s, String motivo) {
        s.marcarRechazada(motivo);
        conflictos.agregarFinal(new ConflictoCanal(s, motivo));
    }

    public ListaDoble<ConflictoCanal> getConflictos() {
        return conflictos;
    }


}
