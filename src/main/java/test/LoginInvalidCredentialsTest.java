package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class LoginInvalidCredentialsTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Carga la vista de login
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldShowErrorAlertForInvalidCredentials(FxRobot robot) {
        // Escribe un email y contraseña que no existen en la "base de datos"
        robot.clickOn("#emailField")
             .write("noexiste@dominio.com");
        robot.clickOn("#passwordField")
             .write("password123");
        robot.clickOn("#loginButton");

        // Captura el Alert y comprueba su contenido
        DialogPane dialog = robot.lookup(".dialog-pane")
                                .queryAs(DialogPane.class);
        assertNotNull(dialog, "Se esperaba un DialogPane tras intentar login inválido");
        assertEquals(
            "Credenciales incorrectas.",
            dialog.getContentText(),
            "El mensaje de error por credenciales inválidas no coincide"
        );

        // Cierra el Alert
        robot.clickOn(dialog.lookupButton(javafx.scene.control.ButtonType.OK));
    }
}
