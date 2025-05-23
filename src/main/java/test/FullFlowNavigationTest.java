package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class FullFlowNavigationTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Arrancamos en la pantalla de login
        Parent login = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(login));
        stage.show();
    }

    @Test
    void shouldLoginNavigateSectionsAndShowProfile(FxRobot robot) {
        // --- Paso 1: Login ---
        robot.clickOn("#emailField").write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField").write("abc123");
        robot.clickOn("#loginButton");

        // Esperar a que HomePane esté listo
        WaitForAsyncUtils.sleep(2, SECONDS);

        // --- Paso 2: Navegar a Recomendaciones ---
        robot.clickOn("Recomendaciones");
        WaitForAsyncUtils.sleep(1, SECONDS);
        verifyThat(".label-title", hasText("Recomendaciones para ti"));

        // --- Paso 3: Navegar a Mi Biblioteca ---
        robot.clickOn("Mi Biblioteca");
        WaitForAsyncUtils.sleep(1, SECONDS);
        verifyThat(".label-title", hasText("Mis libros guardados"));

        // --- Paso 4: Volver al HomePane ---
        robot.clickOn("Buscar");
        WaitForAsyncUtils.sleep(1, SECONDS);

        // --- Paso 5: Abrir Perfil de Usuario ---
        robot.clickOn("#profileButton");
        WaitForAsyncUtils.sleep(1, SECONDS);

        // En lugar de buscar por fx:id, simplemente verificamos que el texto
        // con el email aparece en pantalla:
        verifyThat("usuario01@ejemplo.com", isVisible());
    }
}

