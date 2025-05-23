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

@ExtendWith(ApplicationExtension.class)
public class HomePaneSearchTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/HomePane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void shouldTypeHarryPotterAndClickSearch(FxRobot robot) {
        // 1) Escribir "Harry Potter" en el campo de búsqueda
        robot.clickOn("#searchField")
             .write("Harry Potter");
        // 2) Pulsar el botón de búsqueda junto a la barra
        robot.clickOn("#searchButton");
        // El test pasa si no se lanza ninguna excepción
    }
}
