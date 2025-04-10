package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginDataPaneController implements Initializable {

    private LoginController mainController;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView logoID;

    public void setMainController(LoginController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        mainController.procesarLogin(email, password, event);
    }

    @FXML
    private void handleGoToRegister() {
        mainController.loadRegisterPane();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[INIT] Inicializando LoginDataPaneController...");
        System.out.println("[DEBUG] ImageView (logoID) es null: " + (logoID == null));

        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png";
        System.out.println("[DEBUG] URL de la imagen: " + imageUrl);

        try {
            Image image = new Image(imageUrl);
            if (image.isError()) {
                System.out.println("[ERROR] Fallo al cargar la imagen: " + image.getException());
            } else {
                logoID.setImage(image);
                System.out.println("[OK] Imagen cargada correctamente.");
            }
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Excepción al cargar imagen:");
            e.printStackTrace();
        }
    }
}
