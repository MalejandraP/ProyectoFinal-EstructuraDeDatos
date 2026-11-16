# Trazabilidad de la GUI – SGA Aparta-Suites UQ

Cada funcionalidad del código tiene su control en la GUI (JavaFX + FXML, MVC). Los controladores
de vista (`viewController`) solo leen la pantalla, llaman a la fachada `SGA` y muestran el
resultado; las reglas de negocio viven en el modelo. Las estructuras propias se recorren con su
`Iterator` y se copian a una `ObservableList` solo para mostrarlas (`VistaUtil.lista`).

Archivos FXML en `src/main/resources/co/edu/uniquindio/apartasuitesuq/`; controladores en
`co.edu.uniquindio.apartasuitesuq.viewController`; datos compartidos en `controller.DatosPrueba`.

## K. Navegación y roles — `login.fxml`, `principal.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `SGA.autenticar` (empleado) / `SGA.buscarHuesped` (huésped, árbol) | ToggleButton, TextField, PasswordField | `tgEmpleado`, `tgHuesped`, `txtUsuario`, `txtContrasena`, `#onIngresar` |
| `DatosPrueba.cargarEscenario()` | Button + resumen | `#onCargarEscenario`, `lblResumen` |
| Menú lateral por `Rol` (enum `Pantalla`) | Button por pantalla (`userData`) | `btnInicio`…`btnCarga`, `#onNavegar` |
| Atrás con `Pila<Pantalla>` | Button, Label | `btnAtras`, `#onAtras`, `lblHistorial` |
| Usuario y rol actual | Label avatar, chip | `lblAvatar`, `lblUsuario`, `lblRol` |
| `turnoActual()`, `pasarTurno()` (lista circular) | Label, Button | `lblTurno`, `#onPasarTurno` |

## A. Inicio — `inicio.fxml`, `tarjeta-apartamento.fxml`, `detalle-apartamento.fxml`, `ficha-huesped.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `ocupacionActual()`, `entradasDeHoy()`, `contarPorEstado()` | Tiles con punto del semáforo | `lblOcupacion`, `lblCheckins`, `lblPreparados`, `lblOcupados`, `lblPendientes`, `lblFuera` |
| `filtrarApartamentos(estados, bloque, piso)`, `bloques()`, `pisos()` | ToggleButton ×5, ComboBox ×2 | `grupoEstado`, `cbBloque`, `cbPiso`, `#onFiltrar`, `#onLimpiar` |
| `buscarApartamento` (árbol) / `apartamentosDeHuesped` | TextField + Button | `txtCodigo` `#onBuscarCodigo`, `txtHuesped` `#onBuscarHuesped` |
| Tarjeta: código, nombre, bloque, piso, dormitorios, capacidad, estado con color | FXML por apartamento | `TarjetaApartamentoViewController.mostrar` |
| Contexto: `entradaDeHoy`, `reservaEnCurso`, `prioridadPreparacion`, `proximaReserva`, `motivoFueraDeServicio` | Label | `lblContexto` |
| Detalle: tarifas por temporada, `reservasDe(apartamento)` | Diálogo con TableView | `onDetalle` |
| `checkInDeHoy` (solo Preparado) / `checkOutApartamento` | Button | `btnCheckIn`, `btnCheckOut` |
| `rutaHacia` (Dijkstra) | Button → mapa con la ruta | `btnRuta` → `PrincipalViewController.abrirMapa` |
| Ficha del huésped: perfil, edad, foto opcional, favoritos (`Conjunto`) | Diálogo | `btnHuesped` |

