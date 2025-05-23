package test;

import application.Main;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

import org.testfx.api.FxRobot;

@ExtendWith(ApplicationExtension.class)
public class LoginFieldsTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Arranca la app tal como en tu Main
        new Main().start(stage);
    }

    @Test
    void emailAndPasswordFieldsShouldBeEmpty(FxRobot robot) {
        // Localiza los TextField por su fx:id
        TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
        PasswordField passwordField = robot.lookup("#passwordField").queryAs(PasswordField.class);

        // Comprueba que existen
        assertNotNull(emailField,    "El campo de email debería estar presente");
        assertNotNull(passwordField, "El campo de contraseña debería estar presente");

        // Comprueba que ambos empiezan vacíos
        assertEquals("", emailField.getText(),    "El email debería inicializarse vacío");
        assertEquals("", passwordField.getText(), "La contraseña debería inicializarse vacía");
    }
}
