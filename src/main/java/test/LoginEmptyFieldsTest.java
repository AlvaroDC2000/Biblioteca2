package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class LoginEmptyFieldsTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Carga directamente el FXML de login:
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldShowErrorAlertWhenFieldsEmpty(FxRobot robot) {
        // Ahora sí existe:
        robot.clickOn("#loginButton");

        // Espera al DialogPane del Alert
        DialogPane pane = robot.lookup(".dialog-pane")
                                .queryAs(DialogPane.class);
        assertNotNull(pane, "Se esperaba un DialogPane tras el click");

        // Comprueba el texto
        assertEquals("Rellena todos los campos.", pane.getContentText());

        // Cierra la alerta
        robot.clickOn(pane.lookupButton(ButtonType.OK));
    }
}