## B. Reservas — `reservas.fxml`, `canal-externo.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `buscarDisponibles` + `OrdenApartamentos` (precio, capacidad, dormitorios, más reservados) | DatePicker ×2, Spinner, ComboBox, TableView | `dpEntrada`, `dpSalida`, `spPersonas`, `cbOrden`, `tablaDisponibles` |
| `cotizar` + `Cotizacion.getDesglose()` (Iterator de `Estancia`, liquidación recursiva) | TableView desglose, totales | `#onCotizar`, `tablaDesglose`, `lblTotal`, `lblAnticipo` |
| `crearReserva` (titular nuevo o existente, canal Directa/Portal, ocupantes con nacimiento) | Formulario + ListView | `txtDocumento` `#onBuscarTitular`, `cbCanal`, `listaOcupantes`, `#onAgregarOcupante`, `#onCrearReserva` |
| `asignarGrupoGrande` (backtracking) + `reservarGrupo` | Pestaña «Grupos grandes» | `#onBuscarGrupo`, `tablaGrupo`, `#onReservarGrupo` |
| `getReservas()` (árbol por fecha) / `reservasOrdenadasPorCodigo()` / `buscarReserva` | TableView + filtros | `tablaReservas`, `tgPorFecha`, `tgPorCodigo`, `#onBuscarReserva` |
| `confirmarReserva`, `pagarAnticipo`, `checkIn`, `checkOut`, `marcarNoShow` | Botones según el estado | `btnConfirmar`, `btnAnticipo`, `btnCheckIn`, `btnCheckOut`, `btnNoShow` |
| `calcularDevolucion` (mostrada antes) + `cancelarReserva` | Button + confirmación | `btnCancelar`, `#onCancelar` |
| `procesarVencimientos` (24 h) | Button | `btnVencimientos` |
| `recibirSolicitudExterna` | Formulario | `#onEncolar` |
| `procesarSolicitudCompleta` una a una / todas (FIFO), `getPendientes`, `getProcesadas`, `getConflictosCanal` | Botones + 3 tablas | `#onProcesarSiguiente`, `#onProcesarTodas`, `tablaPendientes`, `tablaProcesadas`, `tablaConflictos` |

## C. Calendario — `calendario.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `ocupacionEn(apartamento, noche)`, `bloqueadaEn(apartamento, noche)` | GridPane apartamentos × noches (ocupada / bloqueada / libre) | `cuadricula` |
| Navegación de meses | Button ◀ ▶ Hoy | `#onMesAnterior`, `#onMesSiguiente`, `#onHoy` |
| Detalle de la celda (reserva y huésped) | Label | `lblDetalle` |

## D. Panel de usuario — `panel-usuario.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| Perfil, `getEdad()`, foto opcional | Labels, ImageView | `lblNombre`, `lblNacimiento`, `imgFoto` |
| `reservasDe(huesped)` con total, pagado (`totalPago`) y `saldoDe` | Lista de tarjetas | `listaReservas` |
| Folio de solo lectura | TableView + totales | `tablaFolio`, `lblSaldoPendiente` |
| `agregarFavorito` / `quitarFavorito` (`Conjunto`, sin repetidos) | ComboBox + botones | `cmbApartamentos`, `#onAgregarFavorito`, `#onQuitarFavorito` |
| `getHistorial()` (más reciente primero) | ListView | `listaHistorial` |
| `pagarAnticipo`, `calcularDevolucion` + `cancelarReserva`, nueva reserva | Botones de cada tarjeta | `onPagarAnticipo`, `onCancelarReserva`, `#onSolicitarReserva` |

## E. Preparación — `preparacion.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `colaEnOrden()` (cola de prioridad: 1 entrada hoy por hora, 2 el resto) | TableView | `tablaCola` |
| `iniciarSiguientePreparacion` → En preparación | Button | `btnDespachar` |
| `getEnPreparacion`, `terminarPreparacion` → Preparado | TableView + Button | `tablaEnPreparacion`, `#onTerminar` |
| `rutaPreparacion()` con distancia total | Label + «Ver en el mapa» | `lblRuta`, `lblDistancia`, `#onVerEnMapa` |
| `getPreparacionesTerminadas` (tiempo por apartamento), `tiempoMedioPreparacionMin` | TableView, tile | `tablaTiempos`, `lblPromedio` |

## F. Mapa — `mapa.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `MapaEdificio` + `getPasillos()` dibujados con estados del semáforo | Pane con Circle / Line / Text | `lienzo` |
| `rutaHacia` / `rutaPreparacion` (Dijkstra) dibujada con su costo | ComboBox + botones | `cbDestino`, `#onRutaDestino`, `#onRutaPreparacion`, `lblRuta` |
| (Admin) `cerrarPasillo` / `abrirPasillo` + `inalcanzables()` | ComboBox + botones | `cbPasillo`, `#onCerrarPasillo`, `#onAbrirPasillo`, `lblInalcanzables` |
| (Admin) `recorridoBfs`, `recorridoDfs`, `recorridoBloque` (agrupar por bloque), `componentesConexas`, `contarPasillos` | Botones + Label | `#onBfs`, `#onDfs`, `#onAgruparBloques`, `lblConectividad` |

