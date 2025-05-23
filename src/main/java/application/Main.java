package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación Biblioteca.
 * <p>
 * Extiende de {@link javafx.application.Application} para gestionar
 * el ciclo de vida de la aplicación JavaFX. Se encarga de cargar
 * la interfaz de usuario definida en el archivo FXML correspondiente
 * al login y mostrar la ventana principal.
 * </p>
 */
public class Main extends Application {

    /**
     * Método de inicio de la aplicación JavaFX.
     * <p>
     * Se invoca automáticamente tras la llamada a {@link #launch(String...)}.
     * Aquí se carga el layout definido en LoginDataPane.fxml y se muestra
     * en la ventana principal (Stage).
     * </p>
     *
     * @param primaryStage escena principal proporcionada por JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Carga el archivo FXML que define la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
            Parent root = loader.load();

            // Crea la escena con el nodo raíz cargado desde FXML
            Scene scene = new Scene(root);

            // Configuración de la ventana principal
            primaryStage.setTitle("Biblioteca");      // Título de la ventana
            primaryStage.setScene(scene);             // Asigna la escena al Stage
            primaryStage.setResizable(false);         // Deshabilita el redimensionado
            primaryStage.show();                      // Muestra la ventana

        } catch (IOException e) {
            // Imprime la traza de la excepción en la consola para depuración
            e.printStackTrace();
            System.err.println("Error al cargar el archivo FXML");
        }
    }

    /**
     * Punto de entrada de la aplicación.
     * <p>
     * Llama internamente a {@link Application#launch(String...)} para
     * inicializar y arrancar la plataforma JavaFX.
     * </p>
     *
     * @param args argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
