package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración sobre la fachada {@link SGA}:
 * catálogos jerárquicos, búsqueda ordenada de disponibilidad y exención
 * tarifaria para acompañantes menores de 6 años.
 */
class GestionReservasP2Test {

    private SGA sga;
    private Apartamento a101;
    private Apartamento a102;
    private Apartamento a201;
    private Huesped titular;

    @BeforeEach
    void setUp() {
        sga = new SGA();
        a101 = new Apartamento("A101", "Suite Cafetal", "A", 1, 2, 4, 300_000.0);
        a102 = new Apartamento("A102", "Estudio Guadua", "A", 1, 1, 2, 150_000.0);
        a201 = new Apartamento("A201", "Ático Yarumo", "B", 2, 3, 6, 450_000.0);

        sga.registrarApartamento(a201);
        sga.registrarApartamento(a101);
        sga.registrarApartamento(a102);

        titular = new Huesped("1094000001", "Ana Gómez", "3101112233", "ana@correo.com", LocalDate.of(1990, 5, 10));
        sga.registrarHuesped(titular);
    }

    @Test
    void catalogosJerarquicosBuscanYBloqueanEliminacionConReservasActivas() {
        assertEquals("Suite Cafetal", sga.buscarApartamento("a101").getNombre());
        assertEquals("Ana Gómez", sga.buscarHuesped("1094000001").getNombre());

        Recepcionista r1 = new Recepcionista("Laura Pérez", "R1", "r1");
        sga.registrarEmpleado(r1);
        assertNotNull(sga.autenticar("R1", "r1"));
        assertTrue(sga.eliminarEmpleado("R1"));
        assertNull(sga.buscarEmpleado("R1"));

        LocalDate entrada = sga.hoy().plusDays(5);
        LocalDate salida = entrada.plusDays(2);
        Reserva res = sga.crearReserva(titular, "A101", entrada, salida, null, 2, Canal.DIRECTA);

        // No debe permitir eliminar apartamento ni huésped con reserva activa
        assertThrows(IllegalStateException.class, () -> sga.eliminarApartamento("A101"));
        assertThrows(IllegalStateException.class, () -> sga.eliminarHuesped("1094000001"));

        // Al cancelar la reserva sí permite eliminar
        sga.cancelarReserva(res);
        assertTrue(sga.eliminarApartamento("A101"));
        assertTrue(sga.eliminarHuesped("1094000001"));
    }

    @Test
    void buscarDisponiblesOrdenaPorCriterioYExcluyeOcupadosOFueraDeServicio() {
        LocalDate entrada = sga.hoy().plusDays(15);
        LocalDate salida = entrada.plusDays(2);

        // Orden por precio ascendente: A102 (150k), A101 (300k), A201 (450k)
        List<String> porPrecio = codigos(sga.buscarDisponibles(entrada, salida, 2, OrdenApartamentos.porPrecio()));
        assertEquals(List.of("A102", "A101", "A201"), porPrecio);

        // Orden por capacidad descendente: A201 (6), A101 (4), A102 (2)
        List<String> porCapacidad = codigos(sga.buscarDisponibles(entrada, salida, 2, OrdenApartamentos.porCapacidad()));
        assertEquals(List.of("A201", "A101", "A102"), porCapacidad);

        // Reservar A102 en esas fechas debe excluirlo de disponibles
        sga.crearReserva(titular, "A102", entrada, salida, null, 2, Canal.DIRECTA);
        List<String> trasReserva = codigos(sga.buscarDisponibles(entrada, salida, 2, OrdenApartamentos.porPrecio()));
        assertEquals(List.of("A101", "A201"), trasReserva);

        // Poner A201 fuera de servicio también lo excluye
        sga.ponerFueraDeServicio("A201", "Mantenimiento hidráulico");
        List<String> trasBloqueo = codigos(sga.buscarDisponibles(entrada, salida, 2, OrdenApartamentos.porPrecio()));
        assertEquals(List.of("A101"), trasBloqueo);
    }

    @Test
    void crearReservaConAcompananteMenorDe6AniosAplicaExencionTarifaria() {
        LocalDate entrada = sga.hoy().plusDays(20);
        LocalDate salida = entrada.plusDays(2); // 2 noches

        // Titular adulto + niño de 4 años + adulto acompañante = 3 personas, pero solo 2 pagantes
        ListaDoble<Ocupante> acompanantes = new ListaDoble<>();
        acompanantes.agregarFinal(new Ocupante("Mateo Gómez", entrada.minusYears(4)));
        acompanantes.agregarFinal(new Ocupante("Carlos Ruiz", entrada.minusYears(30)));

        Reserva reserva = sga.crearReserva(titular, "A101", entrada, salida, acompanantes, 3, Canal.PORTAL);

        assertEquals(2, reserva.contarPagantesEn(entrada));
        // 2 noches * 2 pagantes * 300.000 = 1.200.000 (el menor de 4 años no paga)
        assertEquals(1_200_000.0, reserva.getCostoTotal(), 0.01);
        assertEquals(360_000.0, reserva.getValorAnticipo(), 0.01);
    }

    private static List<String> codigos(ListaDoble<Apartamento> lista) {
        List<String> res = new ArrayList<>();
        for (Apartamento a : lista) {
            res.add(a.getCodigo());
        }
        return res;
    }
}