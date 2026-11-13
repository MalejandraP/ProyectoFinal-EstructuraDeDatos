# SGA – Sistema de Gestión de Alojamiento · Aparta-Suites UQ

Aplicación de escritorio (Java + JavaFX) para administrar el alojamiento **Aparta-Suites UQ**
(Salento, Quindío): apartamentos, huéspedes, reservas por tres canales, folios, temporadas y
tarifas, preparación de apartamentos con rutas sobre el mapa del edificio y estadísticas.
Todo el núcleo se apoya en **estructuras de datos propias, genéricas e iterables**, sin
colecciones de `java.util`.

Proyecto final de Estructuras de Datos – Universidad del Quindío.

## Requisitos y ejecución

- JDK 24 o superior (el proyecto compila con `release 24`).
- No hace falta instalar Maven: se usa el wrapper incluido.

```bash
cd Aparta-SuitesUQ
./mvnw javafx:run     # abre la aplicación
./mvnw test           # ejecuta las pruebas JUnit 5
```

En la pantalla de ingreso, **Cargar escenario de prueba** llena el sistema con
[`escenario.txt`](Aparta-SuitesUQ/src/main/resources/co/edu/uniquindio/apartasuitesuq/datos/escenario.txt).

| Rol | Usuario | Contraseña |
| --- | --- | --- |
| Administrador | `admin` | `admin123` |
| Recepcionista | `R1`, `R2`, `R3` | `r1`, `r2`, `r3` |
| Huésped | documento, p. ej. `1094000001` | — |

## Funcionalidades por rol

- **Huésped:** panel con sus reservas; cotización con desglose noche por noche; búsqueda de
  disponibilidad ordenada por precio, capacidad, dormitorios o popularidad.
- **Recepcionista:** tablero de estados, mapa del edificio con la ruta de preparación (Dijkstra),
  cola de preparación con prioridad, folio y rotación de turnos.
- **Administrador:** todo lo anterior más temporadas y tarifas, estadísticas (ocupación,
  ingresos, canales, conflictos, tiempo de preparación) y carga de escenarios desde archivo.

## Reglas del negocio implementadas

- Una estancia es el intervalo `[entrada, salida)`: la noche de salida no se cobra ni se ocupa.
- Se cobra **por persona y por noche** con la tarifa de la **temporada de cada noche** (Baja,
  Media, Alta); los ocupantes **menores de 6 años** ocupan cupo pero no pagan, y la edad se
  evalúa noche a noche.
- Un apartamento no puede tener dos reservas activas que compartan una noche, sin importar el canal.
- Ciclo de vida: Pendiente → Confirmada → En curso → Finalizada, o Cancelada / No-show.
- Portal: anticipo del 30 % y vencimiento automático a las 24 h.
- Check-in solo con el apartamento Preparado; el check-out lo deja Pendiente de preparación.

## Estructura del código

```
Aparta-SuitesUQ/src/main/java/co/edu/uniquindio/apartasuitesuq/
├── HelloApplication.java      punto de entrada (pantalla de ingreso)
├── view/                      ventana principal por rol, cotización, temporadas, estadísticas, carga
├── viewController/            panel de usuario y folio (FXML)
└── model/
    ├── SGA.java               fachada: único punto de acceso de la GUI al modelo
    ├── CargadorDatos.java     carga de escenarios desde archivo plano
    ├── Estadisticas.java      reportes del administrador
    ├── Estructuras/           estructuras propias genéricas + grafo del edificio
    ├── Servicios/             reservas, canal externo, turnos, preparación, recepción,
    │                          vencimientos, liquidación de tarifas, asignación de grupos
    ├── Excepciones/           excepciones propias
    └── ViewPrueba/            vistas del mapa, tablero y panel de preparación
```

## Conceptos de Java exigidos

| Concepto | Dónde |
| --- | --- |
| Genéricos | Todas las estructuras de `model.Estructuras` (`ListaDoble<T>`, `Cola<T>`, `Pila<T>`, `ColaPrioridad<T>`, `ArbolBinarioBusqueda<T extends Comparable<T>>`, `Grafo<T>`, `Conjunto<T>`). |
| Comparable | `Apartamento` por código, `Huesped` por documento, `Reserva` por fecha de entrada (desempate por código), `Empleado` por id, `PeriodoTemporada` por inicio. |
| Comparator | `OrdenApartamentos` (precio, capacidad, dormitorios, número de reservas), cola de preparación (`thenComparing`), vencimientos. |
| Iterable / Iterator | Todas las estructuras propias y `Estancia` (`for (LocalDate noche : estancia)`). |
| Recursividad | Árbol binario (insertar, buscar, eliminar, altura, recorridos), DFS del grafo, liquidación noche por noche (`LiquidadorTarifas.liquidarDesde`), asignación de grupos con backtracking. |
| Excepciones propias | `ReservaSolapadaException`, `CapacidadExcedidaException`, `FechasInvalidasException`, `TransicionEstadoInvalidaException`, `EstructuraVaciaException`. |

## Documentación

- [Diagrama de clases](docs/diagrama-clases.md)
- [Diagrama de estructuras propias](docs/diagrama-estructuras.md)
- [Guion de la demostración](docs/guion-demo.md)

## Equipo

| Integrante | Parte |
| --- | --- |
| Alejandra | P1 – estructuras lineales, folio, canal externo y turnos |
| Camila | P2 – árbol binario de búsqueda y servicio de reservas |
| Alejandro | P3 – grafo del edificio, cola de prioridad, preparación y vencimientos |
| Jerónimo | P4 – modelo de dominio, estancia, temporadas, liquidación, fachada, carga de datos, estadísticas, ventana principal y documentación |
