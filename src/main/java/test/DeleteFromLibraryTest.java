package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class DeleteFromLibraryTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent login = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(login));
        stage.show();
    }

    @Test
    void shouldLoginGoToLibraryAndDeleteFirstBook(FxRobot robot) {
        // 1) Login
        robot.clickOn("#emailField").write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField").write("abc123");
        robot.clickOn("#loginButton");
        WaitForAsyncUtils.sleep(3, SECONDS);

        // 2) Ir a "Mi Biblioteca"
        robot.clickOn("Mi Biblioteca");
        WaitForAsyncUtils.sleep(3, SECONDS);

        // 3) Encontrar el primer botón "Eliminar" visible y borrarlo
        Button deleteBtn = robot.lookup("Eliminar")
                                .queryAllAs(Button.class).stream()
                                .filter(Button::isVisible)
                                .findFirst()
                                .orElseThrow(() -> new AssertionError("No se encontró ningún botón 'Eliminar'"));

        // Ejecutar el .fire() en el hilo de JavaFX
        robot.interact(deleteBtn::fire);

        // 4) Pausa de 3 segundos para ver el resultado
        WaitForAsyncUtils.sleep(3, SECONDS);
    }
}

