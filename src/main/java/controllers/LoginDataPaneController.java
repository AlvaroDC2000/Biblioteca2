package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import models.Usuario;
import utils.HibernateUtil;
import utils.SessionManager;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginDataPaneController implements Initializable {

  // Campos de entrada para email y contraseña del usuario
  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;

  // Imagen del logo en la pantalla de login
  @FXML private ImageView logoID;

  // Evento al pulsar el botón "Iniciar sesión"
  @FXML
  private void handleLogin(ActionEvent event) {
      String email = emailField.getText();
      String password = passwordField.getText();
      procesarLogin(email, password, event);
  }

  // Método que realiza la autenticación contra la base de datos
  public void procesarLogin(String email, String password, ActionEvent event) {
      // Validación de campos vacíos
      if (email.isEmpty() || password.isEmpty()) {
          showAlert(Alert.AlertType.ERROR, "Rellena todos los campos.");
          return;
      }

      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
          // Consulta para buscar el usuario con las credenciales proporcionadas
          Query<Usuario> query = session.createQuery(
              "FROM Usuario WHERE email = :email AND password = :password", Usuario.class);
          query.setParameter("email", email);
          query.setParameter("password", password);
          Usuario usuario = query.uniqueResult();

          if (usuario != null) {
              // Si el usuario existe, lo guardamos en sesión y cargamos la pantalla principal
              SessionManager.setUsuarioActual(usuario);

              FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HomePane.fxml"));
              AnchorPane homePane = loader.load();

              Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
              Scene scene = new Scene(homePane);
              stage.setScene(scene);
              stage.show();
          } else {
              // Si las credenciales no son correctas, mostramos un mensaje de error
              showAlert(Alert.AlertType.ERROR, "Credenciales incorrectas.");
          }
      } catch (Exception e) {
          e.printStackTrace();
          showAlert(Alert.AlertType.ERROR, "Error al acceder a la base de datos.");
      }
  }

  // Evento al pulsar el enlace o botón "Registrarse"
  @FXML
  private void handleGoToRegister(ActionEvent event) {
      try {
          // Carga la pantalla de registro y la reemplaza en la escena actual
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

  // Método para mostrar mensajes emergentes (alertas) al usuario
  private void showAlert(Alert.AlertType type, String message) {
      Alert alert = new Alert(type);
      alert.setTitle("Mensaje");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
  }

  // Al inicializar el controlador, cargamos el logo desde una URL
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
