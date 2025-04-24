package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Usuario;
import utils.HibernateUtil;
import java.net.URL;
import java.util.ResourceBundle;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class LoginDataPaneController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView logoID;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        procesarLogin(email, password, event);
    }

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

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterDataPane.fxml"));
            AnchorPane registerPane = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(registerPane);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "No se pudo cargar la pantalla de registro.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png";
        try {
            Image image = new Image(imageUrl);
            if (!image.isError()) {
                logoID.setImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
