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
public class NavigationBackToLoginTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/RegisterDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldNavigateBackToLogin(FxRobot robot) {
        // Pulsar el botón "Volver" de la vista de registro
        robot.clickOn("Volver");

        // Verificar que el login se muestra: Label principal con el texto esperado
        FxAssert.verifyThat(".label-title", hasText("Bienvenido a la biblioteca"));
    }
}
