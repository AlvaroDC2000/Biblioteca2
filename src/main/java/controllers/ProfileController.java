package controllers;

import dao.UsuarioDAO;
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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de "Mi Perfil".
 * <p>
 * Muestra los datos del usuario (email y género favorito),
 * permite cambiar su contraseña y eliminar la cuenta.
 * También maneja la navegación lateral.
 * </p>
 */
public class ProfileController implements Initializable {

    /** Logo que aparece en el menú lateral. */
    @FXML
    private ImageView drawerLogo;

    /** Campo de texto que muestra el email del usuario (no editable). */
    @FXML
    private TextField emailField;

    /** Campo de texto que muestra el género favorito del usuario (no editable). */
    @FXML
    private TextField generoField;

    /** Campo para introducir la nueva contraseña. */
    @FXML
    private PasswordField nuevaPassField;

    /** Campo para repetir la nueva contraseña. */
    @FXML
    private PasswordField repitePassField;

    /** Usuario actualmente logueado. */
    private Usuario usuario;

    /**
     * Inicializa el controlador una vez cargado el FXML.
     * <ul>
     *   <li>Carga el logo en el ImageView.</li>
     *   <li>Recupera y muestra los datos del usuario actual.</li>
     * </ul>
     *
     * @param location  URL del recurso (no usado)
     * @param resources Bundle de recursos (no usado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        cargarDatosUsuario();
    }

    /**
     * Descarga y asigna la imagen del logo al {@link #drawerLogo}.
     * En caso de fallo, imprime el stack trace en consola.
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
     * Carga los datos del usuario actual desde {@link SessionManager}
     * y los muestra en los campos correspondientes.
     */
    private void cargarDatosUsuario() {
        usuario = SessionManager.getUsuarioActual();
        if (usuario != null) {
            emailField.setText(usuario.getEmail());
            generoField.setText(usuario.getGeneroFavorito());
        }
    }

    /**
     * Maneja la acción de cambiar la contraseña.
     * <ol>
     *   <li>Valida longitud mínima de 6 caracteres.</li>
     *   <li>Comprueba que las dos contraseñas coincidan.</li>
     *   <li>Actualiza la entidad en BD mediante {@link UsuarioDAO}.</li>
     *   <li>Notifica al usuario del resultado.</li>
     * </ol>
     */
    @FXML
    private void handleChangePassword() {
        String pass1 = nuevaPassField.getText();
        String pass2 = repitePassField.getText();

        if (pass1.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!pass1.equals(pass2)) {
            showAlert(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }

        usuario.setPassword(pass1);
        UsuarioDAO.updateUsuario(usuario);
        showAlert(Alert.AlertType.INFORMATION, "Contraseña cambiada correctamente.");
        nuevaPassField.clear();
        repitePassField.clear();
    }

    /**
     * Maneja la acción de eliminar la cuenta.
     * <ol>
     *   <li>Pide confirmación al usuario.</li>
     *   <li>Si confirma, elimina la cuenta de BD y borra la sesión.</li>
     *   <li>Redirige al login.</li>
     * </ol>
     */
    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar cuenta");
        confirm.setHeaderText("¿Seguro que deseas eliminar tu cuenta?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.orElse(ButtonType.CANCEL) == ButtonType.OK) {
            UsuarioDAO.deleteUsuario(usuario);
            SessionManager.clear();
            volverALogin();
        }
    }

    /**
     * Muestra una ventana de alerta con el tipo y mensaje especificados.
     *
     * @param tipo Tipo de alerta ({@link Alert.AlertType})
     * @param msg  Mensaje a mostrar
     */
    private void showAlert(Alert.AlertType tipo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Redirige al usuario a la pantalla de login y actualiza el Stage.
     */
    private void volverALogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
            AnchorPane loginPane = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.setScene(new Scene(loginPane));
            stage.setTitle("Biblioteca");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ————— Navegación lateral ————— */

    /** Navega a la pantalla de búsqueda de libros. */
    @FXML private void handleShowBuscar()           { 
      cambiarPantalla("/views/HomePane.fxml");
      }

    /** Navega a la pantalla de recomendaciones. */
    @FXML private void handleShowRecomendaciones()  {
      cambiarPantalla("/views/Recomendations.fxml");
      }

    /** Navega a la pantalla de "Mi Biblioteca". */
    @FXML private void handleShowBiblioteca()       {
      cambiarPantalla("/views/UserLibrary.fxml");
      }

    /** Navega a la propia pantalla de perfil (esta vista). */
    @FXML private void handleShowPerfil()           {
      cambiarPantalla("/views/Profile.fxml");
      }

    /** Cierra la sesión y vuelve al login. */
    @FXML private void handleLogout() {
        SessionManager.clear();
        volverALogin();
    }

    /**
     * Método helper para cambiar la vista actual.
     *
     * @param ruta Ruta del FXML destino (p.e. "/views/HomePane.fxml")
     */
    private void cambiarPantalla(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            AnchorPane root = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
