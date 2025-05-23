package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;

import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class NavigationToRegisterTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Carga directamente la vista de login
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldNavigateToRegisterPane(FxRobot robot) {
        // Pulsar el botón de registro por su fx:id
        robot.clickOn("#registerButton");

        // Verificar que el título de la pantalla de registro está presente
        FxAssert.verifyThat(".label-title", hasText("Crear una cuenta"));
    }
}
