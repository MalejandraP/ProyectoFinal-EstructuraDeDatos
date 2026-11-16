package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ListaDoble;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.EstructuraVaciaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.ReservaSolapadaException;
import co.edu.uniquindio.apartasuitesuq.model.Excepciones.FechasInvalidasException;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioCanalExterno;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioReservas;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.ServicioTurnos;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.AnalisisGrafo;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.MapaEdificio;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.Recorrido;
import co.edu.uniquindio.apartasuitesuq.model.Servicios.AsignacionGrupos;
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
 * Los catálogos de apartamentos (por código), huéspedes (por documento) y empleados (por id)
 * son árboles binarios de búsqueda: registrar, buscar y eliminar son recursivos y recorrer el
 * catálogo lo entrega ordenado (inorden).
 */
public class SGA {

    private static SGA instancia;

    private final Clock reloj;

    // catálogos
    private ArbolBinarioBusqueda<Apartamento> apartamentos;
    private ArbolBinarioBusqueda<Huesped> huespedes;
    private ArbolBinarioBusqueda<Empleado> empleados;

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
    private ListaDoble<ColaPreparacion.Item> enPreparacion;
    private ListaDoble<RegistroPreparacion> preparacionesTerminadas;

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
        apartamentos = new ArbolBinarioBusqueda<>();
        huespedes = new ArbolBinarioBusqueda<>();
        empleados = new ArbolBinarioBusqueda<>();
        calendario = new CalendarioTemporadas();
        liquidador = new LiquidadorTarifas(calendario);
        servicioReservas = new ServicioReservas();
        canalExterno = new ServicioCanalExterno(servicioReservas);
        solicitudesExternasPendientes = 0;
        turnos = new ServicioTurnos();
        enPreparacion = new ListaDoble<>();
        preparacionesTerminadas = new ListaDoble<>();
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
        enPreparacion = otro.enPreparacion;
        preparacionesTerminadas = otro.preparacionesTerminadas;
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
        if (apartamentos.contiene(apartamento)) {
            throw new IllegalArgumentException("Ya existe el apartamento " + apartamento.getCodigo() + ".");
        }
        apartamentos.insertar(apartamento);
    }

    /**
     * Búsqueda recursiva en el árbol por código (sin distinguir mayúsculas).
     * @return el apartamento o null si no existe
     */
    public Apartamento buscarApartamento(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return apartamentos.buscar(new Apartamento(codigo, "clave", "clave", 1, 1, 1, 1));
    }

    /**
     * Elimina un apartamento del catálogo.
     * @throws IllegalStateException si tiene reservas activas
     */
    public boolean eliminarApartamento(String codigo) {
        Apartamento apartamento = buscarApartamento(codigo);
        if (apartamento == null) {
            return false;
        }
        for (Reserva reserva : servicioReservas.getReservas()) {
            if (reserva.estaActiva() && reserva.getApartamento().equals(apartamento)) {
                throw new IllegalStateException("El apartamento " + apartamento.getCodigo()
                        + " tiene reservas activas (" + reserva.getCodigoReserva() + ").");
            }
        }
        return apartamentos.eliminar(apartamento);
    }

    /** @throws IllegalArgumentException si el código no existe */
    public Apartamento obtenerApartamento(String codigo) {
        Apartamento apartamento = buscarApartamento(codigo);
        if (apartamento == null) {
            throw new IllegalArgumentException("No existe el apartamento " + codigo + ".");
        }
        return apartamento;
    }

    /** @return el catálogo; recorrerlo con for-each lo entrega ordenado por código */
    public ArbolBinarioBusqueda<Apartamento> getApartamentos() {
        return apartamentos;
    }

    // ================================================================= huéspedes

    /** @throws IllegalArgumentException si ya existe un huésped con ese documento */
    public void registrarHuesped(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("El huésped no puede ser nulo.");
        }
        if (huespedes.contiene(huesped)) {
            throw new IllegalArgumentException("Ya existe un huésped con documento " + huesped.getDocumento() + ".");
        }
        huespedes.insertar(huesped);
    }

    /**
     * Búsqueda recursiva en el árbol por documento. También sirve para que el huésped inicie sesión.
     * @return el huésped o null si no existe
     */
    public Huesped buscarHuesped(String documento) {
        if (documento == null || documento.isBlank()) {
            return null;
        }
        return huespedes.buscar(new Huesped(documento, "clave", "clave", "clave@clave.co"));
    }

    /**
     * Elimina un huésped del catálogo.
     * @throws IllegalStateException si es titular de reservas activas
     */
    public boolean eliminarHuesped(String documento) {
        Huesped huesped = buscarHuesped(documento);
        if (huesped == null) {
            return false;
        }
        for (Reserva reserva : servicioReservas.reservasDe(huesped)) {
            if (reserva.estaActiva()) {
                throw new IllegalStateException("El huésped tiene reservas activas (" + reserva.getCodigoReserva() + ").");
            }
        }
        return huespedes.eliminar(huesped);
    }

    /** @return el catálogo; recorrerlo con for-each lo entrega ordenado por documento */
    public ArbolBinarioBusqueda<Huesped> getHuespedes() {
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
        empleados.insertar(empleado);
        if (empleado instanceof Recepcionista) {
            turnos.registrar((Recepcionista) empleado);
        }
    }

    /** Búsqueda recursiva en el árbol por id. @return el empleado o null */
    public Empleado buscarEmpleado(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return empleados.buscar(new Administrador("clave", id.trim()));
    }

    /** Elimina un empleado; si es recepcionista, sale también de la rotación de turnos. */
    public boolean eliminarEmpleado(String id) {
        Empleado empleado = buscarEmpleado(id);
        if (empleado == null) {
            return false;
        }
        if (empleado instanceof Recepcionista) {
            turnos.retirar((Recepcionista) empleado);
        }
        return empleados.eliminar(empleado);
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

    /** @return el catálogo; recorrerlo con for-each lo entrega ordenado por id */
    public ArbolBinarioBusqueda<Empleado> getEmpleados() {
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
        Estancia solicitada = new Estancia(entrada, salida); // valida las fechas
        if (apartamento == null || apartamento.estaFueraDeServicio()) {
            return false;
        }
        return !servicioReservas.haySolapamiento(apartamento, solicitada);
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

    /**
     * Crea una reserva por el canal Portal: queda Pendiente, con el cargo de la estancia
     * en el folio y programada para vencer a las 24 horas si no se paga el anticipo.
     *
     * @param ocupantes acompañantes con fecha de nacimiento (sin incluir al titular, que se registra solo)
     * @return la reserva creada
     */
    public Reserva crearReservaPortal(Huesped titular, Apartamento apartamento,
                                      LocalDate entrada, LocalDate salida,
                                      int personas, ListaDoble<Ocupante> ocupantes) {
        Reserva reserva = servicioReservas.crearReserva(titular, apartamento, entrada, salida,
                personas, Canal.PORTAL);
        try {
            if (ocupantes != null) {
                for (Ocupante o : ocupantes) {
                    reserva.agregarOcupante(o);
                }
            }
            liquidador.liquidar(reserva);
            reserva.getFolio().registrarCargo(reserva.getCostoTotal(), "Alojamiento " + reserva.getEstancia());
            vencimiento.registrar(reserva, reserva.getFechaCreacion());
        } catch (RuntimeException ex) {
            servicioReservas.desindexar(reserva);           // no dejar una reserva a medias
            throw ex;
        }
        return reserva;
    }

    public Reserva crearReservaExterna(SolicitudExterna s) {
        Reserva reserva = servicioReservas.crearReserva(s.getTitular(), s.getApartamento(),
                s.getEntrada(), s.getSalida(), s.getNumPersonas(), Canal.EXTERNO);
        liquidador.liquidar(reserva);
        reserva.getFolio().registrarCargo(reserva.getCostoTotal(), "Alojamiento " + reserva.getEstancia());
        return reserva;
    }
    /** Paga el anticipo del 30 % de una reserva del portal y la confirma. */
    /**
     * Paga lo que falte del anticipo del 30 % (descontando abonos previos) y confirma la reserva.
     * @return el valor pagado ahora
     */
    public double pagarAnticipo(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new TransicionEstadoInvalidaException("La reserva " + reserva.getCodigoReserva()
                    + " no está pendiente de anticipo.");
        }
        return servicioReservas.pagarAnticipo(reserva);
    }

    /** Confirma una reserva Pendiente sin registrar pago (decisión de recepción). */
    public void confirmarReserva(Reserva reserva) {
        servicioReservas.confirmar(reserva);
    }

    /**
     * @return todas las reservas ordenadas por código (RES-1, RES-2, ... RES-10), copiando el
     *         recorrido del árbol a un arreglo y ordenándolo por inserción
     */
    public ListaDoble<Reserva> reservasOrdenadasPorCodigo() {
        ListaDoble<Reserva> todas = servicioReservas.getReservas();
        Reserva[] arreglo = new Reserva[todas.tamanio()];
        int i = 0;
        for (Reserva reserva : todas) {
            arreglo[i++] = reserva;
        }
        ordenarPorInsercion(arreglo, Comparator.comparingInt((Reserva r) -> numeroDeCodigo(r.getCodigoReserva()))
                .thenComparing(Reserva::getCodigoReserva));
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        for (Reserva reserva : arreglo) {
            resultado.agregarFinal(reserva);
        }
        return resultado;
    }

    private static int numeroDeCodigo(String codigo) {
        String digitos = codigo.replaceAll("[^0-9]", "");
        try {
            return digitos.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(digitos);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    // ============================================================ grupos grandes

    /**
     * Combinación con el menor número de apartamentos libres que aloja a todo el grupo
     * (backtracking de {@code AsignacionGrupos}).
     * @return la combinación, o una lista vacía si ni todos los libres juntos alcanzan
     */
    public ListaDoble<Apartamento> asignarGrupoGrande(LocalDate entrada, LocalDate salida, int personas) {
        if (personas < 1) {
            throw new CapacidadExcedidaException("El grupo debe tener al menos una persona.");
        }
        ListaDoble<Apartamento> libres = buscarDisponibles(entrada, salida, 1, OrdenApartamentos.porCapacidad());
        ListaDoble<Apartamento> combinacion = AsignacionGrupos.asignarGrupo(libres, personas);
        return combinacion == null ? new ListaDoble<>() : combinacion;
    }

    /**
     * Reserva todos los apartamentos de una combinación, repartiendo el grupo según la capacidad
     * de cada uno. Si alguna reserva falla, se deshacen las ya creadas.
     */
    public ListaDoble<Reserva> reservarGrupo(Huesped titular, ListaDoble<Apartamento> combinacion,
                                             LocalDate entrada, LocalDate salida, int personas, Canal canal) {
        int capacidadTotal = 0;
        for (Apartamento apartamento : combinacion) {
            capacidadTotal += apartamento.getCapacidadMaxima();
        }
        if (combinacion.estaVacia() || capacidadTotal < personas) {
            throw new CapacidadExcedidaException("La combinación aloja " + capacidadTotal + " de " + personas + " personas.");
        }
        ListaDoble<Reserva> creadas = new ListaDoble<>();
        int faltan = personas;
        try {
            for (Apartamento apartamento : combinacion) {
                if (faltan <= 0) {
                    break;
                }
                int aqui = Math.min(faltan, apartamento.getCapacidadMaxima());
                creadas.agregarFinal(crearReserva(titular, apartamento.getCodigo(), entrada, salida, null, aqui, canal));
                faltan -= aqui;
            }
        } catch (RuntimeException ex) {
            for (Reserva reserva : creadas) {
                servicioReservas.desindexar(reserva);
            }
            throw ex;
        }
        return creadas;
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
    /** @return las reservas de un apartamento ordenadas por fecha de entrada */
    public ListaDoble<Reserva> reservasDe(Apartamento apartamento) {
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        if (apartamento != null) {
            apartamento.getReservas().recorridoInorden(resultado::agregarFinal);
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

    public ServicioReservas getServicioReservas() {return servicioReservas;}

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
        SolicitudExterna s = procesarSolicitudCompleta();
        return s.getEstado() == EstadoSolicitud.ACEPTADA ? s.getReservaCreada() : null;
    }
    /** Procesa la siguiente solicitud y la devuelve con su resultado (para la pantalla). */
    public SolicitudExterna procesarSolicitudCompleta() {
        if (solicitudesExternasPendientes == 0) {
            throw new EstructuraVaciaException("No hay solicitudes del canal externo por procesar.");
        }
        solicitudesExternasPendientes--;
        SolicitudExterna s = canalExterno.procesarSolicitud();
        if (s.getEstado() == EstadoSolicitud.ACEPTADA) {
            prepararReservaNueva(s.getReservaCreada());
        }
        return s;
    }

    public int getSolicitudesExternasPendientes() {
        return solicitudesExternasPendientes;
    }

    public ListaDoble<ConflictoCanal> getConflictosCanal() {
        return canalExterno.getConflictos();
    }

    public ServicioCanalExterno getCanalExterno() {
        return canalExterno;
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

    /** @return el historial de apartamentos fuera de servicio (motivo, desde, hasta) */
    public co.edu.uniquindio.apartasuitesuq.model.Servicios.RegistroFueraDeServicio historialFueraDeServicio() {
        return preparacion.historialFueraDeServicio();
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
        if (colaPreparacion.estaVacia()) {
            throw new EstructuraVaciaException("No hay apartamentos en la cola de preparación.");
        }
        ColaPreparacion.Item item = preparacion.iniciarSiguiente();
        enPreparacion.agregarFinal(item);
        return item;
    }

    /** Deja el apartamento Preparado y registra cuánto tardó su preparación. */
    public void terminarPreparacion(ColaPreparacion.Item item) {
        LocalDateTime fin = ahora();
        preparacion.terminarPreparacion(item, fin);
        enPreparacion.eliminar(item);
        preparacionesTerminadas.agregarFinal(
                new RegistroPreparacion(item.getApartamento().getCodigo(), item.getIngreso(), fin));
    }

    /**
     * @return la cola de preparación en orden de atención (prioridad, hora clave, código), sin
     *         desencolarla: el heap se copia a un arreglo y se ordena por inserción
     */
    public ListaDoble<ColaPreparacion.Item> colaEnOrden() {
        ColaPreparacion.Item[] items = new ColaPreparacion.Item[colaPreparacion.tamano()];
        int i = 0;
        for (ColaPreparacion.Item item : colaPreparacion) {
            items[i++] = item;
        }
        ordenarPorInsercion(items, Comparator.comparingInt(ColaPreparacion.Item::getPrioridad)
                .thenComparing(ColaPreparacion.Item::getHoraClave)
                .thenComparing(item -> item.getApartamento().getCodigo()));
        ListaDoble<ColaPreparacion.Item> resultado = new ListaDoble<>();
        for (ColaPreparacion.Item item : items) {
            if (item.getApartamento().getEstadoOperativo() == EstadoApartamento.PENDIENTEDEPREPARACION) {
                resultado.agregarFinal(item); // los ya despachados se descartan al desencolar
            }
        }
        return resultado;
    }

    /** @return los apartamentos que el personal está preparando ahora */
    public ListaDoble<ColaPreparacion.Item> getEnPreparacion() {
        return enPreparacion;
    }

    /** @return el tiempo de cada preparación terminada, por apartamento */
    public ListaDoble<RegistroPreparacion> getPreparacionesTerminadas() {
        return preparacionesTerminadas;
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

    /** @return los pasillos del grafo, con su longitud y si están cerrados */
    public ListaDoble<Pasillo> getPasillos() {
        ListaDoble<Pasillo> pasillos = new ListaDoble<>();
        mapa.getGrafo().recorrerPasillos((a, b, metros, cerrado) -> pasillos.agregarFinal(new Pasillo(a, b, metros, cerrado)));
        return pasillos;
    }

    public void cerrarPasillo(String nodoA, String nodoB) {
        mapa.getGrafo().cerrarPasillo(nodoA, nodoB);
    }

    public void abrirPasillo(String nodoA, String nodoB) {
        mapa.getGrafo().abrirPasillo(nodoA, nodoB);
    }

    /** @return nodos que no se pueden alcanzar desde recepción con los pasillos cerrados actuales */
    public Recorrido<String> inalcanzables() {
        return mapa.getRecepcion() == null ? new Recorrido<>() : getAnalisisGrafo().inalcanzablesDesdeRecepcion();
    }

    /** @return el orden en que BFS visita el edificio desde recepción */
    public Recorrido<String> recorridoBfs() {
        return mapa.getRecepcion() == null ? new Recorrido<>() : mapa.getGrafo().bfs(mapa.getRecepcion());
    }

    /** @return el orden en que DFS (recursivo) visita el edificio desde recepción */
    public Recorrido<String> recorridoDfs() {
        return mapa.getRecepcion() == null ? new Recorrido<>() : mapa.getGrafo().dfs(mapa.getRecepcion());
    }

    /**
     * Agrupa por bloque con BFS: recorre el edificio desde recepción pasando solo por zonas
     * comunes y apartamentos del bloque, y devuelve los apartamentos alcanzados en orden de visita.
     */
    public Recorrido<String> recorridoBloque(String bloque) {
        Recorrido<String> resultado = new Recorrido<>();
        if (mapa.getRecepcion() == null) {
            return resultado;
        }
        for (String nodo : mapa.getGrafo().bfs(mapa.getRecepcion(), codigo -> {
            Apartamento apartamento = buscarApartamento(codigo);
            return apartamento == null || apartamento.getBloque().equalsIgnoreCase(bloque);
        })) {
            Apartamento apartamento = buscarApartamento(nodo);
            if (apartamento != null) {
                resultado.agregar(nodo);
            }
        }
        return resultado;
    }

    // ===================================================== gestión de catálogos

    /** Modifica los datos de un apartamento (el código es la clave del árbol y no cambia). */
    public void modificarApartamento(String codigo, String nombre, String bloque, int piso, int dormitorios,
                                     int capacidad, String caracteristicas) {
        Apartamento apartamento = obtenerApartamento(codigo);
        apartamento.setNombre(nombre);
        apartamento.setBloque(bloque);
        apartamento.setPiso(piso);
        apartamento.setNumeroDormitorios(dormitorios);
        apartamento.setCapacidadMaxima(capacidad);
        apartamento.setCaracteristicas(caracteristicas);
    }

    /** Modifica el perfil de un huésped (el documento es la clave del árbol y no cambia). */
    public void modificarHuesped(String documento, String nombre, LocalDate fechaNacimiento, String telefono,
                                 String correo, String rutaFoto) {
        Huesped huesped = buscarHuesped(documento);
        if (huesped == null) {
            throw new IllegalArgumentException("No existe el huésped " + documento + ".");
        }
        huesped.setNombre(nombre);
        huesped.setFechaNacimiento(fechaNacimiento);
        huesped.setTelefono(telefono);
        huesped.setCorreo(correo);
        huesped.setRutaFoto(rutaFoto);
    }

    /** Modifica nombre y, si se indica, contraseña de un empleado (el id es la clave). */
    public void modificarEmpleado(String id, String nombre, String contrasenaNueva) {
        Empleado empleado = buscarEmpleado(id);
        if (empleado == null) {
            throw new IllegalArgumentException("No existe el empleado " + id + ".");
        }
        empleado.setNombre(nombre);
        if (contrasenaNueva != null && !contrasenaNueva.isBlank()) {
            empleado.setContrasena(contrasenaNueva);
        }
    }


    // ======================================================== tablero de inicio

    /** @return la reserva En curso del apartamento (el grupo alojado ahora) o null */
    public Reserva reservaEnCurso(Apartamento apartamento) {
        for (Reserva reserva : reservasDe(apartamento)) {
            if (reserva.getEstado() == EstadoReserva.ENCURSO) {
                return reserva;
            }
        }
        return null;
    }

    /** @return la reserva Confirmada que entra hoy al apartamento o null */
    public Reserva entradaDeHoy(Apartamento apartamento) {
        for (Reserva reserva : reservasDe(apartamento)) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA && hoy().equals(reserva.getFechaInicio())) {
                return reserva;
            }
        }
        return null;
    }

    /** @return las reservas Confirmadas que entran hoy (los check-ins pendientes del día) */
    public ListaDoble<Reserva> entradasDeHoy() {
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        for (Reserva reserva : servicioReservas.getReservas()) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA && hoy().equals(reserva.getFechaInicio())) {
                resultado.agregarFinal(reserva);
            }
        }
        return resultado;
    }

    /**
     * @return la reserva que ocupa el apartamento esa noche (cualquier estado salvo Cancelada y
     *         No-show, que liberaron sus noches), o null si la noche está libre
     */
    public Reserva ocupacionEn(Apartamento apartamento, LocalDate noche) {
        for (Reserva reserva : reservasDe(apartamento)) {
            if (reserva.getEstado() != EstadoReserva.CANCELADA && reserva.getEstado() != EstadoReserva.NOSHOW
                    && reserva.ocupaNoche(noche)) {
                return reserva;
            }
        }
        return null;
    }

    /**
     * @return true si el apartamento estuvo o está Fuera de servicio esa noche (según el historial
     *         de bloqueos; un bloqueo vigente cubre desde su inicio en adelante)
     */
    public boolean bloqueadaEn(Apartamento apartamento, LocalDate noche) {
        for (co.edu.uniquindio.apartasuitesuq.model.Servicios.RegistroFueraDeServicio.Registro registro
                : preparacion.historialFueraDeServicio()) {
            if (registro.getCodigoApartamento().equalsIgnoreCase(apartamento.getCodigo())
                    && !noche.isBefore(registro.getDesde().toLocalDate())
                    && (registro.getHasta() == null || noche.isBefore(registro.getHasta().toLocalDate()))) {
                return true;
            }
        }
        return false;
    }

    /** @return la próxima reserva Pendiente o Confirmada del apartamento desde hoy, o null */
    public Reserva proximaReserva(Apartamento apartamento) {
        for (Reserva reserva : reservasDe(apartamento)) { // ordenadas por fecha de entrada
            if ((reserva.getEstado() == EstadoReserva.PENDIENTE || reserva.getEstado() == EstadoReserva.CONFIRMADA)
                    && !reserva.getFechaInicio().isBefore(hoy())) {
                return reserva;
            }
        }
        return null;
    }

    /** @return cuántos apartamentos hay en cada estado, indexado por {@code EstadoApartamento.ordinal()} */
    public int[] contarPorEstado() {
        int[] conteo = new int[EstadoApartamento.values().length];
        for (Apartamento apartamento : apartamentos) {
            conteo[apartamento.getEstadoOperativo().ordinal()]++;
        }
        return conteo;
    }

    /** @return porcentaje de apartamentos Ocupados en este momento */
    public double ocupacionActual() {
        int total = apartamentos.getTamano();
        return total == 0 ? 0 : contarPorEstado()[EstadoApartamento.OCUPADO.ordinal()] * 100.0 / total;
    }

    /**
     * @return prioridad del apartamento en la cola de preparación: 1 si tiene entrada hoy,
     *         2 si no; 0 si no está en la cola
     */
    public int prioridadPreparacion(Apartamento apartamento) {
        for (ColaPreparacion.Item item : colaPreparacion) {
            if (item.getApartamento().equals(apartamento)) {
                return item.getPrioridad();
            }
        }
        return 0;
    }

    /**
     * Filtra el catálogo (recorrido inorden del árbol, así que sale ordenado por código).
     *
     * @param estados estados aceptados; null o vacío = todos
     * @param bloque  bloque exacto; null = todos
     * @param piso    piso exacto; null = todos
     */
    public ListaDoble<Apartamento> filtrarApartamentos(EstadoApartamento[] estados, String bloque, Integer piso) {
        ListaDoble<Apartamento> resultado = new ListaDoble<>();
        for (Apartamento apartamento : apartamentos) {
            boolean estadoOk = estados == null || estados.length == 0;
            if (!estadoOk) {
                for (EstadoApartamento estado : estados) {
                    if (apartamento.getEstadoOperativo() == estado) {
                        estadoOk = true;
                        break;
                    }
                }
            }
            if (estadoOk && (bloque == null || apartamento.getBloque().equalsIgnoreCase(bloque))
                    && (piso == null || apartamento.getPiso() == piso)) {
                resultado.agregarFinal(apartamento);
            }
        }
        return resultado;
    }

    /** @return los apartamentos de las reservas activas del huésped con ese documento (sin repetir) */
    public ListaDoble<Apartamento> apartamentosDeHuesped(String documento) {
        ListaDoble<Apartamento> resultado = new ListaDoble<>();
        Huesped huesped = buscarHuesped(documento);
        if (huesped == null) {
            return resultado;
        }
        for (Reserva reserva : reservasDe(huesped)) {
            if (reserva.estaActiva() && !resultado.buscar(reserva.getApartamento())) {
                resultado.agregarFinal(reserva.getApartamento());
            }
        }
        return resultado;
    }

    /** @return los bloques distintos del catálogo, en orden de aparición (inorden) */
    public ListaDoble<String> bloques() {
        ListaDoble<String> resultado = new ListaDoble<>();
        for (Apartamento apartamento : apartamentos) {
            if (!resultado.buscar(apartamento.getBloque())) {
                resultado.agregarFinal(apartamento.getBloque());
            }
        }
        return resultado;
    }

    /** @return los pisos distintos del catálogo */
    public ListaDoble<Integer> pisos() {
        ListaDoble<Integer> resultado = new ListaDoble<>();
        for (Apartamento apartamento : apartamentos) {
            if (!resultado.buscar(apartamento.getPiso())) {
                resultado.agregarFinal(apartamento.getPiso());
            }
        }
        return resultado;
    }

    /**
     * Check-in desde el tablero: busca la reserva Confirmada que entra hoy al apartamento.
     * @throws TransicionEstadoInvalidaException si no hay entrada hoy o el apartamento no está Preparado
     */
    public Reserva checkInDeHoy(String codigoApartamento) {
        Apartamento apartamento = obtenerApartamento(codigoApartamento);
        Reserva reserva = entradaDeHoy(apartamento);
        if (reserva == null) {
            throw new TransicionEstadoInvalidaException("El apartamento " + apartamento.getCodigo()
                    + " no tiene una reserva confirmada que entre hoy.");
        }
        checkIn(reserva);
        return reserva;
    }

    /**
     * Check-out desde el tablero: finaliza la reserva En curso del apartamento.
     * @throws TransicionEstadoInvalidaException si el apartamento no tiene un grupo alojado
     */
    public Reserva checkOutApartamento(String codigoApartamento) {
        Apartamento apartamento = obtenerApartamento(codigoApartamento);
        Reserva reserva = reservaEnCurso(apartamento);
        if (reserva == null) {
            throw new TransicionEstadoInvalidaException("El apartamento " + apartamento.getCodigo()
                    + " no tiene un grupo alojado.");
        }
        checkOut(reserva);
        return reserva;
    }

    /** @return ruta más corta (Dijkstra) desde recepción hasta el apartamento, con su distancia */
    public Recorrido<String> rutaHacia(String codigoApartamento) {
        if (mapa.getRecepcion() == null) {
            throw new IllegalStateException("El mapa del edificio no tiene recepción.");
        }
        return getAnalisisGrafo().rutaDesdeRecepcion(obtenerApartamento(codigoApartamento).getCodigo());
    }

    // ============================================================= estadísticas

    /** @return los reportes calculados sobre el estado actual del sistema */
    public Estadisticas getEstadisticas() {
        return new Estadisticas(this);
    }
}
