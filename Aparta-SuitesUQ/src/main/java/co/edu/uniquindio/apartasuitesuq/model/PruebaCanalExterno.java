package co.edu.uniquindio.apartasuitesuq.model;

import java.time.LocalDate;

public class PruebaCanalExterno {

    public static void main(String[] args) {
        ServicioReservas servicioReservas = new ServicioReservas();
        ServicioCanalExterno canal = new ServicioCanalExterno(servicioReservas);


        // --- Datos de prueba (AJUSTAR constructores) ---
        Huesped h1 = new Huesped("1001", "Ana Gómez","3224789051", "pony@boy");
        Huesped h2 = new Huesped("1002", "Luis Pérez","3144803768","omar@gmail.com");
        Huesped h3 = new Huesped("1003", "Marta Ruiz", "3105677856","sabraDios@gmail.com");

        Apartamento a101 = new Apartamento("A101", "Suite Café","2",2, 2 , 4,30000/* capacidad */);
        Apartamento a102 = new Apartamento("A102", "Suite Palma", "4",4,3,4,50000 /* capacidad */);

        LocalDate hoy = LocalDate.now();

        // --- Solicitudes (llegan en este orden) ---
        // S1: A101 días +10 a +13           -> debe ACEPTARSE
        // S2: A101 días +12 a +15           -> choca con S1 -> CONFLICTO DE CANAL
        // S3: A101 días +13 a +15           -> empieza el día que S1 sale (no se cobra esa noche) -> ACEPTARSE
        // S4: A101 días +20 a +22, 6 pers.  -> excede capacidad -> RECHAZADA (no es conflicto de canal)
        // S5: A102 días +10 a +12           -> otro apartamento -> ACEPTARSE
        SolicitudExterna s1 = new SolicitudExterna("EXT-1", a101, hoy.plusDays(10), hoy.plusDays(13), 2,h1);
        SolicitudExterna s2 = new SolicitudExterna("EXT-2", a101, hoy.plusDays(12), hoy.plusDays(15), 2, h2);
        SolicitudExterna s3 = new SolicitudExterna("EXT-3", a101, hoy.plusDays(13), hoy.plusDays(15), 3, h3);
        SolicitudExterna s4 = new SolicitudExterna("EXT-4", a101, hoy.plusDays(20), hoy.plusDays(22), 6, h1);
        SolicitudExterna s5 = new SolicitudExterna("EXT-5", a102, hoy.plusDays(10), hoy.plusDays(12), 2, h2);

        SolicitudExterna[] solicitudes = { s1, s2, s3, s4, s5 };
        for (SolicitudExterna s : solicitudes) {
            canal.recibirSolicitud(s);
        }

        // --- Procesar la cola en orden de llegada ---
        System.out.println("=== Procesando cola (FIFO) ===");
        for (SolicitudExterna s : solicitudes) {
            Reserva r = canal.procesarSiguiente();
            System.out.println(s.getIdExterno() + " -> " + s.getEstado()
                    + (r != null ? " (reserva " + r.getCodigoReserva() + ")" : "")
                    + (s.getMotivoRechazo() != null ? " | motivo: " + s.getMotivoRechazo() : ""));
        }

        // --- Conflictos registrados ---
        System.out.println("\n=== Conflictos de canal ===");
        for (ConflictoCanal c : canal.getConflictos()) {
            System.out.println(c);
        }

        // --- Verificaciones ---
        System.out.println("\n=== Verificaciones ===");
        comprobar("S1 aceptada", s1.getEstado() == EstadoSolicitud.ACEPTADA);
        comprobar("S2 rechazada por choque", s2.getEstado() == EstadoSolicitud.RECHAZADA);
        comprobar("S3 aceptada (noche de salida no choca)", s3.getEstado() == EstadoSolicitud.ACEPTADA);
        comprobar("S4 rechazada por capacidad", s4.getEstado() == EstadoSolicitud.RECHAZADA);
        comprobar("S5 aceptada", s5.getEstado() == EstadoSolicitud.ACEPTADA);
        comprobar("Solo S2 es conflicto de canal (hay 1)", contar(canal) == 1);

        // --- Cola vacía: debe lanzar la excepción propia ---
        System.out.println("\n=== Cola vacía ===");
        try {
            canal.procesarSiguiente();
            comprobar("Debía lanzar EstructuraVaciaException", false);
        } catch (IllegalArgumentException e) {
            comprobar("Lanza EstructuraVaciaException al procesar con cola vacía", true);
        }
    }

    private static int contar(ServicioCanalExterno canal) {
        int n = 0;
        for (ConflictoCanal c : canal.getConflictos()) {
            n++;
        }
        return n;
    }

    private static void comprobar(String descripcion, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}