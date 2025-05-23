package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class HomePaneLoadTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/HomePane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void homePaneShouldInitializeCorrectly(FxRobot robot) {
        // 1) Logo del menú lateral
        ImageView drawerLogo = robot.lookup("#drawerLogo")
                                     .queryAs(ImageView.class);
        assertNotNull(drawerLogo, "Debe existir el ImageView #drawerLogo");
        assertNotNull(drawerLogo.getImage(), 
            "El ImageView #drawerLogo debería tener una imagen cargada");

        // 2) Campo de búsqueda con el prompt correcto
        TextField searchField = robot.lookup("#searchField")
                                     .queryAs(TextField.class);
        assertNotNull(searchField, "Debe existir el TextField #searchField");
        assertEquals("Buscar libros por título, autor, ISBN...", 
            searchField.getPromptText(),
            "El prompt del campo de búsqueda no coincide");

        // 3) Contenedor de resultados vacío al inicio
        VBox resultsContainer = robot.lookup("#resultsContainer")
                                     .queryAs(VBox.class);
        assertNotNull(resultsContainer, 
            "Debe existir el contenedor #resultsContainer");
        assertEquals(0, resultsContainer.getChildren().size(),
            "El contenedor de resultados debe empezar vacío");
    }
}
