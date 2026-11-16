package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ColaPreparacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Métodos de la fachada que usan las pantallas de reservas, preparación, mapa y administración. */
class FachadaPantallasTest {

    private SGA sga;
    private LocalDate hoy;

    @BeforeEach
    void preparar() throws IOException {
        sga = new SGA();
        sga.cargarEscenarioDeEjemplo();
        hoy = sga.hoy();
    }

    private static int contar(Iterable<?> datos) {
        int n = 0;
        for (Object ignored : datos) {
            n++;
        }
        return n;
    }

    @Test
    void reservasOrdenadasPorCodigoNumerico() {
        ListaDoble<Reserva> ordenadas = sga.reservasOrdenadasPorCodigo();
        assertEquals("RES-1", ordenadas.obtener(0).getCodigoReserva());
        assertEquals("RES-2", ordenadas.obtener(1).getCodigoReserva());
        assertEquals("RES-11", ordenadas.obtener(10).getCodigoReserva(), "RES-10 y RES-11 van después de RES-9");
    }

    @Test
    void grupoGrandeUsaLaMenorCantidadDeApartamentos() {
        LocalDate entrada = hoy.plusDays(60);
        ListaDoble<Apartamento> combinacion = sga.asignarGrupoGrande(entrada, entrada.plusDays(2), 10);
        assertEquals(2, contar(combinacion), "B101 y B201 alojan 6 + 6");
        int capacidad = 0;
        for (Apartamento apartamento : combinacion) {
            capacidad += apartamento.getCapacidadMaxima();
        }
        assertTrue(capacidad >= 10);

        Huesped titular = sga.buscarHuesped("1094000001");
        ListaDoble<Reserva> creadas = sga.reservarGrupo(titular, combinacion, entrada, entrada.plusDays(2), 10, Canal.DIRECTA);
        assertEquals(2, contar(creadas));
        int personas = 0;
        for (Reserva reserva : creadas) {
            personas += reserva.getNumeroPersonas();
        }
        assertEquals(10, personas);
        assertTrue(sga.asignarGrupoGrande(entrada, entrada.plusDays(2), 200).estaVacia(), "no alcanza la capacidad");
    }

    @Test
    void preparacionRegistraElTiempoPorApartamento() {
        ColaPreparacion.Item item = sga.iniciarSiguientePreparacion();
        assertEquals(1, contar(sga.getEnPreparacion()));
        sga.terminarPreparacion(item);
        assertTrue(sga.getEnPreparacion().estaVacia());
        RegistroPreparacion registro = sga.getPreparacionesTerminadas().obtener(0);
        assertEquals(item.getApartamento().getCodigo(), registro.getCodigoApartamento());
        assertTrue(registro.getMinutos() >= 0);
        assertEquals(EstadoApartamento.PREPARADO, item.getApartamento().getEstadoOperativo());

        while (!sga.getColaPreparacion().estaVacia()) {
            sga.iniciarSiguientePreparacion();
        }
        assertThrows(EstructuraVaciaException.class, sga::iniciarSiguientePreparacion);
    }

    @Test
    void mapaPasillosInalcanzablesYRecorridos() {
        assertEquals(12, contar(sga.getPasillos()));
        assertTrue(sga.inalcanzables().estaVacio());
        sga.cerrarPasillo("ESC_A", "ESC_B");
        Recorrido<String> inalcanzables = sga.inalcanzables();
        assertEquals(4, inalcanzables.tamano(), "ESC_B, B101, B102 y B201 quedan aislados");
        boolean cerrado = false;
        for (Pasillo pasillo : sga.getPasillos()) {
            cerrado |= pasillo.isCerrado();
        }
        assertTrue(cerrado);
        sga.abrirPasillo("ESC_A", "ESC_B");
        assertTrue(sga.inalcanzables().estaVacio());

        assertEquals(12, sga.recorridoBfs().tamano());
        assertEquals(12, sga.recorridoDfs().tamano());
        assertEquals("REC", sga.recorridoBfs().obtener(0));
        assertEquals(5, sga.recorridoBloque("A").tamano());
        assertEquals(3, sga.recorridoBloque("B").tamano());
    }

    @Test
    void colaEnOrdenDePrioridadYCalendario() {
        ListaDoble<ColaPreparacion.Item> cola = sga.colaEnOrden();
        assertEquals(3, contar(cola));
        assertEquals(1, cola.obtener(0).getPrioridad());
        assertEquals(1, cola.obtener(1).getPrioridad());
        assertEquals("B201", cola.obtener(2).getApartamento().getCodigo(), "sin entrada hoy: prioridad 2");
        assertTrue(cola.obtener(0).getHoraClave().isBefore(cola.obtener(1).getHoraClave()));

        Apartamento a102 = sga.buscarApartamento("A102");
        assertEquals("RES-2", sga.ocupacionEn(a102, hoy).getCodigoReserva());
        assertNull(sga.ocupacionEn(a102, hoy.plusDays(1)), "la noche de salida queda libre");
        assertNull(sga.ocupacionEn(sga.buscarApartamento("A202"), hoy.minusDays(9)), "RES-7 está cancelada");
        assertTrue(sga.bloqueadaEn(sga.buscarApartamento("A202"), hoy.plusDays(3)));
        assertFalse(sga.bloqueadaEn(sga.buscarApartamento("A202"), hoy.minusDays(1)));
    }

    @Test
    void modificarCatalogos() {
        sga.modificarApartamento("A101", "Suite Cafetal Renovada", "A", 1, 2, 5, "Con terraza");
        assertEquals(5, sga.buscarApartamento("A101").getCapacidadMaxima());
        sga.modificarHuesped("1094000001", "Ana María Gómez", LocalDate.of(1990, 5, 20), "3000000000",
                "ana@nuevo.co", null);
        assertEquals("Ana María Gómez", sga.buscarHuesped("1094000001").getNombre());
        sga.modificarEmpleado("R1", "Laura P.", "nueva");
        assertNotNull(sga.autenticar("R1", "nueva"));
        assertThrows(IllegalArgumentException.class, () -> sga.modificarEmpleado("X", "Y", null));
    }

    @Test
    void anticipoDescuentaAbonosPrevios() {
        Reserva res6 = sga.buscarReserva("RES-6");
        sga.registrarPago(res6, 10000, "Abono");
        double pagado = sga.pagarAnticipo(res6);
        assertEquals(res6.getValorAnticipo() - 10000, pagado, 0.001);
        assertEquals(EstadoReserva.CONFIRMADA, res6.getEstado());
    }
}
