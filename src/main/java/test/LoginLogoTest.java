package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class LoginLogoTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void logoShouldBeLoaded(FxRobot robot) {
        // Localiza el ImageView por su fx:id
        ImageView logo = robot.lookup("#logoID")
                              .queryAs(ImageView.class);
        assertNotNull(logo, "Debe existir el ImageView con fx:id 'logoID'");
        // Comprueba que haya cargado una imagen
        assertNotNull(
            logo.getImage(),
            "El ImageView 'logoID' debería tener una imagen cargada tras initialize()"
        );
    }
}
