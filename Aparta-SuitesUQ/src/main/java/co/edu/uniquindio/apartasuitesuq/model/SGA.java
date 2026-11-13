package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.AnalisisGrafo;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.MapaEdificio;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ColaPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ConsultaEntradasReservas;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.LiquidadorTarifas;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioPreparacion;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioRecepcion;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioVencimiento;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Fachada del Sistema de Gestión de Alojamiento.
 * <p>
 * Es el único punto de acceso de la interfaz gráfica al modelo: guarda los catálogos
 * (apartamentos, huéspedes, empleados), el mapa del edificio y el calendario de temporadas,
 * y delega cada operación en el servicio que la implementa (reservas, canal externo,
 * recepción, preparación, vencimientos, turnos y liquidación de tarifas).
 * <p>
 * La GUI obtiene la instancia compartida con {@link #getInstancia()}; las pruebas pueden
 * crear la suya con un {@link Clock} fijo para controlar la fecha "de hoy".
 * <p>
 * Integración pendiente: cuando se integre el {@code ArbolBinarioBusqueda} de P2, los catálogos
 * de apartamentos, huéspedes y empleados pasan de {@link ListaDoble} al árbol. Solo cambian
 * los métodos {@code registrar*} y {@code buscar*} de esta clase; la GUI no se entera.
 */
public class SGA {

    private static SGA instancia;

    private final Clock reloj;

    // catálogos
    private ListaDoble<Apartamento> apartamentos;
    private ListaDoble<Huesped> huespedes;
    private ListaDoble<Empleado> empleados;

    // edificio y tarifas
    private MapaEdificio mapa;
    private CalendarioTemporadas calendario;
    private LiquidadorTarifas liquidador;

    // servicios
    private ServicioReservas servicioReservas;
    private ServicioCanalExterno canalExterno;
    private int solicitudesExternasPendientes;
    private ServicioTurnos turnos;
    private ColaPreparacion colaPreparacion;
    private ServicioPreparacion preparacion;
    private ServicioRecepcion recepcion;
    private ServicioVencimiento vencimiento;

    /** Crea un sistema vacío que usa la fecha y hora del equipo. */
    public SGA() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Crea un sistema vacío con un reloj propio.
     * @param reloj de aquí salen "hoy" y "ahora" (en las pruebas se usa {@code Clock.fixed})
     */
    public SGA(Clock reloj) {
        if (reloj == null) {
            throw new IllegalArgumentException("El reloj es obligatorio.");
        }
        this.reloj = reloj;
        reiniciar();
    }

    /** @return la instancia compartida por todas las pantallas */
    public static synchronized SGA getInstancia() {
        if (instancia == null) {
            instancia = new SGA();
        }
        return instancia;
    }

    /** Deja el sistema vacío: sin apartamentos, huéspedes, empleados, reservas ni temporadas. */
    public final void reiniciar() {
        apartamentos = new ListaDoble<>();
        huespedes = new ListaDoble<>();
        empleados = new ListaDoble<>();
        calendario = new CalendarioTemporadas();
        liquidador = new LiquidadorTarifas(calendario);
        servicioReservas = new ServicioReservas();
        canalExterno = new ServicioCanalExterno(servicioReservas);
        solicitudesExternasPendientes = 0;
        turnos = new ServicioTurnos();
        vencimiento = new ServicioVencimiento(r -> r.cambiarEstado(EstadoReserva.CANCELADA));
        colaPreparacion = new ColaPreparacion(
                new ConsultaEntradasReservas(() -> servicioReservas.getReservas(), this::hoy,
                        Reserva::getHoraEstimadaLlegada));
        definirMapa(new MapaEdificio());
    }

    // ================================================================ carga de datos

    /**
     * Reemplaza todo el contenido del sistema por el escenario del archivo.
     * Si el archivo tiene un error, el sistema queda como estaba.
     *
     * @throws IOException              si el archivo no se puede leer
     * @throws IllegalArgumentException si el archivo tiene un error (el mensaje indica la línea)
     */
    public void cargarEscenario(Path archivo) throws IOException {
        SGA nuevo = new SGA(reloj);
        new CargadorDatos(nuevo).cargar(archivo);
        adoptar(nuevo);
    }

    /** Igual que {@link #cargarEscenario(Path)} pero leyendo de un Reader. */
    public void cargarEscenario(Reader lector) throws IOException {
        SGA nuevo = new SGA(reloj);
        new CargadorDatos(nuevo).cargar(lector);
        adoptar(nuevo);
    }

    /** Carga el escenario de prueba que viene incluido en la aplicación. */
    public void cargarEscenarioDeEjemplo() throws IOException {
        SGA nuevo = new SGA(reloj);
        new CargadorDatos(nuevo).cargarEjemplo();
        adoptar(nuevo);
    }

    /** Toma el estado de un sistema ya cargado (todos sus servicios quedan enlazados entre sí). */
    private void adoptar(SGA otro) {
        apartamentos = otro.apartamentos;
        huespedes = otro.huespedes;
        empleados = otro.empleados;
        mapa = otro.mapa;
        calendario = otro.calendario;
        liquidador = otro.liquidador;
        servicioReservas = otro.servicioReservas;
        canalExterno = otro.canalExterno;
        solicitudesExternasPendientes = otro.solicitudesExternasPendientes;
        turnos = otro.turnos;
        colaPreparacion = otro.colaPreparacion;
        preparacion = otro.preparacion;
        recepcion = otro.recepcion;
        vencimiento = otro.vencimiento;
    }

    // ===================================================================== tiempo

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    // ============================================================== apartamentos

    /** @throws IllegalArgumentException si ya existe un apartamento con ese código */
    public void registrarApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento no puede ser nulo.");
        }
        if (buscarApartamento(apartamento.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe el apartamento " + apartamento.getCodigo() + ".");
        }
        apartamentos.agregarFinal(apartamento);
    }

    /** @return el apartamento con ese código (sin distinguir mayúsculas) o null */
    public Apartamento buscarApartamento(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Apartamento apartamento : apartamentos) {
            if (apartamento.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return apartamento;
            }
        }
        return null;
    }

    /** @throws IllegalArgumentException si el código no existe */
    public Apartamento obtenerApartamento(String codigo) {
        Apartamento apartamento = buscarApartamento(codigo);
        if (apartamento == null) {
            throw new IllegalArgumentException("No existe el apartamento " + codigo + ".");
        }
        return apartamento;
    }

    public ListaDoble<Apartamento> getApartamentos() {
        return apartamentos;
    }

    // ================================================================= huéspedes

    /** @throws IllegalArgumentException si ya existe un huésped con ese documento */
    public void registrarHuesped(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("El huésped no puede ser nulo.");
        }
        if (buscarHuesped(huesped.getDocumento()) != null) {
            throw new IllegalArgumentException("Ya existe un huésped con documento " + huesped.getDocumento() + ".");
        }
        huespedes.agregarFinal(huesped);
    }

    /** @return el huésped con ese documento o null. También sirve para que el huésped inicie sesión. */
    public Huesped buscarHuesped(String documento) {
        if (documento == null) {
            return null;
        }
        for (Huesped huesped : huespedes) {
            if (huesped.getDocumento().equals(documento.trim())) {
                return huesped;
            }
        }
        return null;
    }

    public ListaDoble<Huesped> getHuespedes() {
        return huespedes;
    }

    // ================================================================= empleados

    /**
     * Registra un empleado. Los recepcionistas entran además a la rotación de turnos.
     * @throws IllegalArgumentException si ya existe un empleado con ese id
     */
    public void registrarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo.");
        }
        if (buscarEmpleado(empleado.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un empleado con id " + empleado.getId() + ".");
        }
        empleados.agregarFinal(empleado);
        if (empleado instanceof Recepcionista) {
            turnos.registrar((Recepcionista) empleado);
        }
    }

    public Empleado buscarEmpleado(String id) {
        if (id == null) {
            return null;
        }
        for (Empleado empleado : empleados) {
            if (empleado.getId().equalsIgnoreCase(id.trim())) {
                return empleado;
            }
        }
        return null;
    }

    /**
     * Inicio de sesión de recepcionistas y administradores.
     * @return el empleado si el id y la contraseña coinciden; null en otro caso
     */
    public Empleado autenticar(String id, String contrasena) {
        Empleado empleado = buscarEmpleado(id);
        if (empleado != null && empleado.verificarContrasena(contrasena)) {
            return empleado;
        }
        return null;
    }

    public ListaDoble<Empleado> getEmpleados() {
        return empleados;
    }

    // ===================================================== temporadas y tarifas

    public CalendarioTemporadas getCalendario() {
        return calendario;
    }

    /** @throws FechasInvalidasException si el periodo es inválido o se cruza con otro */
    public void definirTemporada(Temporada temporada, LocalDate inicio, LocalDate fin) {
        calendario.agregarPeriodo(temporada, inicio, fin);
    }

    public boolean eliminarPeriodo(PeriodoTemporada periodo) {
        return calendario.eliminarPeriodo(periodo);
    }

    /** Define la tarifa por persona por noche de un apartamento en una temporada. */
    public void definirTarifa(String codigoApartamento, Temporada temporada, double tarifa) {
        obtenerApartamento(codigoApartamento).setTarifa(temporada, tarifa);
    }

    // ================================================ disponibilidad y cotización

    /**
     * @return true si el apartamento no está fuera de servicio y ninguna reserva activa
     *         ocupa alguna de las noches [entrada, salida)
     */
    public boolean estaDisponible(Apartamento apartamento, LocalDate entrada, LocalDate salida) {
        new Estancia(entrada, salida); // valida las fechas
        if (apartamento.estaFueraDeServicio()) {
            return false;
        }
        for (Reserva reserva : servicioReservas.getReservas()) {
            if (reserva.estaActiva() && reserva.getApartamento().equals(apartamento)
                    && reserva.seSolapaCon(entrada, salida)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apartamentos libres para un rango de fechas y un grupo.
     *
     * @param orden criterio elegido por el huésped (precio, capacidad, dormitorios...);
     *              si es null se usa el orden natural (por código)
     * @return los apartamentos disponibles, ordenados
     */
    public ListaDoble<Apartamento> buscarDisponibles(LocalDate entrada, LocalDate salida, int personas,
                                                     Comparator<Apartamento> orden) {
        int cantidad = 0;
        for (Apartamento apartamento : apartamentos) {
            if (apartamento.soportaCapacidad(personas) && estaDisponible(apartamento, entrada, salida)) {
                cantidad++;
            }
        }
        Apartamento[] libres = new Apartamento[cantidad];
        int i = 0;
        for (Apartamento apartamento : apartamentos) {
            if (apartamento.soportaCapacidad(personas) && estaDisponible(apartamento, entrada, salida)) {
                libres[i++] = apartamento;
            }
        }
        ordenarPorInsercion(libres, orden == null ? Comparator.naturalOrder() : orden);

        ListaDoble<Apartamento> resultado = new ListaDoble<>();
        for (Apartamento apartamento : libres) {
            resultado.agregarFinal(apartamento);
        }
        return resultado;
    }

    private static <T> void ordenarPorInsercion(T[] datos, Comparator<? super T> orden) {
        for (int i = 1; i < datos.length; i++) {
            T actual = datos[i];
            int j = i - 1;
            while (j >= 0 && orden.compare(datos[j], actual) > 0) {
                datos[j + 1] = datos[j];
                j--;
            }
            datos[j + 1] = actual;
        }
    }

    /**
     * Cotización con desglose noche por noche.
     * @param ocupantes ocupantes con fecha de nacimiento (puede ser null); el resto del grupo se cobra como adulto
     */
    public Cotizacion cotizar(String codigoApartamento, LocalDate entrada, LocalDate salida,
                              ListaDoble<Ocupante> ocupantes, int personas) {
        return liquidador.cotizar(obtenerApartamento(codigoApartamento), new Estancia(entrada, salida),
                ocupantes, personas);
    }

    public LiquidadorTarifas getLiquidador() {
        return liquidador;
    }

    // ================================================================== reservas

    /**
     * Crea una reserva por el portal o de forma directa.
     * Portal: queda Pendiente hasta pagar el anticipo y vence a las 24 h.
     * Directa: queda Confirmada.
     *
     * @param ocupantes acompañantes con fecha de nacimiento, sin incluir al titular (puede ser null)
     * @throws FechasInvalidasException   si las fechas son inválidas o están en el pasado
     * @throws CapacidadExcedidaException si el grupo no cabe o hay más ocupantes que personas
     * @throws ReservaSolapadaException   si el apartamento ya está reservado en esas noches
     */
    public Reserva crearReserva(Huesped titular, String codigoApartamento, LocalDate entrada, LocalDate salida,
                                ListaDoble<Ocupante> ocupantes, int personas, Canal canal) {
        if (titular == null || canal == null) {
            throw new IllegalArgumentException("El titular y el canal son obligatorios.");
        }
        if (canal == Canal.EXTERNO) {
            throw new IllegalArgumentException("Las reservas externas entran por la cola del canal externo.");
        }
        if (buscarHuesped(titular.getDocumento()) == null) {
            registrarHuesped(titular);
        }
        validarOcupantes(titular, ocupantes, personas);

        Reserva reserva = servicioReservas.crearReserva(titular, obtenerApartamento(codigoApartamento),
                entrada, salida, personas, canal);
        if (ocupantes != null) {
            for (Ocupante ocupante : ocupantes) {
                reserva.agregarOcupante(ocupante);
            }
        }
        prepararReservaNueva(reserva);
        return reserva;
    }

    private static void validarOcupantes(Huesped titular, ListaDoble<Ocupante> ocupantes, int personas) {
        int registrados = titular.getFechaNacimiento() != null ? 1 : 0;
        if (ocupantes != null) {
            for (Ocupante ignored : ocupantes) {
                registrados++;
            }
        }
        if (registrados > personas) {
            throw new CapacidadExcedidaException("Se registraron " + registrados + " ocupantes para un grupo de "
                    + personas + " personas.");
        }
    }

    /** Liquida la reserva, carga el valor al folio y, si quedó Pendiente, la pone a vencer. */
    private void prepararReservaNueva(Reserva reserva) {
        liquidador.liquidar(reserva);
        reserva.getFolio().registrarCargo(reserva.getCostoTotal(),
                "Alojamiento " + reserva.getEstancia());
        if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
            vencimiento.registrar(reserva, reserva.getFechaCreacion());
        }
    }

    /**
     * Registra una reserva ya existente sin las validaciones de creación (fechas pasadas,
     * estado inicial). La usa el cargador de datos.
     * @throws ReservaSolapadaException si está activa y choca con otra reserva activa
     */
    void registrarReservaExistente(Reserva reserva) {
        servicioReservas.registrar(reserva);
        if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
            vencimiento.registrar(reserva, reserva.getFechaCreacion());
        }
    }

    /** Paga el anticipo del 30 % de una reserva del portal y la confirma. */
    public void pagarAnticipo(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new TransicionEstadoInvalidaException("La reserva " + reserva.getCodigoReserva()
                    + " no está pendiente de anticipo.");
        }
        reserva.getFolio().registrarPago(reserva.getValorAnticipo(), "Anticipo 30 %");
        reserva.cambiarEstado(EstadoReserva.CONFIRMADA);
    }

    /** @return lo que se le devolvería al huésped si cancela ahora */
    public double calcularDevolucion(Reserva reserva) {
        return servicioReservas.calcularDevolucion(reserva);
    }

    /** Cancela la reserva y libera sus noches. @return el valor devuelto */
    public double cancelarReserva(Reserva reserva) {
        return servicioReservas.cancelar(reserva);
    }

    /** Marca como no-show una reserva confirmada cuyo grupo nunca llegó. */
    public void marcarNoShow(Reserva reserva) {
        reserva.cambiarEstado(EstadoReserva.NOSHOW);
    }

    public Reserva buscarReserva(String codigo) {
        return servicioReservas.buscarPorCodigo(codigo);
    }

    public ListaDoble<Reserva> reservasDe(Huesped huesped) {
        return servicioReservas.reservasDe(huesped);
    }

    /** @return las reservas de un apartamento, en el orden en que se registraron */
    public ListaDoble<Reserva> reservasDe(Apartamento apartamento) {
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        for (Reserva reserva : servicioReservas.getReservas()) {
            if (reserva.getApartamento().equals(apartamento)) {
                resultado.agregarFinal(reserva);
            }
        }
        return resultado;
    }

    public ListaDoble<Reserva> getReservas() {
        return servicioReservas.getReservas();
    }

    /** Cancela las reservas Pendientes cuyo plazo de 24 h ya pasó. @return las canceladas */
    public ListaDoble<Reserva> procesarVencimientos() {
        return vencimiento.procesarVencimientos(ahora());
    }

    // ============================================================= canal externo

    /** Encola una solicitud de la plataforma externa. Si el titular no existe, se registra. */
    public void recibirSolicitudExterna(SolicitudExterna solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula.");
        }
        if (buscarHuesped(solicitud.getTitular().getDocumento()) == null) {
            registrarHuesped(solicitud.getTitular());
        }
        canalExterno.recibirSolicitud(solicitud);
        solicitudesExternasPendientes++;
    }

    /**
     * Procesa la solicitud más antigua de la cola (FIFO).
     * @return la reserva creada, o null si la solicitud fue rechazada
     * @throws EstructuraVaciaException si no hay solicitudes pendientes
     */
    public Reserva procesarSiguienteSolicitud() {
        if (solicitudesExternasPendientes == 0) {
            throw new EstructuraVaciaException("No hay solicitudes del canal externo por procesar.");
        }
        solicitudesExternasPendientes--;
        Reserva reserva = canalExterno.procesarSiguiente();
        if (reserva != null) {
            prepararReservaNueva(reserva);
        }
        return reserva;
    }

    public int getSolicitudesExternasPendientes() {
        return solicitudesExternasPendientes;
    }

    public ListaDoble<ConflictoCanal> getConflictosCanal() {
        return canalExterno.getConflictos();
    }

    // ============================================================ folio

    public void registrarCargo(Reserva reserva, double monto, String descripcion) {
        reserva.getFolio().registrarCargo(monto, descripcion);
    }

    public void registrarPago(Reserva reserva, double monto, String descripcion) {
        reserva.getFolio().registrarPago(monto, descripcion);
    }

    /** Deshace la última operación del folio registrando el movimiento contrario. */
    public void deshacerUltimaOperacion(Reserva reserva) {
        reserva.getFolio().deshacer();
    }

    public double saldoDe(Reserva reserva) {
        return reserva.getFolio().calcularSaldo();
    }

    // ============================================================ recepción

    /** Check-in de una reserva Confirmada que entra hoy; el apartamento debe estar Preparado. */
    public void checkIn(Reserva reserva) {
        recepcion.checkIn(reserva, hoy());
    }

    /** Check-out: la reserva finaliza y el apartamento entra a la cola de preparación. */
    public void checkOut(Reserva reserva) {
        recepcion.checkOut(reserva, ahora());
    }

    public Recepcionista turnoActual() {
        return turnos.turnoActual();
    }

    public Recepcionista pasarTurno() {
        return turnos.pasarTurno();
    }

    public ServicioTurnos getTurnos() {
        return turnos;
    }

    // ============================================================ preparación

    /** Saca de la cola el apartamento más prioritario y lo pone En preparación. */
    public ColaPreparacion.Item iniciarSiguientePreparacion() {
        return preparacion.iniciarSiguiente();
    }

    public void terminarPreparacion(ColaPreparacion.Item item) {
        preparacion.terminarPreparacion(item, ahora());
    }

    public void ponerFueraDeServicio(String codigoApartamento, String motivo) {
        preparacion.ponerFueraDeServicio(obtenerApartamento(codigoApartamento), motivo, ahora());
    }

    public void volverAServicio(String codigoApartamento) {
        preparacion.volverAServicio(obtenerApartamento(codigoApartamento), ahora());
    }

    /** Pone en la cola de preparación un apartamento que ya está Pendiente (lo usa la carga de datos). */
    void encolarParaPreparacion(Apartamento apartamento) {
        colaPreparacion.encolar(apartamento, ahora());
    }

    /** @return ruta más corta desde recepción que pasa por los apartamentos pendientes */
    public Recorrido<String> rutaPreparacion() {
        if (mapa.getRecepcion() == null) {
            throw new IllegalStateException("El mapa del edificio no tiene recepción.");
        }
        return preparacion.rutaSugerida();
    }

    public double tiempoMedioPreparacionMin() {
        return preparacion.tiempoMedioPreparacionMin();
    }

    public String motivoFueraDeServicio(Apartamento apartamento) {
        String motivo = preparacion.motivoFueraDeServicio(apartamento);
        return motivo != null ? motivo : apartamento.getMotivoFueraServicio();
    }

    public ColaPreparacion getColaPreparacion() {
        return colaPreparacion;
    }

    public ServicioPreparacion getServicioPreparacion() {
        return preparacion;
    }

    // ================================================================ mapa

    /**
     * Cambia el mapa del edificio. Los servicios de preparación y recepción se reconstruyen
     * porque dependen del grafo y del nodo de recepción.
     */
    public void definirMapa(MapaEdificio nuevoMapa) {
        if (nuevoMapa == null) {
            throw new IllegalArgumentException("El mapa no puede ser nulo.");
        }
        this.mapa = nuevoMapa;
        this.preparacion = new ServicioPreparacion(mapa.getGrafo(), mapa.getRecepcion(), colaPreparacion);
        this.recepcion = new ServicioRecepcion(preparacion);
    }

    public MapaEdificio getMapa() {
        return mapa;
    }

    public AnalisisGrafo getAnalisisGrafo() {
        return new AnalisisGrafo(mapa);
    }
}
