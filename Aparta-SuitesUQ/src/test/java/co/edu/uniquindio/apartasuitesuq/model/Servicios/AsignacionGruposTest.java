package co.edu.uniquindio.apartasuitesuq.model.Servicios;

import co.edu.uniquindio.apartasuitesuq.model.Apartamento;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionGruposTest {

    // AJUSTA este constructor a tu clase Apartamento
    private Apartamento apto(String codigo, int capacidad) {
        return new Apartamento(codigo, "Las Heliconias", "BloqueA", 1, 1, capacidad, 130000);
    }

    private ListaDoble<Apartamento> lista(Apartamento... aptos) {
        ListaDoble<Apartamento> l = new ListaDoble<>();
        for (Apartamento a : aptos) l.agregarFinal(a);
        return l;
    }

    private int capacidadTotal(ListaDoble<Apartamento> l) {
        int total = 0;
        for (Apartamento a : l) total += a.getCapacidadMaxima();
        return total;
    }

    @Test
    void unSoloApartamentoSiCabe() {
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(
                lista(apto("A1", 4), apto("A2", 6)), 5);
        assertNotNull(r);
        assertEquals(1, r.tamanio());
        assertEquals("A2", r.obtener(0).getCodigo());
    }

    @Test
    void usaElMenorNumeroDeApartamentos() {
        // 8 personas: 6+4 (2 aptos) es mejor que 4+4+... o 3+3+3
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(
                lista(apto("A1", 3), apto("A2", 3), apto("A3", 3), apto("A4", 6), apto("A5", 4)), 8);
        assertNotNull(r);
        assertEquals(2, r.tamanio());
        assertTrue(capacidadTotal(r) >= 8);
    }

    @Test
    void necesitaVariosApartamentos() {
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(
                lista(apto("A1", 4), apto("A2", 4), apto("A3", 4)), 10);
        assertNotNull(r);
        assertEquals(3, r.tamanio());
        assertEquals(12, capacidadTotal(r));
    }

    @Test
    void capacidadExactaSeAcepta() {
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(
                lista(apto("A1", 4), apto("A2", 4)), 8);
        assertNotNull(r);
        assertEquals(2, r.tamanio());
    }

    @Test
    void siNoAlcanzaDevuelveNull() {
        assertNull(AsignacionGrupos.asignarGrupo(lista(apto("A1", 4), apto("A2", 4)), 9));
    }

    @Test
    void sinApartamentosDisponiblesDevuelveNull() {
        assertNull(AsignacionGrupos.asignarGrupo(new ListaDoble<>(), 2));
    }

    @Test
    void nuncaMasApartamentosQueLosNecesarios() {
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(
                lista(apto("A1", 2), apto("A2", 2), apto("A3", 2), apto("A4", 5)), 5);
        assertEquals(1, r.tamanio());
        assertEquals("A4", r.obtener(0).getCodigo());
    }

    @Test
    void empateSeResuelveConLaPrimeraCombinacionEncontrada() {
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(
                lista(apto("A1", 4), apto("A2", 4), apto("A3", 4)), 8);
        assertEquals(2, r.tamanio());
        assertEquals("A1", r.obtener(0).getCodigo());
        assertEquals("A2", r.obtener(1).getCodigo());
    }

    @Test
    void noModificaLaListaDeEntrada() {
        ListaDoble<Apartamento> entrada = lista(apto("A1", 4), apto("A2", 4));
        AsignacionGrupos.asignarGrupo(entrada, 6);
        assertEquals(2, entrada.tamanio());
    }

    @Test
    void llamadasConsecutivasNoSeContaminan() {
        ListaDoble<Apartamento> d = lista(apto("A1", 4), apto("A2", 4));
        assertEquals(2, AsignacionGrupos.asignarGrupo(d, 8).tamanio());
        assertEquals(1, AsignacionGrupos.asignarGrupo(d, 3).tamanio());
        assertNull(AsignacionGrupos.asignarGrupo(d, 20));
    }

    @Test
    void conCeroPersonasDevuelveListaVacia() {
        ListaDoble<Apartamento> r = AsignacionGrupos.asignarGrupo(lista(apto("A1", 4)), 0);
        assertNotNull(r);
        assertTrue(r.estaVacia());
    }
}
