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

import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
public class LogoutNavigationTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Carga la vista principal (HomePane)
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/HomePane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldReturnToLoginWhenLoggingOut(FxRobot robot) {
        // 1) Pulsar el botón de cerrar sesión (su fx:id debe ser "logoutButton")
        robot.clickOn("#logoutButton");

        // 2) Verificar que el login aparece: el botón de login debe ser visible
        FxAssert.verifyThat("#loginButton", isVisible());
    }
}
