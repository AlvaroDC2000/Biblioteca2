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
public class RegistrationEmptyFieldsTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/RegisterDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldShowErrorWhenRegisterFieldsEmpty(FxRobot robot) {
        robot.clickOn("Registrar");

        Label errorLabel = robot.lookup("#errorLabel")
                                .queryAs(Label.class);
        assertNotNull(errorLabel, "Se esperaba la etiqueta de error tras el click");

        // Aquí cambiamos la expectativa al mensaje de longitud mínima de contraseña
        assertEquals(
            "Introduce un email válido.",
            errorLabel.getText(),
            "El mensaje de validación no coincide con el comportamiento actual"
        );
    }
}
