package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDateTime;

public class ConflictoCanal {
    private final SolicitudExterna solicitud;
    private final LocalDateTime fechaRegistro;
    private final String motivo;

    public ConflictoCanal(SolicitudExterna solicitud, String motivo) {
        this.solicitud = solicitud;
        this.motivo = motivo;
        this.fechaRegistro = LocalDateTime.now();
    }

    public SolicitudExterna getSolicitud() { return solicitud; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getMotivo() { return motivo; }

    @Override
    public String toString() {
        return "Conflicto [" + fechaRegistro + "] apto " + solicitud.getApartamento().getCodigo()
                + " " + solicitud.getEntrada() + " a " + solicitud.getSalida()
                + " - " + motivo;
    }
}
