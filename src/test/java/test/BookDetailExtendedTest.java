package test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class BookDetailExtendedTest {

    @Start
    private void start(Stage stage) throws Exception {
        Parent login = FXMLLoader.load(
            getClass().getResource("/views/LoginDataPane.fxml")
        );
        stage.setScene(new Scene(login));
        stage.show();
    }

    @Test
    void shouldFullFlowCompleteAllDetailsSaveAndPauseInMyLibrary(FxRobot robot) {
        // 1) Login
        robot.clickOn("#emailField").write("usuario01@ejemplo.com");
        robot.clickOn("#passwordField").write("abc123");
        robot.clickOn("#loginButton");
        WaitForAsyncUtils.sleep(3, SECONDS);

        // 2) Search and open detail
        robot.clickOn("#searchField").write("Harry Potter");
        robot.clickOn("#searchButton");
        WaitForAsyncUtils.sleep(3, SECONDS);
        VBox results = robot.lookup("#resultsContainer").queryAs(VBox.class);
        assertFalse(results.getChildren().isEmpty(), "Debe haber resultados");
        robot.clickOn(results.getChildren().get(0));
        WaitForAsyncUtils.sleep(3, SECONDS);

        // 3) Fill in all detail‐pane fields
        TextArea comentarioArea   = robot.lookup("#comentarioArea").queryAs(TextArea.class);
        ComboBox<String> estado    = robot.lookup("#estadoCombo").queryAs(ComboBox.class);
        DatePicker lectura         = robot.lookup("#fechaLecturaPicker").queryAs(DatePicker.class);
        TextField prestadoA        = robot.lookup("#prestadoAField").queryAs(TextField.class);
        DatePicker prestamo        = robot.lookup("#fechaPrestamoPicker").queryAs(DatePicker.class);
        CheckBox devuelto          = robot.lookup("#devueltoCheck").queryAs(CheckBox.class);
        Spinner<Integer> nota      = robot.lookup("#notaOpinionSpinner").queryAs(Spinner.class);
        TextArea contenidoOpinion = robot.lookup("#contenidoOpinionArea").queryAs(TextArea.class);

        assertNotNull(comentarioArea);
        assertNotNull(estado);
        assertNotNull(lectura);
        assertNotNull(prestadoA);
        assertNotNull(prestamo);
        assertNotNull(devuelto);
        assertNotNull(nota);
        assertNotNull(contenidoOpinion);

        robot.interact(() -> {
            comentarioArea.setText("Mi comentario personal");
            estado.setValue("Leído");
            lectura.setValue(LocalDate.of(2025,5,23));
            prestadoA.setText("amigo@correo.com");
            prestamo.setValue(LocalDate.of(2025,5,20));
            devuelto.setSelected(true);
            nota.getValueFactory().setValue(5);
            contenidoOpinion.setText("Opinión detallada");
        });

        // 4) Publish opinion and save to library
        Button btnPublicar = robot.lookup("Publicar opinión").queryAs(Button.class);
        Button btnGuardar  = robot.lookup("Guardar en Mi Biblioteca").queryAs(Button.class);
        assertNotNull(btnPublicar);
        assertNotNull(btnGuardar);

        robot.interact(btnPublicar::fire);
        WaitForAsyncUtils.sleep(1, SECONDS);
        robot.interact(btnGuardar::fire);
        WaitForAsyncUtils.sleep(2, SECONDS);

        // 5) Ir a Mi Biblioteca y pausar 5 segundos
        robot.clickOn("Mi Biblioteca");
        WaitForAsyncUtils.sleep(5, SECONDS);

        // Fin del test: hemos permanecido 5 segundos en Mi Biblioteca
    }
}
