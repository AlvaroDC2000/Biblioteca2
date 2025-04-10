package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
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
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class RegisterDataPaneController {

    private LoginController mainController;

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private ComboBox<String> favoriteGenreComboBox;

    @FXML
    private Label errorLabel;

    public void setMainController(LoginController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleRegister() {
        String email = newUsernameField.getText();
        String password = newPasswordField.getText();
        String repeatPassword = repeatPasswordField.getText();
        String genre = favoriteGenreComboBox.getValue();

        if (!password.equals(repeatPassword)) {
            errorLabel.setText("⚠ Las contraseñas no coinciden.");
            return;
        }

        if (!email.isEmpty() && !password.isEmpty() && genre != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Query<Usuario> query = session.createQuery("FROM Usuario WHERE email = :email", Usuario.class);
                query.setParameter("email", email);
                Usuario existingUser = query.uniqueResult();

                if (existingUser != null) {
                    errorLabel.setText("⚠ Ya existe un usuario con ese email.");
                    return;
                }

                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setPassword(password);
                usuario.setGeneroFavorito(genre);

                Transaction tx = session.beginTransaction();
                session.save(usuario);
                tx.commit();

                errorLabel.setText(""); // Limpiar error si todo está bien
                mainController.showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Usuario registrado correctamente.");
                mainController.loadLoginDataPane();

            } catch (Exception e) {
                errorLabel.setText("⚠ Error al registrar el usuario.");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("⚠ Rellena todos los campos.");
        }
    }

    @FXML
    public void initialize() {
        new Thread(() -> {
            Set<String> generos = new HashSet<>();
            try {
                String urlStr = "https://www.googleapis.com/books/v1/volumes?q=bestseller&maxResults=40";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONArray items = json.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
                    if (volumeInfo.has("categories")) {
                        JSONArray categories = volumeInfo.getJSONArray("categories");
                        for (int j = 0; j < categories.length(); j++) {
                            generos.add(categories.getString(j));
                        }
                    }
                }

                Platform.runLater(() -> favoriteGenreComboBox.getItems().addAll(generos));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    errorLabel.setText("Error al cargar géneros desde Google Books.");
                });
            }
        }).start();
    }

    @FXML
    private void goToLogin() {
        mainController.loadLoginDataPane();
    }
}