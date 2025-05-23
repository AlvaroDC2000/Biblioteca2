package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class BookDetailNavigationTest {

    @Start
    private void start(Stage stage) throws Exception {
        // Arrancamos en la pantalla de login
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldLoginSearchAndNavigateToDetail(FxRobot robot) {
        // --- Paso 1: Login ---
        robot.clickOn("#emailField")
             .write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField")
             .write("abc123");
        robot.clickOn("#loginButton");

        // Esperamos a que cargue HomePane: buscaremos #searchField
        WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);
        assertNotNull(robot.lookup("#searchField").queryAs(javafx.scene.control.TextField.class),
                      "Tras login debería mostrarse HomePane con #searchField");

        // --- Paso 2: Búsqueda ---
        robot.clickOn("#searchField")
             .write("Harry Potter");
        robot.clickOn("#searchButton");

        // Damos tiempo a la llamada asíncrona
        WaitForAsyncUtils.sleep(10, TimeUnit.SECONDS);

        // Comprobamos resultados
        VBox results = robot.lookup("#resultsContainer").queryAs(VBox.class);
        assertNotNull(results, "Debe existir #resultsContainer");
        assertTrue(results.getChildren().size() > 0,
                   "Tras buscar debería haber al menos un resultado");

        // --- Paso 3: Navegar a detalle ---
        robot.clickOn(results.getChildren().get(0));

        // Verificamos que llega a DetailsPane: Label #titleLabel
        Label detailTitle = robot.lookup("#titleLabel").queryAs(Label.class);
        assertNotNull(detailTitle, "Debe existir #titleLabel en DetailsPane");
        assertTrue(detailTitle.getText().toLowerCase().contains("harry potter"),
                   "El título de detalle debe contener 'harry potter'");
    }
}
