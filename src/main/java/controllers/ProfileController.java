package controllers;

import dao.UsuarioDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Usuario;
import utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controlador para la pantalla de "Mi Perfil".
 * <p>
 * Muestra y permite editar los datos del usuario: email y género favorito,
 * cambiar contraseña, eliminar cuenta y navegar por la aplicación.
 * Además, carga dinámicamente la lista de géneros desde Google Books API.
 * </p>
 */
public class ProfileController implements Initializable {

    /** Logo que aparece en el menú lateral. */
    @FXML private ImageView drawerLogo;

    /** Campo de texto que muestra el email del usuario. */
    @FXML private TextField emailField;

    /** ComboBox para seleccionar el género literario favorito. */
    @FXML private ComboBox<String> generoComboBox;

    /** Campo de texto para ingresar la nueva contraseña. */
    @FXML private PasswordField nuevaPassField;

    /** Campo de texto para repetir la nueva contraseña. */
    @FXML private PasswordField repitePassField;

    /** Usuario actualmente autenticado. */
    private Usuario usuario;

    /** URL para obtener géneros populares desde Google Books. */
    private static final String GOOGLE_BOOKS_API =
        "https://www.googleapis.com/books/v1/volumes?q=bestseller&maxResults=30&langRestrict=es";

    /**
     * Inicializa el controlador al cargar el FXML.
     * <ol>
     *   <li>Carga el logo.</li>
     *   <li>Carga los datos del usuario en los campos.</li>
     *   <li>Inicia la carga de géneros desde la API.</li>
     * </ol>
     *
     * @param location  ubicación del recurso FXML (no utilizado)
     * @param resources bundle de recursos (no utilizado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        cargarDatosUsuario();
        cargarGenerosDesdeAPI();
    }

    /**
     * Descarga y establece la imagen del logo en el ImageView.
     * <p>
     * Añade un User-Agent para evitar bloqueos HTTP.
     * </p>
     */
    private void cargarLogo() {
        try {
            URL url = new URI(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/"
              + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png"
            ).toURL();
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream is = conn.getInputStream()) {
                drawerLogo.setImage(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el usuario actual de sesión y rellena el campo de email.
     * <p>
     * También programa la selección del género favorito una vez
     * que el ComboBox esté poblado.
     * </p>
     */
    private void cargarDatosUsuario() {
        usuario = SessionManager.getUsuarioActual();
        if (usuario != null) {
            emailField.setText(usuario.getEmail());
            // Espera a que el ComboBox se haya llenado para seleccionar valor
            Platform.runLater(() ->
                generoComboBox.setValue(usuario.getGeneroFavorito())
            );
        }
    }

    /**
     * Lanza un hilo para solicitar categorías de libros a Google Books
     * y rellenar el ComboBox de géneros.
     */
    private void cargarGenerosDesdeAPI() {
        new Thread(() -> {
            Set<String> generos = new TreeSet<>();
            try {
                // Abrir conexión HTTP a la API de Google Books
                URI uri = URI.create(GOOGLE_BOOKS_API);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("GET");

                // Leer respuesta JSON completa
                StringBuilder sb = new StringBuilder();
                try (BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                }

                // Parsear JSON para extraer las categorías
                JSONObject json = new JSONObject(sb.toString());
                JSONArray items = json.optJSONArray("items");
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject vi = items.getJSONObject(i).getJSONObject("volumeInfo");
                        if (vi.has("categories")) {
                            JSONArray cats = vi.getJSONArray("categories");
                            for (int j = 0; j < cats.length(); j++) {
                                generos.add(cats.getString(j));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Rellenar ComboBox en el hilo de UI
            Platform.runLater(() -> {
                if (generos.isEmpty()) {
                    generoComboBox.getItems().add("General");
                } else {
                    generoComboBox.getItems().addAll(generos);
                }
                // Restaurar selección del usuario si existe
                if (usuario != null) {
                    generoComboBox.setValue(usuario.getGeneroFavorito());
                }
            });
        }).start();
    }

    /**
     * Guarda el género favorito seleccionado por el usuario.
     * <p>
     * Valida que se haya elegido un elemento y actualiza la entidad.
     * </p>
     */
    @FXML
    private void handleSaveGenero() {
        String nuevo = generoComboBox.getValue();
        if (nuevo == null || nuevo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selecciona un género.");
            return;
        }
        // Actualiza entidad y persiste cambios
        usuario.setGeneroFavorito(nuevo);
        UsuarioDAO.updateUsuario(usuario);
        showAlert(Alert.AlertType.INFORMATION, "Género favorito actualizado.");
    }

    /**
     * Permite cambiar la contraseña del usuario.
     * <ol>
     *   <li>Verifica longitud mínima de 6 caracteres.</li>
     *   <li>Comprueba que ambas contraseñas coincidan.</li>
     *   <li>Actualiza y persiste la nueva contraseña.</li>
     * </ol>
     */
    @FXML
    private void handleChangePassword() {
        String p1 = nuevaPassField.getText();
        String p2 = repitePassField.getText();
        if (p1.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "La contraseña debe tener 6+ caracteres.");
            return;
        }
        if (!p1.equals(p2)) {
            showAlert(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }
        // Persistir nuevo password
        usuario.setPassword(p1);
        UsuarioDAO.updateUsuario(usuario);
        showAlert(Alert.AlertType.INFORMATION, "Contraseña cambiada.");
        nuevaPassField.clear();
        repitePassField.clear();
    }

    /**
     * Elimina la cuenta del usuario tras confirmación del diálogo.
     * <p>
     * Borra la entidad, limpia la sesión y redirige al login.
     * </p>
     */
    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Esta acción no se puede deshacer.",
            ButtonType.CANCEL, ButtonType.OK
        );
        confirm.setHeaderText("¿Eliminar cuenta?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.orElse(ButtonType.CANCEL) == ButtonType.OK) {
            UsuarioDAO.deleteUsuario(usuario);
            SessionManager.clear();
            volverALogin();
        }
    }

    /**
     * Muestra una alerta emergente al usuario.
     *
     * @param type tipo de alerta (INFO, WARNING, ERROR)
     * @param msg  mensaje a mostrar
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Vuelve a la pantalla de login tras eliminar cuenta.
     */
    private void volverALogin() {
        try {
            AnchorPane pane = FXMLLoader.load(
                getClass().getResource("/views/LoginDataPane.fxml")
            );
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.setScene(new Scene(pane));
            stage.setTitle("Biblioteca");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Navega a la vista de búsqueda de libros. */
    @FXML private void handleShowBuscar() {
        cambiarPantalla("/views/HomePanePane.fxml");
    }

    /** Navega a la vista de recomendaciones. */
    @FXML private void handleShowRecomendaciones() {
        cambiarPantalla("/views/RecomendationsPane.fxml");
    }

    /** Navega a la vista de "Mi Biblioteca". */
    @FXML private void handleShowBiblioteca() {
        cambiarPantalla("/views/UserLibraryPane.fxml");
    }

    /** Navega a la vista de perfil de usuario. */
    @FXML private void handleShowPerfil() {
        cambiarPantalla("/views/ProfilePane.fxml");
    }

    /**
     * Cierra la sesión actual y vuelve al login.
     * <p>
     * Carga la pantalla de login y la establece en el Stage principal.
     * </p>
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/LoginDataPane.fxml")
            );
            AnchorPane loginPane = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.setScene(new Scene(loginPane));
            stage.setTitle("Biblioteca");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método helper para cambiar de pantalla.
     *
     * @param ruta ruta al recurso FXML destino
     */
    private void cambiarPantalla(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(ruta)
            );
            AnchorPane root = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
