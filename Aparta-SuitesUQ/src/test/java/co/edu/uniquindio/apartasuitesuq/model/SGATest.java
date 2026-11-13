package co.edu.uniquindio.apartasuitesuq.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la fachada. Sin temporadas definidas, todas las noches son temporada BAJA. */
class SGATest {

    private static final LocalDate HOY = LocalDate.now();

    private SGA sga;
    private Huesped ana;

    private static int contar(Iterable<?> datos) {
        int n = 0;
        for (Object ignored : datos) {
            n++;
        }
        return n;
    }

    @BeforeEach
    void preparar() {
        sga = new SGA();
        sga.registrarApartamento(new Apartamento("A101", "Suite Cafetal", "A", 1, 2, 4, 90000));
        sga.registrarApartamento(new Apartamento("A102", "Estudio Guadua", "A", 1, 1, 2, 70000));
        sga.registrarApartamento(new Apartamento("B101", "Casa Bahareque", "B", 1, 3, 6, 80000));
        ana = new Huesped("1094", "Ana Gómez", LocalDate.of(1990, 5, 20), "3001234567", "ana@correo.co", null);
        sga.registrarHuesped(ana);
        sga.registrarEmpleado(new Administrador("Gloria", "admin", "admin123"));
        sga.registrarEmpleado(new Recepcionista("Laura", "R1", "r1"));
    }

