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
 * <p>
 * Gestiona la inicialización de la vista, la carga del logo,
 * la validación y persistencia de datos, así como la navegación
 * entre las diferentes pantallas de la aplicación.
 * </p>
 */
public class AddLibroController implements Initializable {

    /** Logo que aparece en el menú lateral. */
    @FXML
    private ImageView drawerLogo;

    /** Campo de texto para el título del libro. */
    @FXML
    private TextField tituloField;

    /** Campo de texto para el autor del libro. */
    @FXML
    private TextField autorField;

    /** Campo de texto para la editorial del libro. */
    @FXML
    private TextField editorialField;

    /** Área de texto para la descripción del libro. */
    @FXML
    private TextArea descripcionField;

    /** Campo de texto para la URL de la imagen de portada. */
    @FXML
    private TextField imagenUrlField;

    /**
     * Inicializa el controlador tras cargar el FXML.
     * <p>
     * Se utiliza para realizar tareas de arranque, como
     * cargar el logo en el ImageView correspondiente.
     * </p>
     *
     * @param location  ubicación del recurso FXML (no usado)
     * @param resources bundle de recursos (no usado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
    }

    /**
     * Descarga y establece la imagen del logo desde una URL externa.
     * <p>
     * Añade un header de User-Agent para evitar bloqueos de conexión.
     * En caso de error, imprime la traza de la excepción.
     * </p>
     */
    private void cargarLogo() {
        try {
            // Construye la URL del logo y abre conexión HTTP
            URL url = new URI(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/"
              + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png"
            ).toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Lee el stream de datos y asigna la imagen al ImageView
            try (InputStream is = connection.getInputStream()) {
                drawerLogo.setImage(new Image(is));
            }
        } catch (Exception e) {
            // Log en consola si hay fallo al cargar imagen
            e.printStackTrace();
        }
    }

    /**
     * Manejador para el botón "Guardar libro".
     * <p>
     * Valida los campos obligatorios (título y autor), crea
     * la entidad Libro, la asocia al usuario actual, la persiste
     * en la base de datos y muestra una alerta de éxito.
     * Finalmente, vuelve a la vista de la biblioteca.
     * </p>
     */
    @FXML
    private void handleGuardarLibro() {
        String titulo = tituloField.getText().trim();
        String autor  = autorField.getText().trim();

        // Validación de campos obligatorios
        if (titulo.isEmpty() || autor.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos incompletos");
            alert.setHeaderText(null);
            alert.setContentText("Los campos 'Título' y 'Autor' son obligatorios.");
            alert.showAndWait();
            return;
        }

        // Creación de la entidad Libro con los datos ingresados
        Libro libro = new Libro(
            titulo,
            autor,
            descripcionField.getText().trim(),
            imagenUrlField.getText().trim(),
            editorialField.getText().trim()
        );
        // Asocia el libro al usuario autenticado
        libro.setUsuario(SessionManager.getUsuarioActual());
        // Persiste el libro en la base de datos
        LibroDAO.guardarLibro(libro);

        // Muestra alerta de confirmación
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("El libro ha sido guardado correctamente.");
        alert.showAndWait();

        // Navega de vuelta a la vista de biblioteca
        volverABiblioteca();
    }

    /**
     * Manejador para el botón "Cancelar".
     * <p>
     * Descarta la operación actual y vuelve a la vista
     * de la biblioteca sin guardar cambios.
     * </p>
     */
    @FXML
    private void handleCancelar() {
        volverABiblioteca();
    }

    /**
     * Recarga la vista de "Mi Biblioteca" como escena principal.
     * <p>
     * Carga el FXML correspondiente y lo establece como
     * la nueva escena en el Stage actual.
     * </p>
     */
    private void volverABiblioteca() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserLibraryPane.fxml"));
            AnchorPane pane = loader.load();
            Stage stage = (Stage) tituloField.getScene().getWindow();
            stage.setScene(new Scene(pane));
        } catch (Exception e) {
            // Log en consola si falla la recarga de la vista
            e.printStackTrace();
        }
    }

    /**
     * Navega a la vista de búsqueda de libros.
     * <p>
     * Utiliza el helper cambiarPantalla con la ruta al FXML.
     * </p>
     */
    @FXML
    private void handleShowBuscar() {
        cambiarPantalla("/views/HomePanePane.fxml");
    }

    /**
     * Navega a la vista de recomendaciones.
     */
    @FXML
    private void handleShowRecomendaciones() {
        cambiarPantalla("/views/RecomendationsPane.fxml");
    }

    /**
     * Navega a la vista de "Mi Biblioteca".
     */
    @FXML
    private void handleShowBiblioteca() {
        cambiarPantalla("/views/UserLibraryPane.fxml");
    }

    /**
     * Navega a la vista de perfil de usuario.
     */
    @FXML
    private void handleShowPerfil() {
        cambiarPantalla("/views/ProfilePane.fxml");
    }

    /**
     * Cierra la sesión actual y vuelve al login.
     * <p>
     * Carga la escena de login y la muestra en el Stage principal,
     * reiniciando el título de la ventana.
     * </p>
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
            // Log en consola si falla el logout
            e.printStackTrace();
        }
    }

    /**
     * Método helper para cambiar de pantalla.
     * <p>
     * Carga el FXML indicado y reemplaza el root de la escena actual.
     * </p>
     *
     * @param ruta ruta del recurso FXML destino
     */
    private void cambiarPantalla(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            AnchorPane root = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            // Reemplaza el contenido raíz sin cambiar el Stage
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            // Log en consola si hay error al cambiar de pantalla
            e.printStackTrace();
        }
    }

}
