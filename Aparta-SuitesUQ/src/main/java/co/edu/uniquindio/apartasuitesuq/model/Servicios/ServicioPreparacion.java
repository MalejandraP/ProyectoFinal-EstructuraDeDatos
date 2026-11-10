package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Grafo;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.TransicionEstadoInvalidaException;

import java.time.Duration;
import java.time.LocalDateTime;

public class ServicioPreparacion {

    private final Grafo<String> mapa;
    private final String nodoRecepcion;
    private final ColaPreparacion cola;
    private long minutosTotales;
    private int preparaciones;

    public ServicioPreparacion(Grafo<String> mapa, String nodoRecepcion, ColaPreparacion cola) {
        this.mapa = mapa;
        this.nodoRecepcion = nodoRecepcion;
        this.cola = cola;
    }


    public void checkIn(Apartamento a) {
        if (a.getEstado() != EstadoApartamento.PREPARADO) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede hacer check-in: " + a.getCodigo() + " está " + a.getEstado());
        }
        a.setEstado(EstadoApartamento.OCUPADO);
    }


    public void checkOut(Apartamento a, LocalDateTime ahora) {
        if (a.getEstado() != EstadoApartamento.OCUPADO) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede hacer check-out: " + a.getCodigo() + " está " + a.getEstado());
        }
        a.setEstado(EstadoApartamento.PENDIENTEDEPREPARACION);
        cola.encolar(a, ahora);
    }


    public ColaPreparacion.Item iniciarSiguiente() {
        ColaPreparacion.Item item;
        do {
            item = cola.siguiente(); // lanza EstructuraVaciaException si no queda nadie
        } while (item.getApartamento().getEstado() != EstadoApartamento.PENDIENTEDEPREPARACION);
        item.getApartamento().setEstado(EstadoApartamento.ENPREPARACION);
        return item;
    }

    public void terminarPreparacion(ColaPreparacion.Item item, LocalDateTime ahora) {
        Apartamento a = item.getApartamento();
        if (a.getEstado() != EstadoApartamento.ENPREPARACION) {
            throw new TransicionEstadoInvalidaException(a.getCodigo() + " no está en preparación");
        }
        a.setEstado(EstadoApartamento.PREPARADO);
        minutosTotales += Duration.between(item.getIngreso(), ahora).toMinutes();
        preparaciones++;
    }

    public void ponerFueraDeServicio(Apartamento a, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo");
        }
        if (a.getEstado() == EstadoApartamento.OCUPADO) {
            throw new TransicionEstadoInvalidaException("No se puede sacar de servicio un apartamento ocupado");
        }
        //a.setMotivoFueraServicio(motivo);
        a.setEstado(EstadoApartamento.FUERADESERVICIO);
    }

    public void volverAServicio(Apartamento a, LocalDateTime ahora) {
        if (a.getEstado() != EstadoApartamento.FUERADESERVICIO) {
            throw new TransicionEstadoInvalidaException(a.getCodigo() + " no está fuera de servicio");
        }
        //a.setMotivoFueraServicio(null);
        a.setEstado(EstadoApartamento.PENDIENTEDEPREPARACION);
        cola.encolar(a, ahora);
    }


    public Recorrido<String> rutaSugerida() {
        Recorrido<String> codigos = new Recorrido<>();
        for (ColaPreparacion.Item item : cola) {
            if (item.getApartamento().getEstado() == EstadoApartamento.PENDIENTEDEPREPARACION) {
                codigos.agregar(item.getApartamento().getCodigo());
            }
        }
        return mapa.rutaEnOrden(nodoRecepcion, codigos);
    }


    public double tiempoMedioPreparacionMin() {
        return preparaciones == 0 ? 0 : (double) minutosTotales / preparaciones;
    }
}
