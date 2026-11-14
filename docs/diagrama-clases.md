# Diagrama de clases – SGA Aparta-Suites UQ

Vista del modelo de dominio, la fachada y los servicios. Las estructuras propias tienen su
propio diagrama en [diagrama-estructuras.md](diagrama-estructuras.md).

## Dominio

```mermaid
classDiagram
    direction LR

    class Apartamento {
        -String codigo
        -String nombre
        -String bloque
        -int piso
        -int numeroDormitorios
        -int capacidadMaxima
        -String caracteristicas
        -double[] tarifasPorTemporada
        -EstadoApartamento estadoOperativo
        -String motivoFueraServicio
        +getTarifa(Temporada) double
        +setTarifa(Temporada, double)
        +ponerFueraDeServicio(String)
        +soportaCapacidad(int) boolean
        +compareTo(Apartamento) int
    }

    class Huesped {
        -String cedula
        -String nombre
        -LocalDate fechaNacimiento
        -String telefono
        -String correo
        -String rutaFoto
        -Conjunto~Apartamento~ favoritos
        -ListaDoble~Reserva~ historial
        +comoOcupante() Ocupante
        +agregarFavorito(Apartamento) boolean
        +compareTo(Huesped) int
    }

    class Ocupante {
        +EDAD_MINIMA_COBRO = 6$
        -String nombre
        -LocalDate fechaNacimiento
        +edadEn(LocalDate) int
        +pagaEn(LocalDate) boolean
        +contarPagantes(Iterable~Ocupante~, int, LocalDate)$ int
    }

    class Reserva {
        +PORCENTAJE_ANTICIPO = 0.30$
        +HORAS_VENCIMIENTO = 24$
        -String codigoReserva
        -LocalDate fechaInicio
        -LocalDate fechaFin
        -EstadoReserva estado
        -double costoTotal
        -Canal canal
        -int numeroPersonas
        -ListaDoble~Ocupante~ ocupantes
        -ListaDoble~Huesped~ huespedes
        -LocalDateTime fechaCreacion
        +getEstancia() Estancia
        +cambiarEstado(EstadoReserva)
        +seSolapaCon(LocalDate, LocalDate) boolean
        +contarPagantesEn(LocalDate) int
        +estaVencida(LocalDateTime) boolean
        +compareTo(Reserva) int
    }

    class Estancia {
        -LocalDate entrada
        -LocalDate salida
        +getNumeroNoches() int
        +contiene(LocalDate) boolean
        +seSolapaCon(Estancia) boolean
        +iterator() Iterator~LocalDate~
    }

    class Folio {
        -ListaDoble~Movimiento~ movimientos
        -Pila~Movimiento~ operaciones
        +registrarCargo(double, String)
        +registrarPago(double, String)
        +deshacer()
        +calcularSaldo() double
    }

    class Movimiento {
        -TipoMovimiento tipo
        -double monto
        -String descripcion
        -LocalDateTime fechayHora
        +crearReverso() Movimiento
    }

    class Empleado {
        <<abstract>>
        -String id
        -String nombre
        -String contrasena
        +getRol()* Rol
        +verificarContrasena(String) boolean
        +compareTo(Empleado) int
    }
    class Recepcionista
    class Administrador

    class EstadoApartamento {
        <<enumeration>>
        PREPARADO
        OCUPADO
        PENDIENTEDEPREPARACION
        ENPREPARACION
        FUERADESERVICIO
    }
    class EstadoReserva {
        <<enumeration>>
        PENDIENTE
        CONFIRMADA
        ENCURSO
        FINALIZADA
        CANCELADA
        NOSHOW
    }
    class Canal {
        <<enumeration>>
        PORTAL
        DIRECTA
        EXTERNO
    }
    class Temporada {
        <<enumeration>>
        BAJA
        MEDIA
        ALTA
    }
    class Rol {
        <<enumeration>>
        HUESPED
        RECEPCIONISTA
        ADMINISTRADOR
    }

    Empleado <|-- Recepcionista
    Empleado <|-- Administrador
    Empleado ..> Rol
    Reserva "*" --> "1" Huesped : titular
    Reserva "*" --> "1" Apartamento
    Reserva "1" *-- "1" Folio
    Reserva "1" *-- "*" Ocupante
    Reserva ..> Estancia : getEstancia()
    Reserva --> EstadoReserva
    Reserva --> Canal
    Folio "1" *-- "*" Movimiento
    Apartamento --> EstadoApartamento
    Apartamento ..> Temporada : tarifa por temporada
    Huesped ..> Ocupante : comoOcupante()
```

## Temporadas, tarifas y liquidación

