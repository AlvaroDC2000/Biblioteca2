package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static javafx.scene.input.KeyCode.*;

@ExtendWith(ApplicationExtension.class)
public class ProfileActionsTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent login = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(login));
        stage.show();
    }

    @Test
    void shouldEditProfileChangePasswordAndDeleteAccount(FxRobot robot) {
        // 1) login
        robot.clickOn("#emailField").write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField").write("abc123");
        robot.clickOn("#loginButton");
        WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);

        // 2) abrir perfil
        robot.clickOn("Perfil");
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);

        // 3) cambiar género
        robot.interact(() -> robot.clickOn(".combo-box"));
        for (int i = 0; i < 5; i++) {
            robot.type(DOWN);
            String sel = robot.lookup(".combo-box .list-cell:selected")
                              .queryAs(ListCell.class)
                              .getText();
            if ("Otro".equals(sel)) break;
        }
        robot.type(ENTER);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        // guardar género
        robot.lookup("Guardar género")
             .queryAllAs(Button.class)
             .stream()
             .filter(Button::isVisible)
             .findFirst()
             .ifPresent(btn -> robot.interact(btn::fire));
        acceptAlert(robot);

        // 4) cambiar contraseña
        TextField tfNew = robot.lookup(node ->
            node instanceof TextField &&
            "mín. 6 caracteres".equals(((TextField)node).getPromptText())
        ).queryAs(TextField.class);
        TextField tfConfirm = robot.lookup(node ->
            node instanceof TextField &&
            "repite la nueva contraseña".equals(((TextField)node).getPromptText())
        ).queryAs(TextField.class);
        assertNotNull(tfNew);
        assertNotNull(tfConfirm);

        robot.clickOn(tfNew).write("abc1234");
        robot.clickOn(tfConfirm).write("abc1234");
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        // pulsar "Cambiar contraseña"
        robot.lookup("Cambiar contraseña")
             .queryAllAs(Button.class)
             .stream()
             .filter(Button::isVisible)
             .findFirst()
             .ifPresent(btn -> robot.interact(btn::fire));
        acceptAlert(robot);

        // 5) eliminar cuenta
        robot.lookup("Eliminar cuenta")
             .queryAllAs(Button.class)
             .stream()
             .filter(Button::isVisible)
             .findFirst()
             .ifPresent(btn -> robot.interact(btn::fire));
        acceptAlert(robot);

        // 6) verificar vuelta al login
        assertNotNull(robot.lookup("#emailField").tryQuery().orElse(null));
    }

    /** Localiza un Alert y pulsa su botón "Aceptar". */
    private void acceptAlert(FxRobot robot) {
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        robot.lookup(".dialog-pane .button")
             .queryAllAs(Button.class)
             .stream()
             .filter(b -> b.isVisible() && b.getText().equals("Aceptar"))
             .findFirst()
             .ifPresent(b -> robot.interact(b::fire));
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
    }
}
