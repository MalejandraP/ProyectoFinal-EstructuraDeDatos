# Guion de la demostración (≈ 12 minutos)

Preparación: `cd Aparta-SuitesUQ && ./mvnw javafx:run`. Las fechas del escenario son relativas al
día de la demo (`HOY`, `HOY+3`...), así que siempre hay llegadas "hoy" y reservas vigentes.

| Usuario | Id / documento | Contraseña |
| --- | --- | --- |
| Administrador | `admin` | `admin123` |
| Recepcionista | `R1` (también `R2`, `R3`) | `r1` |
| Huésped | `1094000001` (Ana Gómez) | — |

## 1. Carga de datos (1 min)
1. En la pantalla de ingreso, **Cargar escenario de prueba**: muestra el resumen (8 apartamentos,
   7 huéspedes, 4 empleados, 12 nodos del mapa...).
2. Explicar el archivo plano `datos/escenario.txt`: secciones, fechas relativas y que un error
   indica la línea y no deja el sistema a medias.

## 2. Administrador (5 min) — `admin / admin123`
1. **Inicio**: tablero con los colores del enunciado. A102 azul (reserva en curso), A103/B101/B201
   amarillos (pendientes de preparación), A202 rojo (pasar el mouse: motivo "Fuga de agua...").
2. **Temporadas y tarifas**: periodos ordenados por fecha; intentar agregar uno que se cruce →
   `FechasInvalidasException`. Consultar qué temporada es el 20 de diciembre. Cambiar una tarifa.
3. **Cotizar estancia**: A101 del 12 al 17 de diciembre, 4 personas, ocupantes: un adulto, una
   niña nacida el 14/12/2020 y un bebé. Mostrar el desglose: Media → Alta, la niña empieza a pagar
   el 14 (cumple 6), el bebé nunca paga. Explicar la **recursión** de `LiquidadorTarifas` y el
   **Iterator** de `Estancia`. Buscar disponibles ordenando por precio y por capacidad
   (**Comparator** con `thenComparing`).
4. **Estadísticas**: ocupación por apartamento y por temporada, ingresos por mes, reservas por
   canal con canceladas y no-show (RES-7, RES-8), conflictos y tiempo promedio de preparación.
5. **Atrás** varias veces: el historial es una **Pila** propia.

## 3. Recepcionista (4 min) — `R1 / r1`
1. Encabezado: **turno actual** y **Pasar turno** (lista circular R1 → R2 → R3 → R1).
2. **Mapa y preparación**: la cola de preparación pone primero **B101** (llegan hoy a las 11:00),
   luego **A103** (hoy 14:00) y después **B201** (sin entrada hoy) → cola de prioridad con
   `thenComparing`. **Mostrar ruta de preparación** (Dijkstra sobre el grafo). Iniciar y terminar
   la preparación de B101: vuelve a verde.
3. **Folio**: pantalla del folio del equipo (cargos y pagos; deshacer registra el movimiento contrario usando una pila).

## 4. Huésped (1 min) — documento `1094000001`
1. **Mi panel**: sus reservas reales del escenario (RES-8 no-show y RES-9 de diciembre).
2. **Cotizar estancia**: como titular ya aparece en los ocupantes con su fecha de nacimiento.

## 5. Cierre (1 min)
- `./mvnw test`: más de 80 pruebas JUnit (estancia, temporadas, liquidación, reserva, fachada,
  carga de datos, estadísticas, grafo, colas y servicios de preparación).
- Diagramas en `docs/`.
