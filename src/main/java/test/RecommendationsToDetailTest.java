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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class RecommendationsToDetailTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent login = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(login));
        stage.show();
    }

    @Test
    void shouldLoginThenSelectFirstRecommendationAndGoToDetail(FxRobot robot) {
        // --- Paso 1: Login ---
        robot.clickOn("#emailField").write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField").write("abc123");
        robot.clickOn("#loginButton");
        WaitForAsyncUtils.sleep(2, SECONDS);

        // --- Paso 2: Ir a Recomendaciones ---
        robot.clickOn("Recomendaciones");
        WaitForAsyncUtils.sleep(2, SECONDS);

        // Verificar que hay recomendaciones
        VBox recs = robot.lookup("#resultsContainer").queryAs(VBox.class);
        assertNotNull(recs, "Debe existir #resultsContainer en Recomendaciones");
        assertFalse(recs.getChildren().isEmpty(), "Debe haber al menos una recomendación");

        // --- Paso 3: Seleccionar la primera recomendación ---
        robot.clickOn(recs.getChildren().get(0));
        WaitForAsyncUtils.sleep(1, SECONDS);

        // --- Paso 4: Verificar que cargó DetailsPane buscando el título por styleClass ---
        // En DetailsPane solo hay un Label con styleClass="label-title"
        Label detailTitle = robot.lookup(".label-title")
                                 .queryAs(Label.class);
        assertNotNull(detailTitle, "Debe existir un Label con styleClass 'label-title' en DetailsPane");
        assertFalse(detailTitle.getText().isEmpty(),
                    "El texto de detalle no debería estar vacío");
    }
}

