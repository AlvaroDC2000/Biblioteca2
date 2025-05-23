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

/**
 * Controlador de la pantalla de login.
 * <p>
 * Gestiona la autenticación de usuarios frente a la base de datos,
 * la navegación a la pantalla principal tras el login exitoso
 * y la transición a la pantalla de registro.
 * También carga el logo de la aplicación al inicializarse.
 * </p>
 */
public class LoginDataPaneController implements Initializable {

    /** Campo de texto para ingresar el correo electrónico del usuario. */
    @FXML
    private TextField emailField;

    /** Campo de texto para ingresar la contraseña del usuario. */
    @FXML
    private PasswordField passwordField;

    /** Imagen del logo que se muestra en la pantalla de login. */
    @FXML
    private ImageView logoID;

    /**
     * Manejador del botón "Iniciar sesión".
     * <p>
     * Obtiene los valores de email y contraseña y
     * delega en {@link #procesarLogin(String, String, ActionEvent)}.
     * </p>
     *
     * @param event evento de acción disparado al pulsar el botón
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        procesarLogin(email, password, event);
    }

    /**
     * Realiza el proceso de autenticación del usuario.
     * <ol>
     *   <li>Valida que los campos no estén vacíos.</li>
     *   <li>Consulta la base de datos buscando un usuario con las credenciales.</li>
     *   <li>Si existe, guarda el usuario en sesión y carga la vista principal.</li>
     *   <li>Si no existe o hay error, muestra una alerta apropiada.</li>
     * </ol>
     *
     * @param email    correo electrónico ingresado
     * @param password contraseña ingresada
     * @param event    evento de acción para obtener el Stage actual
     */
    public void procesarLogin(String email, String password, ActionEvent event) {
        // Validación de campos obligatorios
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Rellena todos los campos.");
            return;
        }

        // Abrir sesión de Hibernate para consulta
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Crear consulta parametrizada para evitar inyecciones SQL
            Query<Usuario> query = session.createQuery(
                "FROM Usuario WHERE email = :email AND password = :password", Usuario.class
            );
            query.setParameter("email", email);
            query.setParameter("password", password);
            Usuario usuario = query.uniqueResult();

            if (usuario != null) {
                // Login exitoso: guardar usuario en sesión y cargar pantalla principal
                SessionManager.setUsuarioActual(usuario);
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/HomePane.fxml")
                );
                AnchorPane homePane = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(homePane);
                stage.setScene(scene);
                stage.show();
            } else {
                // Credenciales incorrectas
                showAlert(Alert.AlertType.ERROR, "Credenciales incorrectas.");
            }
        } catch (Exception e) {
            // Error de acceso a la base de datos
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al acceder a la base de datos.");
        }
    }

    /**
     * Manejador del enlace o botón "Registrarse".
     * <p>
     * Carga la vista de registro de usuario y la establece
     * como la escena actual.
     * </p>
     *
     * @param event evento de acción para obtener el Stage actual
     */
    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/RegisterDataPane.fxml")
            );
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

    /**
     * Muestra una alerta emergente al usuario.
     *
     * @param type    tipo de alerta (INFO, WARNING, ERROR, etc.)
     * @param message mensaje a mostrar en la alerta
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Inicializador del controlador.
     * <p>
     * Se ejecuta una vez cargado el FXML y su contexto.
     * Carga la imagen del logo de la aplicación.
     * </p>
     *
     * @param location  ubicación del recurso FXML (no usado)
     * @param resources bundle de recursos (no usado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String imageUrl =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/"
          + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png";
        try {
            // Intentar cargar la imagen directamente desde la URL
            Image image = new Image(imageUrl);
            if (!image.isError()) {
                logoID.setImage(image);
            }
        } catch (Exception e) {
            // En caso de fallo, imprimir stack trace
            e.printStackTrace();
        }
    }
}
