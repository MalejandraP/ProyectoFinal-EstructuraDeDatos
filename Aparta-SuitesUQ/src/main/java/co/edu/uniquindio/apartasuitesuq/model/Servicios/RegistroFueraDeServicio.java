package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.ListaDoble;

import java.time.LocalDateTime;

public class RegistroFueraDeServicio implements Iterable<RegistroFueraDeServicio.Registro> {

    public static class Registro {
        private final String codigoApartamento;
        private final String motivo;
        private final LocalDateTime desde;
        private LocalDateTime hasta; // null mientras siga fuera de servicio

        Registro(String codigoApartamento, String motivo, LocalDateTime desde) {
            this.codigoApartamento = codigoApartamento;
            this.motivo = motivo;
            this.desde = desde;
        }

        public String getCodigoApartamento() { return codigoApartamento; }
        public String getMotivo() { return motivo; }
        public LocalDateTime getDesde() { return desde; }
        public LocalDateTime getHasta() { return hasta; }
        public boolean estaAbierto() { return hasta == null; }

        @Override
        public String toString() {
            return codigoApartamento + ": " + motivo + (estaAbierto() ? " (vigente)" : " (hasta " + hasta + ")");
        }
    }

    private final ListaDoble<Registro> registros = new ListaDoble<>();

    public void abrir(String codigo, String motivo, LocalDateTime desde) {
        registros.agregarFinal(new Registro(codigo, motivo, desde));
    }


    public boolean cerrar(String codigo, LocalDateTime hasta) {
        Registro r = vigente(codigo);
        if (r == null) {
            return false;
        }
        r.hasta = hasta;
        return true;
    }


    public Registro vigente(String codigo) {
        for (Registro r : registros) {
            if (r.estaAbierto() && r.codigoApartamento.equalsIgnoreCase(codigo)) {
                return r;
            }
        }
        return null;
    }


    public String motivoDe(String codigo) {
        Registro r = vigente(codigo);
        return r == null ? null : r.motivo;
    }

    @Override
    public java.util.Iterator<Registro> iterator() {
        return registros.iterator();
    }
}
