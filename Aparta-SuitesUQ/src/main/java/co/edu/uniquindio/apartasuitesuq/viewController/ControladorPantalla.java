package co.edu.uniquindio.apartasuitesuq.viewController;

/**
 * Controlador de una pantalla que se muestra dentro de la ventana principal. Al cargar su FXML,
 * la ventana principal le entrega acceso a la fachada, a la sesión y a la navegación.
 */
public interface ControladorPantalla {

    /** Se llama una vez, justo después de cargar el FXML (después de {@code initialize}). */
    void inicializar(PrincipalViewController principal);
}
