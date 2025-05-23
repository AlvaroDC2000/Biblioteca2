package controllers;

import dao.LibroDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Libro;
import models.Usuario;
import utils.SessionManager;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de "Mi Biblioteca".
 * <p>
 * Permite buscar, mostrar y eliminar libros de la biblioteca del usuario,
 * así como navegar entre las distintas pantallas de la aplicación.
 * </p>
 */
public class UserLibraryController implements Initializable {

    /** Campo de texto para filtrar la lista de libros. */
    @FXML private TextField searchField;

    /** Contenedor principal donde se muestran las tarjetas de libros. */
    @FXML private VBox libraryContainer;

    /** Logo que aparece en la barra lateral de navegación. */
    @FXML private ImageView drawerLogo;

    /** Formateador de fechas para mostrar fecha de lectura y de préstamo. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Se ejecuta al cargarse la pantalla.
     * <p>
     * Carga el logo y muestra todos los libros guardados sin aplicar filtro.
     * </p>
     *
     * @param location  ubicación del recurso FXML (no usado)
     * @param resources bundle de recursos (no usado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        mostrarLibrosGuardados("");
    }

    /**
     * Carga y establece el logo de la aplicación desde una URL externa.
     * <p>
     * Añade cabecera de User-Agent para evitar bloqueos HTTP.
     * </p>
     */
    private void cargarLogo() {
        try {
            // Construye la URL del logo
            URL url = new URI(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/"
              + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png"
            ).toURL();
            // Abre conexión y establece User-Agent
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            // Lee el stream de datos y asigna la imagen
            try (InputStream in = conn.getInputStream()) {
                drawerLogo.setImage(new Image(in));
            }
        } catch (Exception e) {
            // Log en caso de fallo
            e.printStackTrace();
        }
    }

    /**
     * Manejador para el TextField de búsqueda.
     * <p>
     * Obtiene el texto ingresado, lo normaliza a minúsculas,
     * y refresca la lista de libros aplicando ese filtro.
     * </p>
     */
    @FXML
    private void handleSearch() {
        String filtro = searchField.getText().trim().toLowerCase();
        mostrarLibrosGuardados(filtro);
    }

