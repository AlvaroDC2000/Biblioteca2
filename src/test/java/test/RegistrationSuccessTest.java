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
public class RegistrationSuccessTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/RegisterDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldRegisterWithoutErrors(FxRobot robot) {
        // 1) Rellenar email y contraseñas iguales
        robot.clickOn("#newUsernameField").write("usuario01@ejemplo.com");
        robot.clickOn("#newPasswordField").write("abc123");
        robot.clickOn("#repeatPasswordField").write("abc123");

        // 2) Seleccionar un género válido
        robot.clickOn("#favoriteGenreComboBox")
             .clickOn("Historia");

        // 3) Pulsar Registrar
        robot.clickOn("Registrar");

        // 4) Comprobar que no hay mensaje de error
        Label errorLabel = robot.lookup("#errorLabel")
                                .queryAs(Label.class);
        assertNotNull(errorLabel, "La etiqueta de error debería existir");
        assertEquals("", errorLabel.getText(),
                     "Tras un registro válido no debería mostrarse ningún error");
    }
}

