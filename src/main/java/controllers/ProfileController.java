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
 * Muestra los datos del usuario (email y género favorito),
 * permite editar y guardar el género favorito,
 * cambiar su contraseña y eliminar la cuenta.
 * También maneja la navegación lateral.
 * </p>
 */
public class ProfileController implements Initializable {

    @FXML private ImageView drawerLogo;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> generoComboBox;
    @FXML private PasswordField nuevaPassField;
    @FXML private PasswordField repitePassField;

    private Usuario usuario;

    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=bestseller&maxResults=30&langRestrict=es";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        cargarDatosUsuario();
        cargarGenerosDesdeAPI();
    }

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

    private void cargarDatosUsuario() {
        usuario = SessionManager.getUsuarioActual();
        if (usuario != null) {
            emailField.setText(usuario.getEmail());
            // Luego de cargar géneros, seleccionamos el actual:
            Platform.runLater(() -> generoComboBox.setValue(usuario.getGeneroFavorito()));
        }
    }

    /**
     * Lanza un hilo para pedir las categorías a Google Books y
     * rellenar el ComboBox.
     */
    private void cargarGenerosDesdeAPI() {
        new Thread(() -> {
            Set<String> generos = new TreeSet<>();
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(GOOGLE_BOOKS_API).openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) sb.append(line);
                rd.close();

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
                // Si ya había usuario cargado, restablecer selección:
                if (usuario != null) {
                    generoComboBox.setValue(usuario.getGeneroFavorito());
                }
            });
        }).start();
    }

    /** Guarda el género favorito editado por el usuario. */
    @FXML
    private void handleSaveGenero() {
        String nuevo = generoComboBox.getValue();
        if (nuevo == null || nuevo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selecciona un género.");
            return;
        }
        usuario.setGeneroFavorito(nuevo);
        UsuarioDAO.updateUsuario(usuario);
        showAlert(Alert.AlertType.INFORMATION, "Género favorito actualizado.");
    }

    /** Cambia la contraseña validando longitud y coincidencia. */
    @FXML
    private void handleChangePassword() {
        String p1 = nuevaPassField.getText(), p2 = repitePassField.getText();
        if (p1.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "La contraseña debe tener 6+ caracteres.");
            return;
        }
        if (!p1.equals(p2)) {
            showAlert(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }
        usuario.setPassword(p1);
        UsuarioDAO.updateUsuario(usuario);
        showAlert(Alert.AlertType.INFORMATION, "Contraseña cambiada.");
        nuevaPassField.clear();
        repitePassField.clear();
    }

    /** Elimina la cuenta tras pedir confirmación. */
    @FXML
    private void handleDeleteAccount() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Esta acción no se puede deshacer.", ButtonType.CANCEL, ButtonType.OK);
        c.setHeaderText("¿Eliminar cuenta?");
        Optional<ButtonType> opt = c.showAndWait();
        if (opt.orElse(ButtonType.CANCEL) == ButtonType.OK) {
            dao.UsuarioDAO.deleteUsuario(usuario);
            SessionManager.clear();
            volverALogin();
        }
    }

    private void showAlert(Alert.AlertType t, String m) {
        Alert a = new Alert(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private void volverALogin() {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("/views/LoginDataPane.fxml"));
            Stage st = (Stage) drawerLogo.getScene().getWindow();
            st.setScene(new Scene(pane)); st.setTitle("Biblioteca"); st.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /* — Navegación lateral — */
    @FXML private void handleShowBuscar()          { cambiar("/views/HomePane.fxml"); }
    @FXML private void handleShowRecomendaciones() { cambiar("/views/Recomendations.fxml"); }
    @FXML private void handleShowBiblioteca()      { cambiar("/views/UserLibrary.fxml"); }
    @FXML private void handleShowPerfil()          { /* ya estás */ }
    @FXML private void handleLogout()              { SessionManager.clear(); volverALogin(); }

    private void cambiar(String r) {
        try {
            AnchorPane p = FXMLLoader.load(getClass().getResource(r));
            Stage st = (Stage) drawerLogo.getScene().getWindow();
            st.getScene().setRoot(p);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