## G. Folio y turnos — `folio.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `Folio.getMovimientos()`, totales, `saldoDe` | TableView + resumen | `tablaMovimientos`, `lblSaldo` |
| `registrarCargo` / `registrarPago` | ToggleButton + campos | `btnCargo`, `btnPago`, `#onRegistrar` |
| `ultimaOperacion()` + `deshacer` (registra el contrario) | Label + Button | `lblUltimaOperacion`, `#onDeshacer` |
| `ServicioTurnos.getTurnos()` (lista circular), `pasarTurno` | ListView + Button | `listaTurnos`, `#onPasarTurno` |

## H. Administración — `administracion.fxml`, `temporadas.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `registrar/buscar/eliminar/modificarApartamento` sobre el árbol; `getTamano`, `obtenerAltura`; recorridos in/pre/postorden | Pestaña Apartamentos | `#onGuardarApto`, `#onEliminarApto`, `#onBuscarApto`, `#onInorden`, `#onPreorden`, `#onPostorden` |
| `registrar/buscar/eliminar/modificarHuesped` (foto opcional) | Pestaña Huéspedes | `#onGuardarHuesped`, `#onEliminarHuesped`, `#onElegirFoto` |
| `registrar/buscar/eliminar/modificarEmpleado` (Recepcionista / Administrador) | Pestaña Empleados | `#onGuardarEmpleado`, `#onEliminarEmpleado` |
| `ponerFueraDeServicio(motivo)`, `volverAServicio`, `historialFueraDeServicio` | Pestaña Estado operativo | `#onFueraDeServicio`, `#onVolverAServicio`, `tablaHistorial` |
| `definirTemporada`, `eliminarPeriodo`, `CalendarioTemporadas.temporadaDe` | temporadas.fxml | `#onAgregarPeriodo`, `#onEliminarPeriodo`, `#onConsultar` |
| `definirTarifa` (Baja, Media, Alta) | temporadas.fxml | `tablaTarifas`, `#onGuardarTarifas` |

## I. Estadísticas — `estadisticas.fxml`

| Funcionalidad del código | Componente | fx:id |
| --- | --- | --- |
| `ocupacionPorApartamento`, `ocupacionPorTemporada`, `ocupacionGeneral` | BarChart ×2, tile | `graficoOcupacionApto`, `graficoOcupacionTemporada`, `lblOcupacion` |
| `ingresosPorMes`, `ingresosTotales` | BarChart, tile | `graficoIngresos`, `lblIngresos` |
| `reservasPorCanalYEstado` (incluye canceladas y no-show) | TableView | `tablaCanales` |
| `contarConflictosCanal`, `getConflictosCanal` | Tile + TableView | `lblConflictos`, `tablaConflictos` |
| `tiempoPromedioPreparacionMin`, `getPreparacionesTerminadas` | Tile + TableView | `lblPreparacion`, `tablaTiempos` |

## J. Carga de datos — `carga.fxml`

| Funcionalidad del código | Componente | fx:id / método |
| --- | --- | --- |
| `cargarEscenarioDeEjemplo()` / `cargarEscenario(Path)` (`CargadorDatos`) | Botones + FileChooser + resultado | `#onCargarEjemplo`, `#onCargarArchivo`, `lblResultado` |
| Formato del archivo plano | TextArea | `txtFormato` |

## Notas

- **Suposición:** «Confirmar» deja Confirmada una reserva Pendiente sin registrar pago (decisión de
  recepción); «Pagar anticipo» registra el 30 % (descontando abonos) y confirma.
- **Suposición:** el rol de un empleado no se cambia al editarlo (define su clase); se elimina y se crea de nuevo.
- `Folio.totalCargos()` no existe en el modelo: el folio suma los cargos recorriendo `getMovimientos()`.
- El árbol de apartamentos no se balancea: si el escenario inserta los códigos en orden, su altura es igual al número de nodos (se muestra en Administración).