    @Test
    void noAdmiteCodigosNiDocumentosRepetidos() {
        assertThrows(IllegalArgumentException.class,
                () -> sga.registrarApartamento(new Apartamento("a101", "Otra", "A", 1, 1, 2, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> sga.registrarHuesped(new Huesped("1094", "Otra", "300", "o@x.co")));
        assertSame(ana, sga.buscarHuesped("1094"));
        assertNotNull(sga.buscarApartamento("b101"));
    }

    @Test
    void autenticaEmpleadosYRotaTurnos() {
        assertEquals(Rol.ADMINISTRADOR, sga.autenticar("admin", "admin123").getRol());
        assertNull(sga.autenticar("admin", "mala"));
        assertNull(sga.autenticar("nadie", "x"));
        assertEquals("R1", sga.turnoActual().getId());
    }

    @Test
    void reservaPorPortalSeLiquidaYSeConfirmaConElAnticipo() {
        ListaDoble<Ocupante> acompanantes = new ListaDoble<>();
        acompanantes.agregarFinal(new Ocupante("Bebé", HOY.minusYears(2)));
        Reserva r = sga.crearReserva(ana, "A101", HOY.plusDays(10), HOY.plusDays(12), acompanantes, 3, Canal.PORTAL);

        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(3, r.getNumeroPersonas());
        assertEquals(2 * 2 * 90000, r.getCostoTotal(), 0.001, "2 noches x 2 pagantes (el bebé no paga)");
        assertEquals(1, contar(r.getFolio().getMovimientos()), "el valor de la estancia queda cargado al folio");

        sga.pagarAnticipo(r);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertEquals(r.getValorAnticipo(), r.getFolio().totalPago(), 0.001);
        assertThrows(TransicionEstadoInvalidaException.class, () -> sga.pagarAnticipo(r));
    }

    @Test
    void noPermiteReservasSolapadasHastaQueSeCancele() {
        Reserva r = sga.crearReserva(ana, "A101", HOY.plusDays(5), HOY.plusDays(8), null, 2, Canal.DIRECTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertThrows(ReservaSolapadaException.class,
                () -> sga.crearReserva(ana, "A101", HOY.plusDays(7), HOY.plusDays(9), null, 2, Canal.PORTAL));
        // salir y entrar el mismo día no es solapamiento
        assertNotNull(sga.crearReserva(ana, "A101", HOY.plusDays(8), HOY.plusDays(9), null, 2, Canal.PORTAL));

        sga.cancelarReserva(r);
        assertNotNull(sga.crearReserva(ana, "A101", HOY.plusDays(6), HOY.plusDays(7), null, 1, Canal.DIRECTA));
    }

    @Test
    void masOcupantesQuePersonasNoDejaReservaGuardada() {
        ListaDoble<Ocupante> dos = new ListaDoble<>();
        dos.agregarFinal(new Ocupante("A", LocalDate.of(1990, 1, 1)));
        dos.agregarFinal(new Ocupante("B", LocalDate.of(1990, 1, 1)));
        assertThrows(CapacidadExcedidaException.class,
                () -> sga.crearReserva(ana, "A101", HOY.plusDays(1), HOY.plusDays(2), dos, 2, Canal.DIRECTA));
        assertTrue(sga.getReservas().estaVacia());
    }

    @Test
    void buscarDisponiblesFiltraYOrdena() {
        sga.crearReserva(ana, "A101", HOY.plusDays(1), HOY.plusDays(4), null, 2, Canal.DIRECTA);
        sga.ponerFueraDeServicio("B101", "Fumigación");

        ListaDoble<Apartamento> libres = sga.buscarDisponibles(HOY.plusDays(2), HOY.plusDays(3), 2, null);
        assertEquals(1, contar(libres));
        assertEquals("A102", libres.obtener(0).getCodigo());

        sga.volverAServicio("B101");
        Comparator<Apartamento> porPrecio = Comparator.comparingDouble(Apartamento::getTarifaBaseNoche);
        ListaDoble<Apartamento> ordenados = sga.buscarDisponibles(HOY.plusDays(10), HOY.plusDays(11), 2, porPrecio);
        assertEquals("A102", ordenados.obtener(0).getCodigo());
        assertEquals("B101", ordenados.obtener(1).getCodigo());
        assertEquals("A101", ordenados.obtener(2).getCodigo());

        assertTrue(sga.buscarDisponibles(HOY.plusDays(10), HOY.plusDays(11), 7, null).estaVacia());
    }

    @Test
    void canalExternoProcesaEnOrdenYRegistraConflictos() {
        sga.crearReserva(ana, "A101", HOY.plusDays(3), HOY.plusDays(5), null, 2, Canal.DIRECTA);
        Huesped carlos = new Huesped("2000", "Carlos", LocalDate.of(1985, 1, 1), "310", "c@x.co", null);
        sga.recibirSolicitudExterna(new SolicitudExterna("EXT-1", sga.buscarApartamento("A101"),
                HOY.plusDays(4), HOY.plusDays(6), 2, carlos));
        sga.recibirSolicitudExterna(new SolicitudExterna("EXT-2", sga.buscarApartamento("A102"),
                HOY.plusDays(4), HOY.plusDays(6), 2, carlos));
        assertNotNull(sga.buscarHuesped("2000"), "el titular externo se registra");

        assertNull(sga.procesarSiguienteSolicitud());
        Reserva aceptada = sga.procesarSiguienteSolicitud();
        assertEquals(Canal.EXTERNO, aceptada.getCanal());
        assertEquals(2 * 2 * 70000, aceptada.getCostoTotal(), 0.001);
        assertEquals("EXT-1", sga.getConflictosCanal().obtener(0).getSolicitud().getIdExterno());
        assertThrows(EstructuraVaciaException.class, sga::procesarSiguienteSolicitud);
    }

    @Test
    void checkInYCheckOutMuevenElApartamentoALaColaDePreparacion() {
        Reserva r = sga.crearReserva(ana, "A102", HOY, HOY.plusDays(1), null, 1, Canal.DIRECTA);
        Apartamento a102 = sga.buscarApartamento("A102");

        sga.checkIn(r);
        assertEquals(EstadoReserva.ENCURSO, r.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, a102.getEstadoOperativo());

        sga.checkOut(r);
        assertEquals(EstadoApartamento.PENDIENTEDEPREPARACION, a102.getEstadoOperativo());
        assertEquals(1, sga.getColaPreparacion().tamano());
        assertSame(a102, sga.iniciarSiguientePreparacion().getApartamento());
    }
}
