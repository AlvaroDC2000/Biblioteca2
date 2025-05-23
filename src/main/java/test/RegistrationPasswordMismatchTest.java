package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class RegistrationPasswordMismatchTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/RegisterDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldShowErrorWhenPasswordsDoNotMatch(FxRobot robot) {
        // Rellenamos el "username" (nuevo campo)
        robot.clickOn("#newUsernameField")
             .write("usuario@ejemplo.com");
        // Rellenamos contraseñas distintas
        robot.clickOn("#newPasswordField")
             .write("abc123");
        robot.clickOn("#repeatPasswordField")
             .write("xyz789");
        // Pulsamos el botón por texto
        robot.clickOn("Registrar");

        // Comprobamos el label de error
        Label errorLabel = robot.lookup("#errorLabel")
                                .queryAs(Label.class);
        assertNotNull(errorLabel, "La etiqueta de error debería estar presente");
        assertEquals(
            "Las contraseñas no coinciden.",
            errorLabel.getText(),
            "El mensaje de validación para contraseñas no coincidentes es incorrecto"
        );
    }
}
