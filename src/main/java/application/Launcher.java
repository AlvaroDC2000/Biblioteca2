package application;

/**
 * Punto de entrada de la aplicación.
 * <p>
 * Esta clase actúa como lanzador (launcher) y se encarga de delegar
 * la ejecución al método {@link Main#main(String[])}. Se emplea para
 * sortear ciertas restricciones de arranque en entornos JavaFX.
 * </p>
 */
public class Launcher {

    /**
     * Método principal de la clase Launcher.
     * <p>
     * Recibe los argumentos proporcionados en la línea de comandos y los
     * reenvía directamente al método {@link Main#main(String[])} para
     * iniciar la aplicación.
     * </p>
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        // Delegamos la ejecución a Main.main(args) para arrancar la aplicación
        Main.main(args);
    }
}
