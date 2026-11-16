package co.edu.uniquindio.apartasuitesuq.viewController;

import co.edu.uniquindio.apartasuitesuq.model.Rol;

/**
 * Pantallas de la ventana principal, con su archivo FXML (en resources) y los roles que pueden
 * verlas. El menú lateral muestra a cada rol solo sus pantallas.
 */
public enum Pantalla {

    INICIO("Inicio", "inicio.fxml", Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    MI_PANEL("Mi panel", "panel-usuario.fxml", Rol.HUESPED),
    RESERVAS("Reservas", "reservas.fxml", Rol.HUESPED, Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    CANAL_EXTERNO("Canal externo", "canal-externo.fxml", Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    CALENDARIO("Calendario de ocupación", "calendario.fxml", Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    PREPARACION("Preparación", "preparacion.fxml", Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    MAPA("Mapa del edificio", "mapa.fxml", Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    FOLIO("Folio y turnos", "folio.fxml", Rol.RECEPCIONISTA, Rol.ADMINISTRADOR),
    ADMINISTRACION("Administración", "administracion.fxml", Rol.ADMINISTRADOR),
    TEMPORADAS("Temporadas y tarifas", "temporadas.fxml", Rol.ADMINISTRADOR),
    ESTADISTICAS("Estadísticas", "estadisticas.fxml", Rol.ADMINISTRADOR),
    CARGA("Carga de datos", "carga.fxml", Rol.ADMINISTRADOR);

    private final String titulo;
    private final String fxml;
    private final Rol[] roles;

    Pantalla(String titulo, String fxml, Rol... roles) {
        this.titulo = titulo;
        this.fxml = fxml;
        this.roles = roles;
    }

    public String getTitulo() {
        return titulo;
    }

    /** @return el archivo FXML de la pantalla, en resources/co/edu/uniquindio/apartasuitesuq */
    public String getFxml() {
        return fxml;
    }

    public boolean permitidaPara(Rol rol) {
        for (Rol permitido : roles) {
            if (permitido == rol) {
                return true;
            }
        }
        return false;
    }

    /** @return la primera pantalla del menú de cada rol */
    public static Pantalla inicialDe(Rol rol) {
        for (Pantalla pantalla : values()) {
            if (pantalla.permitidaPara(rol)) {
                return pantalla;
            }
        }
        throw new IllegalStateException("El rol " + rol + " no tiene pantallas.");
    }
}
