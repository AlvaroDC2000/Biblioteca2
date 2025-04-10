package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;

public class LoginDataPaneController {

    private LoginController mainController;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

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
}
