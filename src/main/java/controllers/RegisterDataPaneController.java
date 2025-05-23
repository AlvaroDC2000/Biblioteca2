package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controlador de la pantalla de registro de nuevos usuarios.
 * <p>
 * Gestiona la validación de datos del formulario, la creación de la cuenta
 * en la base de datos mediante Hibernate, la carga dinámica de géneros desde
 * Google Books API y la navegación de regreso a la pantalla de login.
 * </p>
 */
public class RegisterDataPaneController implements Initializable {

    /** Campo de texto para introducir el email del nuevo usuario. */
    @FXML private TextField newUsernameField;

    /** Campo de texto para introducir la nueva contraseña. */
    @FXML private PasswordField newPasswordField;

    /** Campo de texto para repetir la nueva contraseña. */
    @FXML private PasswordField repeatPasswordField;

    /** ComboBox que muestra la lista de géneros para seleccionar el favorito. */
    @FXML private ComboBox<String> favoriteGenreComboBox;

    /** Etiqueta para mostrar mensajes de error o validación. */
    @FXML private Label errorLabel;

    /**
     * Inicializa el controlador tras cargar el FXML.
     * <p>
     * Realiza una petición a Google Books API para obtener una lista de géneros
     * de libros populares, traduce los géneros del inglés al español mediante
     * un diccionario y llena el ComboBox correspondiente.
     * </p>
     *
     * @param location  no usado
     * @param resources no usado
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Diccionario de traducción de géneros (inglés -> español)
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

        // Conjunto para almacenar géneros sin duplicados
        Set<String> generos = new HashSet<>();

        try {
            // Petición HTTP a Google Books para obtener libros populares
            String urlStr = "https://www.googleapis.com/books/v1/volumes?"
                          + "q=bestseller&maxResults=30&langRestrict=es";
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Lectura de la respuesta JSON
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            // Parseo del JSON y extracción de categorías
            JSONObject json = new JSONObject(result.toString());
            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject volumeInfo = items
                    .getJSONObject(i)
                    .getJSONObject("volumeInfo");
                if (volumeInfo.has("categories")) {
                    JSONArray categories = volumeInfo.getJSONArray("categories");
                    for (int j = 0; j < categories.length(); j++) {
                        String original = categories.getString(j);
                        // Traducir si existe traducción, sino usar original
                        String traducido = traducciones.getOrDefault(original, original);
                        generos.add(traducido);
                    }
                }
            }

            // Rellenar el ComboBox con los géneros obtenidos
            favoriteGenreComboBox.getItems().addAll(generos);

        } catch (Exception e) {
            // Mostrar error si falla la carga de géneros
            e.printStackTrace();
            errorLabel.setText("Error al cargar géneros desde Google Books.");
        }
    }

    /**
     * Manejador del botón "Registrarse".
     * <p>
     * Valida el formato del email, la coincidencia y longitud de las contraseñas,
     * comprueba que no exista ya un usuario con el mismo email,
     * persiste el nuevo usuario en la base de datos y limpia mensajes de error.
     * </p>
     */
    @FXML
    private void handleRegister() {
        String email = newUsernameField.getText();
        String password = newPasswordField.getText();
        String repeatPassword = repeatPasswordField.getText();
        String genre = favoriteGenreComboBox.getValue();

        // Validar formato básico de email
        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Introduce un email válido.");
            return;
        }

        // Verificar que las contraseñas coincidan
        if (!password.equals(repeatPassword)) {
            errorLabel.setText("Las contraseñas no coinciden.");
            return;
        }

        // Verificar longitud mínima de la contraseña
        if (password.length() < 6) {
            errorLabel.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Comprobar que no queden campos vacíos y que se haya elegido un género
        if (email.isEmpty() || password.isEmpty() || genre == null) {
            errorLabel.setText("Rellena todos los campos.");
            return;
        }

        // Intentar registrar el nuevo usuario en la base de datos
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Comprobar si ya existe un usuario con ese email
            Query<Usuario> query = session.createQuery(
                "FROM Usuario WHERE email = :email", Usuario.class
            );
            query.setParameter("email", email);
            Usuario existingUser = query.uniqueResult();

            if (existingUser != null) {
                // Usuario ya existente
                errorLabel.setText("Ya existe un usuario con ese email.");
                return;
            }

            // Crear y persistir el nuevo usuario
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setGeneroFavorito(genre);

            Transaction tx = session.beginTransaction();
            session.persist(usuario);
            tx.commit();

            // Limpiar mensaje de error tras el registro exitoso
            errorLabel.setText("");

        } catch (Exception e) {
            // Mostrar mensaje si hay error durante el registro
            errorLabel.setText("Error al registrar el usuario.");
            e.printStackTrace();
        }
    }

    /**
     * Manejador del enlace o botón "Volver al login".
     * <p>
     * Carga la escena de login y la establece en el Stage actual.
     * </p>
     *
     * @param event evento de acción para obtener la ventana actual
     */
    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/LoginDataPane.fxml")
            );
            AnchorPane loginPane = loader.load();

            // Obtener el Stage desde el nodo que disparó el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginPane));
            stage.show();

        } catch (Exception e) {
            // Mostrar alerta si falla la navegación al login
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo volver a la pantalla de login.");
            alert.showAndWait();
        }
    }
}
