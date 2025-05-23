package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import static java.util.concurrent.TimeUnit.SECONDS;

@ExtendWith(ApplicationExtension.class)
public abstract class LoggedInTestBase {

    @Start
    private void start(Stage stage) throws Exception {
        Parent login = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(login));
        stage.show();
        // Damos un momentito a JavaFX para renderizarla
        WaitForAsyncUtils.sleep(1, SECONDS);
    }

    @BeforeEach
    void doLogin(FxRobot robot) {
        // Pequeña pausa para asegurarnos de que los nodos ya están en escena
        WaitForAsyncUtils.sleep(1, SECONDS);

        // Ahora sí clickeamos los campos de login
        robot.clickOn("#emailField").write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField").write("abc123");
        robot.clickOn("#loginButton");

        // Tras hacer login, esperamos un segundo a que HomePane aparezca
        WaitForAsyncUtils.sleep(1, SECONDS);
    }
}
