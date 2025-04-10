package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import models.Usuario;
import org.hibernate.Session;
import org.hibernate.query.Query;
import utils.HibernateUtil;

public class LoginController {

    public void procesarLogin(String email, String password, ActionEvent event) {
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Rellena todos los campos.");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE email = :email AND password = :password", Usuario.class);
            query.setParameter("email", email);
            query.setParameter("password", password);
            Usuario usuario = query.uniqueResult();

            if (usuario != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HomePane.fxml"));
                AnchorPane homePane = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(homePane);
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Credenciales incorrectas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al acceder a la base de datos.");
        }
    }

    public void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private AnchorPane contentPane;

    public void loadRegisterPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterDataPane.fxml"));
            AnchorPane pane = loader.load();
            RegisterDataPaneController controller = loader.getController();
            controller.setMainController(this);
            contentPane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLoginDataPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
            AnchorPane pane = loader.load();
            LoginDataPaneController controller = loader.getController();
            controller.setMainController(this);
            contentPane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
            AnchorPane pane = loader.load();
            LoginDataPaneController controller = loader.getController();
            controller.setMainController(this);
            contentPane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}