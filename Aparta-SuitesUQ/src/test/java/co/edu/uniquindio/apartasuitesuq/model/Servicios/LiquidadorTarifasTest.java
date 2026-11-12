package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LiquidadorTarifasTest {

    private LiquidadorTarifas liquidador;
    private Apartamento apartamento;

    @BeforeEach
    void preparar() {
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.agregarPeriodo(Temporada.MEDIA, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 14));
        calendario.agregarPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));
        liquidador = new LiquidadorTarifas(calendario);

        apartamento = new Apartamento("B201", "Suite Guadua", "B", 2, 2, 4, 80000);
        apartamento.setTarifa(Temporada.MEDIA, 100000);
        apartamento.setTarifa(Temporada.ALTA, 150000);
    }

    @Test
    void grupoSinDatosSeCobraComoAdultos() {
        Cotizacion c = liquidador.cotizar(apartamento,
                new Estancia(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3)), 3);
        assertEquals(2 * 3 * 80000, c.getTotal(), 0.001);
        assertEquals(2, c.getNumeroNoches());
        assertEquals(2, c.contarNochesEn(Temporada.BAJA));
    }

    @Test
    void estanciaQueCruzaTemporadasConNinoQueCumpleSeisYBebe() {
        ListaDoble<Ocupante> familia = new ListaDoble<>();
        familia.agregarFinal(new Ocupante("Papá", LocalDate.of(1990, 1, 1)));
        familia.agregarFinal(new Ocupante("Mamá", LocalDate.of(1992, 1, 1)));
        familia.agregarFinal(new Ocupante("Sofía", LocalDate.of(2020, 12, 14))); // cumple 6 el 14-dic
        familia.agregarFinal(new Ocupante("Bebé", LocalDate.of(2025, 1, 1)));

        Estancia estancia = new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 17));
        Cotizacion c = liquidador.cotizar(apartamento, estancia, familia, 4);

        // 13-dic Media: 2 pagan | 14-dic Media: 3 pagan | 15 y 16-dic Alta: 3 pagan
        double esperado = 2 * 100000 + 3 * 100000 + 3 * 150000 + 3 * 150000;
        assertEquals(esperado, c.getTotal(), 0.001);
        assertEquals(esperado * 0.30, c.getAnticipo(), 0.001);
        assertEquals(2, c.contarNochesEn(Temporada.MEDIA));
        assertEquals(2, c.contarNochesEn(Temporada.ALTA));

        NocheLiquidada primera = c.getDesglose().obtener(0);
        assertEquals(LocalDate.of(2026, 12, 13), primera.getNoche());
        assertEquals(2, primera.getPersonasQuePagan());
        assertEquals(2, primera.getPersonasSinCobro());
        assertEquals(3, c.getDesglose().obtener(1).getPersonasQuePagan());
    }

    @Test
    void elDesgloseTieneUnaLineaPorNocheYSumaElTotal() {
        Cotizacion c = liquidador.cotizar(apartamento,
                new Estancia(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 20)), 2);
        double suma = 0;
        int lineas = 0;
        for (NocheLiquidada noche : c.getDesglose()) {
            suma += noche.getSubtotal();
            lineas++;
        }
        assertEquals(10, lineas);
        assertEquals(c.getTotal(), suma, 0.001);
    }

    @Test
    void rechazaGruposQueNoCaben() {
        Estancia estancia = new Estancia(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2));
        assertThrows(CapacidadExcedidaException.class, () -> liquidador.cotizar(apartamento, estancia, 5));
    }

    @Test
    void liquidarAsignaElCostoTotalALaReserva() {
        Huesped titular = new Huesped("1094", "Ana Gómez", LocalDate.of(1990, 5, 20),
                "3001234567", "ana@correo.co", null);
        Reserva reserva = new Reserva("R1", titular, apartamento,
                LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 16),
                EstadoReserva.PENDIENTE, 0, Canal.PORTAL, 2, LocalDateTime.of(2026, 11, 12, 9, 0));

        double total = liquidador.liquidar(reserva);
        assertEquals(2 * 100000 + 2 * 150000, total, 0.001);
        assertEquals(total, reserva.getCostoTotal(), 0.001);
    }

    @Test
    void masOcupantesRegistradosQuePersonasLanzaExcepcion() {
        ListaDoble<Ocupante> dos = new ListaDoble<>();
        dos.agregarFinal(new Ocupante("A", LocalDate.of(1990, 1, 1)));
        dos.agregarFinal(new Ocupante("B", LocalDate.of(1990, 1, 1)));
        assertThrows(CapacidadExcedidaException.class,
                () -> Ocupante.contarPagantes(dos, 1, LocalDate.of(2026, 12, 1)));
    }
}
