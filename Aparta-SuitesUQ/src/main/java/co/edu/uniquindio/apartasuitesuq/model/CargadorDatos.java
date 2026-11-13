package co.edu.uniquindio.apartasuitesuq.model;

import co.edu.uniquindio.apartasuitesuq.model.Estructuras.MapaEdificio;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.NodoMapa;
import co.edu.uniquindio.apartasuitesuq.model.Estructuras.TipoNodo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Carga un escenario de prueba desde un archivo plano de texto (UTF-8).
 * <p>
 * Formato: el archivo se divide en secciones {@code [NOMBRE]}; cada línea de una sección es un
 * registro con campos separados por {@code ;}. Las líneas vacías y las que empiezan con
 * {@code #} se ignoran. Las secciones pueden venir en cualquier orden: se procesan siempre en
 * el orden de dependencias (temporadas → apartamentos → mapa → empleados → huéspedes →
 * reservas → ocupantes → pagos → estados → solicitudes externas).
 * <pre>
 * [TEMPORADAS]   temporada;inicio;fin
 * [APARTAMENTOS] codigo;nombre;bloque;piso;dormitorios;capacidad;tarifaBaja;tarifaMedia;tarifaAlta;caracteristicas
 * [MAPA]         NODO;codigo;tipo;x;y          (tipo = RECEPCION, ESCALERA, ZONA_ASEO o APARTAMENTO)
 *                PASILLO;nodoA;nodoB;metros
 * [EMPLEADOS]    rol;id;nombre;contrasena      (rol = RECEPCIONISTA o ADMINISTRADOR)
 * [HUESPEDES]    documento;nombre;fechaNacimiento;telefono;correo;rutaFoto(opcional)
 * [RESERVAS]     codigo;documentoTitular;apartamento;entrada;salida;canal;estado;personas;creacion;horaLlegada(opcional)
 * [OCUPANTES]    codigoReserva;nombre;fechaNacimiento      (acompañantes; el titular se agrega solo)
 * [PAGOS]        codigoReserva;monto;descripcion
 * [ESTADOS]      apartamento;estado;motivo(solo para FUERADESERVICIO)
 * [SOLICITUDES_EXTERNAS] idExterno;apartamento;entrada;salida;personas;documentoTitular
 * </pre>
 * Fechas: {@code 2026-12-15} o relativas a hoy, como {@code HOY}, {@code HOY+3} o {@code HOY-2},
 * para que el escenario siga vigente el día de la demostración. Fecha y hora: {@code HOY-1 09:30}.
 * <p>
 * El valor de cada reserva se liquida noche por noche con el calendario cargado y se registra
 * como cargo en su folio.
 */
public class CargadorDatos {

    /** Ruta, dentro de los recursos de la aplicación, del escenario de ejemplo. */
    public static final String RECURSO_EJEMPLO = "/co/edu/uniquindio/apartasuitesuq/datos/escenario.txt";

    private static final String SEPARADOR = ";";

    /** Secciones en el orden en que se procesan. */
    private enum Seccion {
        TEMPORADAS, APARTAMENTOS, MAPA, EMPLEADOS, HUESPEDES, RESERVAS, OCUPANTES, PAGOS, ESTADOS,
        SOLICITUDES_EXTERNAS
    }

    /** Línea del archivo con su número, para reportar errores. */
    private static class Linea {
        final int numero;
        final String[] campos;

        Linea(int numero, String texto) {
            this.numero = numero;
            this.campos = texto.split(SEPARADOR, -1);
            for (int i = 0; i < campos.length; i++) {
                campos[i] = campos[i].trim();
            }
        }
    }

    private final SGA sga;
    /** Líneas de cada sección, indexadas por {@code Seccion.ordinal()}. */
    private final ListaDoble<Linea>[] secciones;

    @SuppressWarnings("unchecked")
    public CargadorDatos(SGA sga) {
        if (sga == null) {
            throw new IllegalArgumentException("El sistema destino es obligatorio.");
        }
        this.sga = sga;
        this.secciones = new ListaDoble[Seccion.values().length];
        for (int i = 0; i < secciones.length; i++) {
            secciones[i] = new ListaDoble<>();
        }
    }

    // ================================================================ entrada

    public void cargar(Path archivo) throws IOException {
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            cargar(lector);
        }
    }

    /** Carga el escenario de ejemplo incluido en los recursos de la aplicación. */
    public void cargarEjemplo() throws IOException {
        InputStream entrada = CargadorDatos.class.getResourceAsStream(RECURSO_EJEMPLO);
        if (entrada == null) {
            throw new IOException("No se encontró el escenario de ejemplo " + RECURSO_EJEMPLO);
        }
        try (Reader lector = new InputStreamReader(entrada, StandardCharsets.UTF_8)) {
            cargar(lector);
        }
    }

    /**
     * @throws IOException              si falla la lectura
     * @throws IllegalArgumentException si un registro es inválido; el mensaje indica la línea
     */
    public void cargar(Reader lector) throws IOException {
        leerSecciones(new BufferedReader(lector));
        procesar(Seccion.TEMPORADAS);
        procesar(Seccion.APARTAMENTOS);
        procesarMapa();
        procesar(Seccion.EMPLEADOS);
        procesar(Seccion.HUESPEDES);
        procesar(Seccion.RESERVAS);
        procesar(Seccion.OCUPANTES);
        liquidarReservas();
        procesar(Seccion.PAGOS);
        procesar(Seccion.ESTADOS);
        procesar(Seccion.SOLICITUDES_EXTERNAS);
    }

    private void leerSecciones(BufferedReader lector) throws IOException {
        Seccion actual = null;
        String texto;
        int numero = 0;
        while ((texto = lector.readLine()) != null) {
            numero++;
            texto = texto.strip();
            if (numero == 1 && texto.startsWith("﻿")) {
                texto = texto.substring(1); // BOM de algunos editores
            }
            if (texto.isEmpty() || texto.startsWith("#")) {
                continue;
            }
            if (texto.startsWith("[") && texto.endsWith("]")) {
                actual = seccionDe(texto.substring(1, texto.length() - 1), numero);
            } else if (actual == null) {
                throw error(numero, "el registro está antes de cualquier sección [NOMBRE].");
            } else {
                secciones[actual.ordinal()].agregarFinal(new Linea(numero, texto));
            }
        }
    }

    private static Seccion seccionDe(String nombre, int numero) {
        try {
            return Seccion.valueOf(nombre.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw error(numero, "sección desconocida [" + nombre + "].");
        }
    }

    /** Procesa cada línea de la sección y le agrega el número de línea a cualquier error. */
    private void procesar(Seccion seccion) {
        for (Linea linea : secciones[seccion.ordinal()]) {
            try {
                switch (seccion) {
                    case TEMPORADAS -> cargarTemporada(linea);
                    case APARTAMENTOS -> cargarApartamento(linea);
                    case EMPLEADOS -> cargarEmpleado(linea);
                    case HUESPEDES -> cargarHuesped(linea);
                    case RESERVAS -> cargarReserva(linea);
                    case OCUPANTES -> cargarOcupante(linea);
                    case PAGOS -> cargarPago(linea);
                    case ESTADOS -> cargarEstado(linea);
                    case SOLICITUDES_EXTERNAS -> cargarSolicitud(linea);
                    default -> throw new IllegalStateException("Sección sin procesador: " + seccion);
                }
            } catch (CargaInvalida e) {
                throw e;
            } catch (RuntimeException e) {
                throw new CargaInvalida("Línea " + linea.numero + " [" + seccion + "]: " + e.getMessage(), e);
            }
        }
    }

    // ============================================================== secciones

    private void cargarTemporada(Linea l) {
        exigirCampos(l, 3);
        sga.definirTemporada(Temporada.valueOf(l.campos[0].toUpperCase()), fecha(l.campos[1]), fecha(l.campos[2]));
    }

    private void cargarApartamento(Linea l) {
        exigirCampos(l, 9);
        Apartamento apartamento = new Apartamento(l.campos[0], l.campos[1], l.campos[2],
                entero(l.campos[3]), entero(l.campos[4]), entero(l.campos[5]), decimal(l.campos[6]),
                opcional(l, 9));
        apartamento.setTarifa(Temporada.MEDIA, decimal(l.campos[7]));
        apartamento.setTarifa(Temporada.ALTA, decimal(l.campos[8]));
        sga.registrarApartamento(apartamento);
    }

    /**
     * El mapa se arma completo y después se entrega al SGA, porque los servicios de
     * preparación necesitan conocer la recepción desde el principio.
     */
    private void procesarMapa() {
        ListaDoble<Linea> lineas = secciones[Seccion.MAPA.ordinal()];
        if (lineas.estaVacia()) {
            return;
        }
        MapaEdificio mapa = new MapaEdificio();
        for (int pasada = 0; pasada < 2; pasada++) { // primero los nodos, luego los pasillos
            for (Linea l : lineas) {
                try {
                    String tipo = l.campos[0].toUpperCase();
                    if (pasada == 0 && tipo.equals("NODO")) {
                        exigirCampos(l, 5);
                        TipoNodo tipoNodo = TipoNodo.valueOf(l.campos[2].toUpperCase());
                        if (tipoNodo == TipoNodo.APARTAMENTO) {
                            sga.obtenerApartamento(l.campos[1]); // el nodo debe ser un apartamento del catálogo
                        }
                        mapa.agregarNodo(new NodoMapa(l.campos[1], tipoNodo, decimal(l.campos[3]), decimal(l.campos[4])));
                    } else if (pasada == 1 && tipo.equals("PASILLO")) {
                        exigirCampos(l, 4);
                        mapa.agregarPasillo(l.campos[1], l.campos[2], decimal(l.campos[3]));
                    } else if (!tipo.equals("NODO") && !tipo.equals("PASILLO")) {
                        throw new IllegalArgumentException("se esperaba NODO o PASILLO y llegó " + l.campos[0]);
                    }
                } catch (RuntimeException e) {
                    throw new CargaInvalida("Línea " + l.numero + " [MAPA]: " + e.getMessage(), e);
                }
            }
        }
        sga.definirMapa(mapa);
    }

    private void cargarEmpleado(Linea l) {
        exigirCampos(l, 4);
        Rol rol = Rol.valueOf(l.campos[0].toUpperCase());
        Empleado empleado = switch (rol) {
            case RECEPCIONISTA -> new Recepcionista(l.campos[2], l.campos[1], l.campos[3]);
            case ADMINISTRADOR -> new Administrador(l.campos[2], l.campos[1], l.campos[3]);
            default -> throw new IllegalArgumentException("el rol " + rol + " no es de un empleado.");
        };
        sga.registrarEmpleado(empleado);
    }

    private void cargarHuesped(Linea l) {
        exigirCampos(l, 5);
        sga.registrarHuesped(new Huesped(l.campos[0], l.campos[1], fecha(l.campos[2]),
                l.campos[3], l.campos[4], opcional(l, 5)));
    }

    private void cargarReserva(Linea l) {
        exigirCampos(l, 9);
        Huesped titular = huesped(l.campos[1]);
        Apartamento apartamento = sga.obtenerApartamento(l.campos[2]);
        Reserva reserva = new Reserva(l.campos[0], titular, apartamento, fecha(l.campos[3]), fecha(l.campos[4]),
                EstadoReserva.valueOf(l.campos[6].toUpperCase()), 0, Canal.valueOf(l.campos[5].toUpperCase()),
                entero(l.campos[7]), fechaHora(l.campos[8]));
        String hora = opcional(l, 9);
        if (!hora.isEmpty()) {
            reserva.setHoraEstimadaLlegada(LocalTime.parse(hora));
        }
        sga.registrarReservaExistente(reserva);
        if (reserva.getEstado() == EstadoReserva.ENCURSO) {
            apartamento.setEstadoOperativo(EstadoApartamento.OCUPADO);
        }
    }

    private void cargarOcupante(Linea l) {
        exigirCampos(l, 3);
        reserva(l.campos[0]).agregarOcupante(new Ocupante(l.campos[1], fecha(l.campos[2])));
    }

    /** Cada reserva cargada se liquida con el calendario y su valor queda como cargo en el folio. */
    private void liquidarReservas() {
        for (Reserva reserva : sga.getReservas()) {
            sga.getLiquidador().liquidar(reserva);
            reserva.getFolio().registrarCargo(reserva.getCostoTotal(), "Alojamiento " + reserva.getEstancia());
        }
    }

    private void cargarPago(Linea l) {
        exigirCampos(l, 3);
        String monto = l.campos[1].toUpperCase();
        Reserva reserva = reserva(l.campos[0]);
        double valor = switch (monto) {
            case "ANTICIPO" -> reserva.getValorAnticipo();
            case "TOTAL" -> reserva.getCostoTotal();
            default -> decimal(l.campos[1]);
        };
        reserva.getFolio().registrarPago(valor, l.campos[2]);
    }

    /**
     * Estados distintos de Preparado. Los Pendientes de preparación entran a la cola;
     * los Fuera de servicio quedan con su motivo en el historial.
     */
    private void cargarEstado(Linea l) {
        exigirCampos(l, 2);
        Apartamento apartamento = sga.obtenerApartamento(l.campos[0]);
        EstadoApartamento estado = EstadoApartamento.valueOf(l.campos[1].toUpperCase());
        switch (estado) {
            case FUERADESERVICIO -> sga.ponerFueraDeServicio(apartamento.getCodigo(), opcional(l, 2));
            case PENDIENTEDEPREPARACION -> {
                apartamento.setEstadoOperativo(estado);
                sga.encolarParaPreparacion(apartamento);
            }
            default -> apartamento.setEstadoOperativo(estado);
        }
    }

    private void cargarSolicitud(Linea l) {
        exigirCampos(l, 6);
        sga.recibirSolicitudExterna(new SolicitudExterna(l.campos[0], sga.obtenerApartamento(l.campos[1]),
                fecha(l.campos[2]), fecha(l.campos[3]), entero(l.campos[4]), huesped(l.campos[5])));
    }

    // ============================================================== utilidades

    private Huesped huesped(String documento) {
        Huesped huesped = sga.buscarHuesped(documento);
        if (huesped == null) {
            throw new IllegalArgumentException("no existe el huésped con documento " + documento + ".");
        }
        return huesped;
    }

    private Reserva reserva(String codigo) {
        Reserva reserva = sga.buscarReserva(codigo);
        if (reserva == null) {
            throw new IllegalArgumentException("no existe la reserva " + codigo + ".");
        }
        return reserva;
    }

    private static void exigirCampos(Linea l, int minimo) {
        if (l.campos.length < minimo) {
            throw new IllegalArgumentException("se esperaban al menos " + minimo + " campos y hay " + l.campos.length + ".");
        }
    }

    private static String opcional(Linea l, int indice) {
        return indice < l.campos.length ? l.campos[indice] : "";
    }

    private static int entero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + texto + "' no es un número entero.");
        }
    }

    private static double decimal(String texto) {
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + texto + "' no es un número.");
        }
    }

    /** @param texto fecha ISO (2026-12-15) o relativa a hoy (HOY, HOY+3, HOY-2) */
    LocalDate fecha(String texto) {
        String limpio = texto.trim().toUpperCase();
        try {
            if (limpio.startsWith("HOY")) {
                String desplazamiento = limpio.substring(3).trim();
                return desplazamiento.isEmpty() ? sga.hoy()
                        : sga.hoy().plusDays(Integer.parseInt(desplazamiento.replace("+", "")));
            }
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException("'" + texto + "' no es una fecha válida (use AAAA-MM-DD o HOY±n).");
        }
    }

    /** @param texto fecha (ISO o relativa) seguida opcionalmente de la hora: "HOY-1 09:30" */
    LocalDateTime fechaHora(String texto) {
        String limpio = texto.trim();
        int espacio = limpio.lastIndexOf(' ');
        if (espacio < 0) {
            return fecha(limpio).atStartOfDay();
        }
        try {
            return fecha(limpio.substring(0, espacio)).atTime(LocalTime.parse(limpio.substring(espacio + 1)));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("'" + texto + "' no tiene una hora válida (use HH:mm).");
        }
    }

    private static CargaInvalida error(int numero, String mensaje) {
        return new CargaInvalida("Línea " + numero + ": " + mensaje, null);
    }

    /** Error de formato o de datos en el archivo; el mensaje siempre indica la línea. */
    private static class CargaInvalida extends IllegalArgumentException {
        CargaInvalida(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