    /**
     * Recupera y muestra los libros del usuario actual que coincidan con el filtro.
     * <p>
     * Para cada libro que pase el filtro, crea una "tarjeta" con portada,
     * datos y botón de eliminar. Si no hay resultados, muestra un mensaje.
     * </p>
     *
     * @param filtro texto a buscar en título, autor o editorial
     */
    private void mostrarLibrosGuardados(String filtro) {
        // Limpia resultados previos
        libraryContainer.getChildren().clear();

        // Obtiene el usuario actual de sesión
        Usuario usuario = SessionManager.getUsuarioActual();
        // Recupera todos los libros pertenecientes al usuario
        List<Libro> lista = LibroDAO.obtenerPorUsuario(usuario);

        for (Libro libro : lista) {
            // Comprueba si el libro coincide con el filtro
            boolean coincide = filtro.isEmpty()
                || libro.getTitulo().toLowerCase().contains(filtro)
                || libro.getAutor().toLowerCase().contains(filtro)
                || (libro.getEditorial() != null
                    && libro.getEditorial().toLowerCase().contains(filtro));
            if (!coincide) continue;

            // Construye la tarjeta contenedora
            VBox card = new VBox(10);
            card.getStyleClass().add("card-libro");
            card.setPrefWidth(660);

            // Carga la portada si existe URL
            ImageView portada = new ImageView();
            portada.setFitWidth(100);
            portada.setPreserveRatio(true);
            if (libro.getImagenUrl() != null && !libro.getImagenUrl().isEmpty()) {
                try {
                    URL imgUrl = new URI(libro.getImagenUrl()).toURL();
                    URLConnection ic = imgUrl.openConnection();
                    ic.setRequestProperty("User-Agent", "Mozilla/5.0");
                    try (InputStream in = ic.getInputStream()) {
                        portada.setImage(new Image(in));
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo cargar imagen: " + libro.getTitulo());
                }
            }

            // Etiqueta de título con evento de clic para ver detalles
            Label title = new Label("Título: " + libro.getTitulo());
            title.getStyleClass().add("libro-title");
            title.setOnMouseClicked(e -> mostrarDetalles(libro));

            // Otras etiquetas de información
            Label author = new Label("Autor: " + libro.getAutor());
            author.getStyleClass().add("libro-author");

            Label editorial = new Label(
                "Editorial: " + (libro.getEditorial() != null
                    ? libro.getEditorial() : "Desconocida")
            );
            editorial.getStyleClass().add("libro-editorial");

            Label estado = new Label(
                "Estado: " + (libro.getEstado() != null
                    ? libro.getEstado() : "Sin especificar")
            );
            estado.getStyleClass().add("libro-estado");

            Label fechaLectura = new Label(
                "Leído el: " + (libro.getFechaLectura() != null
                    ? libro.getFechaLectura().format(DATE_FORMATTER)
                    : "No leído")
            );
            fechaLectura.getStyleClass().add("libro-fecha");

            Label comentario = new Label(
                "Comentario: " + (libro.getComentario() != null
                    ? libro.getComentario() : "Sin comentario")
            );
            comentario.setWrapText(true);
            comentario.setMaxWidth(500);
            comentario.getStyleClass().add("libro-comentario");

            Label prestado = new Label(
                "Prestado a: " + (libro.getPrestadoA() != null
                    ? libro.getPrestadoA() : "No prestado")
            );
            prestado.getStyleClass().add("libro-prestamo");

            Label fechaPrestamo = new Label(
                "Fecha préstamo: " + (libro.getFechaPrestamo() != null
                    ? libro.getFechaPrestamo().format(DATE_FORMATTER)
                    : "N/A")
            );
            fechaPrestamo.getStyleClass().add("libro-prestamo");

            Label devuelto = new Label(
                "¿Devuelto?: " + ((libro.getDevuelto() != null && libro.getDevuelto())
                    ? "Sí" : "No")
            );
            devuelto.getStyleClass().add("libro-prestamo");

            // Botón para eliminar el libro
            Button removeBtn = new Button("Eliminar");
            removeBtn.getStyleClass().add("button-remove");
            removeBtn.setOnAction(e -> {
                // Elimina de la base de datos y de la UI
                LibroDAO.eliminarLibro(libro);
                libraryContainer.getChildren().remove(card);
            });

            // HBox para alojar botones de acción
            HBox actions = new HBox(10, removeBtn);
            actions.setAlignment(Pos.CENTER_LEFT);

            // Agrupa etiquetas e botones en un VBox de detalles
            VBox details = new VBox(5,
                title, author, editorial, estado,
                fechaLectura, comentario,
                prestado, fechaPrestamo, devuelto,
                actions
            );
            // Combina portada y detalles en un HBox
            HBox row = new HBox(15, portada, details);
            card.getChildren().add(row);

            // Añade la tarjeta al contenedor principal
            libraryContainer.getChildren().add(card);
        }

        // Si no hay tarjetas, muestra mensaje de "sin resultados"
        if (libraryContainer.getChildren().isEmpty()) {
            Label noRes = new Label("No se encontraron resultados.");
            noRes.getStyleClass().add("no-results");
            libraryContainer.getChildren().add(noRes);
        }
    }

    /**
     * Navega a la pantalla de detalles del libro seleccionado.
     *
     * @param libro libro cuyos detalles se mostrarán
     */
    private void mostrarDetalles(Libro libro) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/DetailsPane.fxml")
            );
            AnchorPane pane = loader.load();
            DetailsController ctrl = loader.getController();
            ctrl.setLibro(libro);

            Stage st = (Stage) drawerLogo.getScene().getWindow();
            st.getScene().setRoot(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Abre la vista de añadir un libro manualmente. */
    @FXML
    private void handleShowAddLibro() {
        cambiarPantalla("/views/AddLibroPane.fxml");
    }

    /** Navega a la vista de búsqueda de libros. */
    @FXML
    private void handleShowBuscar() {
        cambiarPantalla("/views/HomePane.fxml");
    }

    /** Navega a la vista de recomendaciones. */
    @FXML
    private void handleShowRecomendaciones() {
        cambiarPantalla("/views/RecomendationsPane.fxml");
    }

    /** Navega a la vista de "Mi Biblioteca" (esta misma). */
    @FXML
    private void handleShowBiblioteca() {
        cambiarPantalla("/views/UserLibraryPane.fxml");
    }

    /** Navega a la vista de perfil de usuario. */
    @FXML
    private void handleShowPerfil() {
        cambiarPantalla("/views/ProfilePane.fxml");
    }

    /** Cierra sesión y vuelve a la pantalla de login. */
    @FXML
    private void handleLogout() {
        cambiarPantalla("/views/LoginDataPane.fxml");
    }

    /**
     * Helper para cambiar la raíz de la escena actual.
     * <p>
     * Carga el FXML indicado y lo asigna como root del Stage existente.
     * </p>
     *
     * @param ruta ruta al recurso FXML dentro de /views
     */
    private void cambiarPantalla(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            AnchorPane root = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("No se pudo cargar " + ruta);
            e.printStackTrace();
        }
    }
}
