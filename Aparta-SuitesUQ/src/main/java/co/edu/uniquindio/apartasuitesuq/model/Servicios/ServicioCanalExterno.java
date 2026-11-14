package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.*;
import co.edu.uniquindio.apartasuitesuq.model.ConflictoCanal;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Cola;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.ReservaSolapadaException;

/**
 * Servicio encargado de gestionar y procesar las solicitudes de reserva provenientes de canales externos.
 * Utiliza una estructura {@link Cola} para atender las peticiones en estricto orden de llegada (FIFO),
 * delegando la creación de las reservas a {@link ServicioReservas} y registrando en una
 * {@link ListaDoble} aquellos casos que generen conflictos de disponibilidad.
 */
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
    /**
     * Extrae y procesa la siguiente solicitud pendiente en la cola (la más antigua).
     * <ul>
     *   <li>Si la reserva es válida, la crea bajo el canal {@code Canal.EXTERNO}, marca la solicitud
     *       como aceptada y retorna la {@link Reserva} generada.</li>
     *   <li>Si existe un solapamiento de fechas ({@link ReservaSolapadaException}), rechaza la solicitud
     *       y registra el incidente en la lista de conflictos.</li>
     *   <li>Si las fechas son inválidas o se excede la capacidad ({@link FechasInvalidasException},
     *       {@link CapacidadExcedidaException}), marca la solicitud como rechazada sin generar conflicto.</li>
     * </ul>
     *
     * @return La {@link Reserva} creada si la solicitud fue aceptada, o {@code null} si fue rechazada.
     */
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
    /**
     * Marca una solicitud externa como rechazada y registra el evento en la lista de conflictos del canal.
     *
     * @param s La solicitud externa que provocó el conflicto.
     * @param motivo Descripción del motivo del rechazo (mensaje de la excepción).
     */
    private void rechazar(SolicitudExterna s, String motivo) {
        s.marcarRechazada(motivo);
        conflictos.agregarFinal(new ConflictoCanal(s, motivo));
    }
    /**
     * Obtiene la lista de todos los conflictos de solapamiento registrados durante el procesamiento
     * de solicitudes externas.
     *
     * @return Una {@link ListaDoble} con los objetos {@link ConflictoCanal} generados.
     */
    public ListaDoble<ConflictoCanal> getConflictos() {
        return conflictos;
    }


}
