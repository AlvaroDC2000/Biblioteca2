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
     * Inicialización del controlador tras cargar el FXML.
     * Carga el logo, los datos del usuario y los géneros desde la API.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        cargarDatosUsuario();
        cargarGenerosDesdeAPI();
    }

    /**
     * Descarga y establece la imagen del logo en el ImageView.
     * Configura User-Agent para evitar bloqueos HTTP.
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
     * Programa la selección del género favorito una vez cargado el ComboBox.
     */
    private void cargarDatosUsuario() {
        usuario = SessionManager.getUsuarioActual();
        if (usuario != null) {
            emailField.setText(usuario.getEmail());
            Platform.runLater(() ->
                generoComboBox.setValue(usuario.getGeneroFavorito())
            );
        }
    }

    /**
     * Lanza un hilo que solicita géneros a Google Books API y los añade al ComboBox.
     * En caso de fallo o sin resultados, añade "General".
     */
    private void cargarGenerosDesdeAPI() {
        new Thread(() -> {
            Set<String> generos = new TreeSet<>();
            try {
                URI uri = URI.create(GOOGLE_BOOKS_API);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("GET");
                StringBuilder sb = new StringBuilder();
                try (BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                }
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
            Platform.runLater(() -> {
                if (generos.isEmpty()) {
                    generoComboBox.getItems().add("General");
                } else {
                    generoComboBox.getItems().addAll(generos);
                }
                if (usuario != null) {
                    generoComboBox.setValue(usuario.getGeneroFavorito());
                }
            });
        }).start();
    }

    /**
     * Guarda el género favorito seleccionado.
     * Valida selección y muestra alerta tras actualizar.
     */
    @FXML
    private void handleSaveGenero() {
        String nuevo = generoComboBox.getValue();
        if (nuevo == null || nuevo.isEmpty()) {
            Platform.runLater(() ->
                showAlert(Alert.AlertType.WARNING, "Selecciona un género.")
            );
            return;
        }
        usuario.setGeneroFavorito(nuevo);
        UsuarioDAO.updateUsuario(usuario);
        Platform.runLater(() ->
            showAlert(Alert.AlertType.INFORMATION, "Género favorito actualizado.")
        );
    }

    /**
     * Permite cambiar la contraseña.
     * Comprueba longitud >=6 y coincidencia de campos.
     * Muestra alertas correspondientes.
     */
    @FXML
    private void handleChangePassword() {
        String p1 = nuevaPassField.getText();
        String p2 = repitePassField.getText();
        if (p1.length() < 6) {
            Platform.runLater(() ->
                showAlert(Alert.AlertType.WARNING, "La contraseña debe tener 6+ caracteres.")
            );
            return;
        }
        if (!p1.equals(p2)) {
            Platform.runLater(() ->
                showAlert(Alert.AlertType.WARNING, "Las contraseñas no coinciden.")
            );
            return;
        }
        usuario.setPassword(p1);
        UsuarioDAO.updateUsuario(usuario);
        Platform.runLater(() -> {
            showAlert(Alert.AlertType.INFORMATION, "Contraseña cambiada.");
            nuevaPassField.clear();
            repitePassField.clear();
        });
    }

    /**
     * Elimina la cuenta tras confirmación de diálogo.
     * Si confirma, borra usuario, limpia sesión y vuelve al login.
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
            Platform.runLater(this::volverALogin);
        }
    }

    /**
     * Muestra una alerta modal asociada al Stage principal.
     *
     * @param type tipo de alerta
     * @param msg  mensaje a mostrar
     */
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initOwner(drawerLogo.getScene().getWindow());
        alert.showAndWait();
    }

    /**
     * Carga de nuevo la vista de login tras eliminar cuenta.
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
    /** Navega a la vista de perfil. */
    @FXML private void handleShowPerfil() {
        cambiarPantalla("/views/ProfilePane.fxml");
    }
    /** Cierra sesión y vuelve al login. */
    @FXML private void handleLogout() {
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
     * Helper para cambiar el contenido del Scene sin recargar el Stage.
     *
     * @param ruta ruta FXML destino
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
