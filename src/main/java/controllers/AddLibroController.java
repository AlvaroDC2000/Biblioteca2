package controllers;

import dao.LibroDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Libro;
import utils.SessionManager;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de añadir un libro manualmente.
 * Permite al usuario completar los datos del libro, validarlos,
 * persistirlos en la base de datos y navegar entre vistas.
 */
public class AddLibroController implements Initializable {

    /** Logo que aparece en el menú lateral */
    @FXML
    private ImageView drawerLogo;

    /** Campo de texto para el título del libro */
    @FXML
    private TextField tituloField;

    /** Campo de texto para el autor del libro */
    @FXML
    private TextField autorField;

    /** Campo de texto para la editorial del libro */
    @FXML
    private TextField editorialField;

    /** Área de texto para la descripción del libro */
    @FXML
    private TextArea descripcionField;

    /** Campo de texto para la URL de la imagen de portada */
    @FXML
    private TextField imagenUrlField;

    /**
     * Inicializa el controlador tras cargar el FXML.
     * Carga el logo en el ImageView correspondiente.
     *
     * @param location  URL del recurso (no usado)
     * @param resources Bundle de recursos (no usado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
    }

    /**
     * Descarga y establece la imagen del logo desde una URL externa.
     * En caso de error, imprime el stack trace.
     */
    private void cargarLogo() {
        try {
            URL url = new URI(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/"
              + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png"
            ).toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream is = connection.getInputStream()) {
                drawerLogo.setImage(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manejador para el botón "Guardar libro".
     * Valida los campos obligatorios, crea la entidad Libro,
     * la persiste y muestra una alerta de éxito.
     * Luego vuelve a la vista de la biblioteca.
     */
    @FXML
    private void handleGuardarLibro() {
        String titulo = tituloField.getText().trim();
        String autor  = autorField .getText().trim();

        if (titulo.isEmpty() || autor.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos incompletos");
            alert.setHeaderText(null);
            alert.setContentText("Los campos 'Título' y 'Autor' son obligatorios.");
            alert.showAndWait();
            return;
        }

        Libro libro = new Libro(
            titulo,
            autor,
            descripcionField.getText().trim(),
            imagenUrlField.getText().trim(),
            editorialField.getText().trim()
        );
        libro.setUsuario(SessionManager.getUsuarioActual());
        LibroDAO.guardarLibro(libro);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("El libro ha sido guardado correctamente.");
        alert.showAndWait();

        volverABiblioteca();
    }

    /**
     * Manejador para el botón "Cancelar".
     * Simplemente vuelve a la vista de la biblioteca.
     */
    @FXML
    private void handleCancelar() {
        volverABiblioteca();
    }

    /**
     * Recarga la vista de "Mi Biblioteca" como escena principal.
     * Carga el FXML correspondiente y lo establece en el Stage actual.
     */
    private void volverABiblioteca() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserLibrary.fxml"));
            AnchorPane pane = loader.load();
            Stage stage = (Stage) tituloField.getScene().getWindow();
            stage.setScene(new Scene(pane));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Navega a la vista de búsqueda de libros */
    @FXML private void handleShowBuscar()           { cambiarPantalla("/views/HomePane.fxml");        }

    /** Navega a la vista de recomendaciones */
    @FXML private void handleShowRecomendaciones()  { cambiarPantalla("/views/Recomendations.fxml");  }

    /** Navega a la vista de "Mi Biblioteca" */
    @FXML private void handleShowBiblioteca()       { cambiarPantalla("/views/UserLibrary.fxml");     }

    /** Navega a la vista de perfil de usuario */
    @FXML private void handleShowPerfil()           { cambiarPantalla("/views/Profile.fxml");         }

    /**
     * Cierra la sesión actual y vuelve al login.
     * Carga y muestra la escena de login en el Stage principal.
     */
    @FXML
    private void handleLogout() {
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

    /**
     * Método helper para cambiar de pantalla.
     *
     * @param ruta Ruta al FXML de la vista destino
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