```mermaid
classDiagram
    direction LR

    class PeriodoTemporada {
        -Temporada temporada
        -LocalDate inicio
        -LocalDate fin
        +contiene(LocalDate) boolean
        +seSolapaCon(PeriodoTemporada) boolean
    }
    class CalendarioTemporadas {
        -ListaDoble~PeriodoTemporada~ periodos
        +agregarPeriodo(PeriodoTemporada)
        +eliminarPeriodo(PeriodoTemporada) boolean
        +temporadaDe(LocalDate) Temporada
        +tarifaPorPersona(Apartamento, LocalDate) double
    }
    class LiquidadorTarifas {
        -CalendarioTemporadas calendario
        +cotizar(Apartamento, Estancia, ListaDoble~Ocupante~, int) Cotizacion
        +cotizar(Reserva) Cotizacion
        +liquidar(Reserva) double
        -liquidarDesde(Iterator~LocalDate~, ...) double
    }
    class Cotizacion {
        -Estancia estancia
        -int numeroPersonas
        -ListaDoble~NocheLiquidada~ desglose
        -double total
        +getAnticipo() double
        +contarNochesEn(Temporada) int
    }
    class NocheLiquidada {
        -LocalDate noche
        -Temporada temporada
        -double tarifaPorPersona
        -int personasQuePagan
        -int personasTotales
        +getSubtotal() double
    }

    CalendarioTemporadas "1" *-- "*" PeriodoTemporada
    LiquidadorTarifas --> CalendarioTemporadas
    LiquidadorTarifas ..> Estancia : recorre con Iterator
    LiquidadorTarifas ..> Cotizacion : crea
    Cotizacion "1" *-- "*" NocheLiquidada
```

`liquidarDesde` es **recursivo**: caso base, el iterador no tiene más noches (vale 0); caso
recursivo, el valor de la noche siguiente más la liquidación del resto de la estancia.

## Fachada, servicios y carga de datos

```mermaid
classDiagram
    direction TB

    class SGA {
        -ArbolBinarioBusqueda~Apartamento~ apartamentos
        -ArbolBinarioBusqueda~Huesped~ huespedes
        -ArbolBinarioBusqueda~Empleado~ empleados
        -MapaEdificio mapa
        -CalendarioTemporadas calendario
        +getInstancia()$ SGA
        +autenticar(String, String) Empleado
        +buscarDisponibles(LocalDate, LocalDate, int, Comparator) ListaDoble~Apartamento~
        +cotizar(...) Cotizacion
        +crearReserva(...) Reserva
        +pagarAnticipo(Reserva)
        +procesarSiguienteSolicitud() Reserva
        +checkIn(Reserva)
        +checkOut(Reserva)
        +rutaPreparacion() Recorrido~String~
        +cargarEscenario(Path)
        +getEstadisticas() Estadisticas
    }
    class CargadorDatos {
        +cargar(Path)
        +cargar(Reader)
        +cargarEjemplo()
    }
    class Estadisticas {
        +ocupacionPorApartamento(Estancia) ListaDoble~Indicador~
        +ocupacionPorTemporada(Estancia) ListaDoble~Indicador~
        +ingresosPorMes(Estancia) ListaDoble~Indicador~
        +reservasPorCanalYEstado(Estancia) int[][]
        +contarConflictosCanal() int
        +tiempoPromedioPreparacionMin() double
    }
    class OrdenApartamentos {
        +porPrecio()$ Comparator
        +porCapacidad()$ Comparator
        +porDormitorios()$ Comparator
        +porNumeroDeReservas(SGA)$ Comparator
    }
    class ServicioReservas {
        -ArbolBinarioBusqueda~Reserva~ arbolReservas
        +crearReserva(...) Reserva
        +registrar(Reserva)
        +cancelar(Reserva) double
        +haySolapamiento(Apartamento, Estancia) boolean
    }
    class ServicioCanalExterno {
        -Cola~SolicitudExterna~ cola
        -ListaDoble~ConflictoCanal~ conflictos
        +procesarSiguiente() Reserva
    }
    class ServicioTurnos {
        -ListaCircular~Recepcionista~ turnos
        +turnoActual() Recepcionista
        +pasarTurno() Recepcionista
    }
    class ServicioPreparacion {
        +checkIn(Apartamento)
        +checkOut(Apartamento, LocalDateTime)
        +iniciarSiguiente() Item
        +rutaSugerida() Recorrido~String~
    }
    class ColaPreparacion {
        -ColaPrioridad~Item~ cola
    }
    class ServicioRecepcion
    class ServicioVencimiento {
        -ColaPrioridad~Seguimiento~ cola
        +procesarVencimientos(LocalDateTime) ListaDoble~Reserva~
    }
    class MapaEdificio {
        -Grafo~String~ grafo
    }

    SGA --> ServicioReservas
    SGA --> ServicioCanalExterno
    SGA --> ServicioTurnos
    SGA --> ServicioPreparacion
    SGA --> ServicioRecepcion
    SGA --> ServicioVencimiento
    SGA --> ColaPreparacion
    SGA --> LiquidadorTarifas
    SGA --> CalendarioTemporadas
    SGA --> MapaEdificio
    SGA ..> Estadisticas : crea
    SGA ..> CargadorDatos : usa
    CargadorDatos --> SGA : llena
    ServicioCanalExterno --> ServicioReservas
    ServicioRecepcion --> ServicioPreparacion
    ServicioPreparacion --> ColaPreparacion
    ServicioPreparacion --> MapaEdificio
```

La interfaz gráfica (paquete `view`) solo habla con `SGA`: `LoginView` → `VentanaPrincipal`, que
abre `CotizacionView`, `TemporadasTarifasView`, `EstadisticasView`, `CargaDatosView` y las
pantallas del equipo (tablero, mapa, preparación, panel de usuario y folio).
