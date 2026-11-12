package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.EstadoApartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Grafo;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;

import java.time.Duration;
import java.time.LocalDateTime;

public class ServicioPreparacion {

    private final Grafo<String> mapa;
    private final String nodoRecepcion;
    private final ColaPreparacion cola;
    private long minutosTotales;
    private int preparaciones;
    private final RegistroFueraDeServicio fueraDeServicio = new RegistroFueraDeServicio();

    public ServicioPreparacion(Grafo<String> mapa, String nodoRecepcion, ColaPreparacion cola) {
        this.mapa = mapa;
        this.nodoRecepcion = nodoRecepcion;
        this.cola = cola;
    }


    public void checkIn(Apartamento a) {
        if (estadoDe(a) != EstadoApartamento.PREPARADO) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede hacer check-in: " + a.getCodigo() + " está " + estadoDe(a));
        }
        cambiarEstado(a, EstadoApartamento.OCUPADO);
    }


    public void checkOut(Apartamento a, LocalDateTime ahora) {
        if (estadoDe(a) != EstadoApartamento.OCUPADO) {
            throw new TransicionEstadoInvalidaException(
                    "No se puede hacer check-out: " + a.getCodigo() + " está " + estadoDe(a));
        }
        cambiarEstado(a, EstadoApartamento.PENDIENTEDEPREPARACION);
        cola.encolar(a, ahora);
    }


    public ColaPreparacion.Item iniciarSiguiente() {
        ColaPreparacion.Item item;
        do {
            item = cola.siguiente();
        } while (estadoDe(item.getApartamento()) != EstadoApartamento.PENDIENTEDEPREPARACION);
        cambiarEstado(item.getApartamento(), EstadoApartamento.ENPREPARACION);
        return item;
    }

    public void terminarPreparacion(ColaPreparacion.Item item, LocalDateTime ahora) {
        Apartamento a = item.getApartamento();
        if (estadoDe(a) != EstadoApartamento.ENPREPARACION) {
            throw new TransicionEstadoInvalidaException(a.getCodigo() + " no está en preparación");
        }
        cambiarEstado(a, EstadoApartamento.PREPARADO);
        minutosTotales += Duration.between(item.getIngreso(), ahora).toMinutes();
        preparaciones++;
    }

    public void ponerFueraDeServicio(Apartamento a, String motivo) {
        ponerFueraDeServicio(a, motivo, LocalDateTime.now());
    }

    public void ponerFueraDeServicio(Apartamento a, String motivo, LocalDateTime ahora) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo");
        }
        if (estadoDe(a) == EstadoApartamento.OCUPADO) {
            throw new TransicionEstadoInvalidaException("No se puede sacar de servicio un apartamento ocupado");
        }
        cambiarEstado(a, EstadoApartamento.FUERADESERVICIO);
        fueraDeServicio.abrir(a.getCodigo(), motivo.trim(), ahora);
    }

    public void volverAServicio(Apartamento a, LocalDateTime ahora) {
        if (estadoDe(a) != EstadoApartamento.FUERADESERVICIO) {
            throw new TransicionEstadoInvalidaException(a.getCodigo() + " no está fuera de servicio");
        }
        fueraDeServicio.cerrar(a.getCodigo(), ahora);
        cambiarEstado(a, EstadoApartamento.PENDIENTEDEPREPARACION);
        cola.encolar(a, ahora);
    }


    public Recorrido<String> rutaSugerida() {
        Recorrido<String> codigos = new Recorrido<>();
        for (ColaPreparacion.Item item : cola) {
            if (estadoDe(item.getApartamento()) == EstadoApartamento.PENDIENTEDEPREPARACION) {
                codigos.agregar(item.getApartamento().getCodigo());
            }
        }
        return mapa.rutaEnOrden(nodoRecepcion, codigos);
    }


    public double tiempoMedioPreparacionMin() {
        return preparaciones == 0 ? 0 : (double) minutosTotales / preparaciones;
    }


    private EstadoApartamento estadoDe(Apartamento a) {
        return a.getEstado();
    }

    private void cambiarEstado(Apartamento a, EstadoApartamento nuevo) {
        a.setEstado(nuevo);
    }


    public String motivoFueraDeServicio(Apartamento a) {
        return fueraDeServicio.motivoDe(a.getCodigo());
    }

    public RegistroFueraDeServicio historialFueraDeServicio() {
        return fueraDeServicio;
    }


}
