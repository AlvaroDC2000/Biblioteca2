package test;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class LoginTest {

    private Button loginButton;

    @Start
    private void start(Stage stage) {
        // Aquí arrancas tu aplicación real.
        // Ejemplo mínimo para probar el botón:
        loginButton = new Button("Login");
        loginButton.setId("loginButton");
        stage.setScene(new Scene(new StackPane(loginButton), 300, 200));
        stage.show();
    }

    @Test
    void shouldDisplayLoginButton(FxRobot robot) {
        // Verifica que el botón con id "loginButton" tiene texto "Login"
        FxAssert.verifyThat("#loginButton", LabeledMatchers.hasText("Login"));
    }
}
