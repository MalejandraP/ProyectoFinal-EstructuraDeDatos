package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDateTime;

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
            Reserva reserva = servicioReservas.crearReserva(s.getTitular(),s.getCodigoApartamento(),s.getEntrada(), s.getSalida(), s.getNumPersonas(), Canal.EXTERNO);
            s.marcarAceptada();
            return reserva;
        } catch (IllegalArgumentException e) {
            rechazar(s, e.getMessage());
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
