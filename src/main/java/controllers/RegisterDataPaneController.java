package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import models.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.HibernateUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegisterDataPaneController {

  // Campos del formulario de registro
  @FXML private TextField newUsernameField;
  @FXML private PasswordField newPasswordField;
  @FXML private PasswordField repeatPasswordField;
  @FXML private ComboBox<String> favoriteGenreComboBox;
  @FXML private Label errorLabel;

  // Método llamado al pulsar el botón "Registrarse"
  @FXML
  private void handleRegister() {
      String email = newUsernameField.getText();
      String password = newPasswordField.getText();
      String repeatPassword = repeatPasswordField.getText();
      String genre = favoriteGenreComboBox.getValue();

      // Validación del email
      if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
          errorLabel.setText(" Introduce un email válido.");
          return;
      }

      // Verificar que ambas contraseñas coincidan
      if (!password.equals(repeatPassword)) {
          errorLabel.setText(" Las contraseñas no coinciden.");
          return;
      }

      // Verificar longitud mínima de la contraseña
      if (password.length() < 6) {
          errorLabel.setText(" La contraseña debe tener al menos 6 caracteres.");
          return;
      }

      // Comprobación final de campos no vacíos
      if (!email.isEmpty() && !password.isEmpty() && genre != null) {
          try (Session session = HibernateUtil.getSessionFactory().openSession()) {
              // Comprobamos si el usuario ya existe por email
              Query<Usuario> query = session.createQuery("FROM Usuario WHERE email = :email", Usuario.class);
              query.setParameter("email", email);
              Usuario existingUser = query.uniqueResult();

              if (existingUser != null) {
                  errorLabel.setText(" Ya existe un usuario con ese email.");
                  return;
              }

              // Creamos el nuevo usuario
              Usuario usuario = new Usuario();
              usuario.setEmail(email);
              usuario.setPassword(password);
              usuario.setGeneroFavorito(genre);

              // Guardamos el usuario en la base de datos
              Transaction tx = session.beginTransaction();
              session.persist(usuario); 
              tx.commit();

              errorLabel.setText(""); // Limpiar error si todo fue correcto

          } catch (Exception e) {
              errorLabel.setText(" Error al registrar el usuario.");
              e.printStackTrace();
          }
      } else {
          errorLabel.setText(" Rellena todos los campos.");
      }
  }

  // Método que permite volver a la pantalla de login
  @FXML
  private void goToLogin(ActionEvent event) {
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
          AnchorPane loginPane = loader.load();

          Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
          Scene scene = new Scene(loginPane);
          stage.setScene(scene);
          stage.show();
      } catch (Exception e) {
          e.printStackTrace();
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText(null);
          alert.setContentText("No se pudo volver a la pantalla de login.");
          alert.showAndWait();
      }
  }

  // Método que se ejecuta al cargar el controlador
  // Se conecta con Google Books para cargar géneros automáticamente
  @FXML
  public void initialize() {
      Set<String> generos = new HashSet<>();

      // Diccionario de traducción de géneros del inglés al español
      Map<String, String> traducciones = Map.ofEntries(
          Map.entry("Fiction", "Ficción"),
          Map.entry("Literary Criticism", "Crítica literaria"),
          Map.entry("History", "Historia"),
          Map.entry("Computers", "Informática"),
          Map.entry("Reference", "Referencia"),
          Map.entry("Biography & Autobiography", "Biografía y autobiografía"),
          Map.entry("Social Science", "Ciencias sociales"),
          Map.entry("Poetry", "Poesía"),
          Map.entry("Business & Economics", "Negocios y economía"),
          Map.entry("Language Arts & Disciplines", "Lengua y literatura")
      );

      try {
          // Petición a Google Books para obtener libros populares
          String urlStr = "https://www.googleapis.com/books/v1/volumes?q=bestseller&maxResults=30&langRestrict=es";
          URL url = URI.create(urlStr).toURL();
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");

          BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          StringBuilder result = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
              result.append(line);
          }
          reader.close();

          // Parsear el JSON recibido y extraer categorías
          JSONObject json = new JSONObject(result.toString());
          JSONArray items = json.getJSONArray("items");

          for (int i = 0; i < items.length(); i++) {
              JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
              if (volumeInfo.has("categories")) {
                  JSONArray categories = volumeInfo.getJSONArray("categories");
                  for (int j = 0; j < categories.length(); j++) {
                      String original = categories.getString(j);
                      String traducido = traducciones.getOrDefault(original, original); 
                      generos.add(traducido);
                  }
              }
          }

          // Rellenamos el ComboBox con los géneros extraídos
          favoriteGenreComboBox.getItems().addAll(generos);
      } catch (Exception e) {
          e.printStackTrace();
          errorLabel.setText("Error al cargar géneros desde Google Books.");
      }
  }
}
