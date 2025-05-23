package controllers;

import dao.LibroDAO;
import dao.OpinionDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Libro;
import models.Opinion;
import models.Usuario;
import utils.SessionManager;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.StringJoiner;

/**
 * Controlador de la vista de detalles de un libro.
 * <p>
 * Se encarga de cargar los datos básicos del libro,
 * editar estado y metadatos (fecha de lectura, nota, préstamo),
 * mostrar opiniones existentes y permitir al usuario añadir o
 * actualizar su propia opinión.
 * </p>
 */
public class DetailsController {

    /** Contenedor principal de la vista de detalles. */
    @FXML private VBox detailsContainer;

    /** Logo que aparece en el menú lateral. */
    @FXML private ImageView drawerLogo;

    /** Imagen de portada del libro. */
    @FXML private ImageView bookImage;

    /** Etiqueta para mostrar el título del libro. */
    @FXML private Label titleLabel;

    /** Etiqueta para mostrar el autor del libro. */
    @FXML private Label authorLabel;

    /** Etiqueta para mostrar la editorial del libro. */
    @FXML private Label editorialLabel;

    /** Área de texto para la descripción larga del libro. */
    @FXML private TextArea descriptionArea;

    /** ComboBox para seleccionar el estado de lectura del libro. */
    @FXML private ComboBox<String> estadoCombo;

    /** DatePicker para seleccionar la fecha en que se leyó el libro. */
    @FXML private DatePicker fechaLecturaPicker;

    /** Área de texto para añadir o editar comentario personal del libro. */
    @FXML private TextArea comentarioArea;

    /** Campo de texto para indicar a quién se ha prestado el libro. */
    @FXML private TextField prestadoAField;

    /** DatePicker para seleccionar la fecha de préstamo del libro. */
    @FXML private DatePicker fechaPrestamoPicker;

    /** Casilla para marcar si el libro prestado ha sido devuelto. */
    @FXML private CheckBox devueltoCheck;

    /** Contenedor donde se agregan las tarjetas de opiniones. */
    @FXML private VBox opinionesContainer;

    /** Spinner para seleccionar la nota de la nueva opinión (0–10). */
    @FXML private Spinner<Integer> notaOpinionSpinner;

    /** Área de texto para redactar el contenido de la nueva opinión. */
    @FXML private TextArea contenidoOpinionArea;

    /** Instancia del libro actualmente visualizado. */
    private Libro libro;

    /** Límite de palabras para truncar la descripción larga. */
    private static final int DESC_WORD_LIMIT = 300;

    /**
     * Se ejecuta automáticamente tras cargar el FXML.
     * <p>
     * - Carga el logo del menú lateral.<br>
     * - Inicializa el ComboBox de estados.<br>
     * - Configura el Spinner de nota de opinión.
     * </p>
     */
    @FXML
    public void initialize() {
        cargarLogo();
        // Opciones de estado de lectura
        estadoCombo.getItems().setAll("Leído", "Pendiente", "Prestado", "Deseo comprar");
        // Rango de notas de 0 a 10, valor inicial 5
        notaOpinionSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5)
        );
    }

    /**
     * Recibe la entidad Libro desde el controlador anterior
     * y actualiza todos los campos de la interfaz con sus datos.
     *
     * @param libro modelo de libro seleccionado
     */
    public void setLibro(Libro libro) {
        this.libro = libro;

        // Mostrar información básica
        titleLabel.setText(libro.getTitulo());
        authorLabel.setText("Autor: " + libro.getAutor());
        editorialLabel.setText("Editorial: " +
            (libro.getEditorial() != null ? libro.getEditorial() : "Desconocida")
        );
        // Truncar la descripción si supera el límite de palabras
        descriptionArea.setText(truncateWords(libro.getDescripcion(), DESC_WORD_LIMIT));

        // Cargar metadatos existentes
        if (libro.getEstado() != null)      estadoCombo.setValue(libro.getEstado());
        if (libro.getFechaLectura() != null) fechaLecturaPicker.setValue(libro.getFechaLectura());
        if (libro.getComentario() != null)   comentarioArea.setText(libro.getComentario());
        if (libro.getPrestadoA() != null)    prestadoAField.setText(libro.getPrestadoA());
        if (libro.getFechaPrestamo() != null) fechaPrestamoPicker.setValue(libro.getFechaPrestamo());
        if (libro.getDevuelto() != null)     devueltoCheck.setSelected(libro.getDevuelto());

        // Cargar imagen de portada de forma asíncrona
        cargarImagenLibro(libro.getImagenUrl());
        // Renderizar opiniones previas
        mostrarOpiniones();

        // Personalizar prompt del área de opinión con el email del usuario
        Usuario actual = SessionManager.getUsuarioActual();
        contenidoOpinionArea.setPromptText("Opinión de " + actual.getEmail());
    }

    /**
     * Trunca un texto tras un número máximo de palabras y añade "…" si se excede.
     *
     * @param text  texto original
     * @param limit número máximo de palabras
     * @return texto truncado o completo si no supera el límite
     */
    private String truncateWords(String text, int limit) {
        if (text == null) return "";
        String[] words = text.split("\\s+");
        if (words.length <= limit) return text;
        StringJoiner sj = new StringJoiner(" ");
        for (int i = 0; i < limit; i++) sj.add(words[i]);
        return sj.toString() + "…";
    }

    /**
     * Consulta la base de datos y muestra todas las opiniones
     * realizadas sobre este libro dentro de un contenedor VBox.
     */
    private void mostrarOpiniones() {
        opinionesContainer.getChildren().clear();
        // Recuperar lista de opiniones filtrando por título y autor
        List<Opinion> lista = OpinionDAO.obtenerOpinionesPorTituloYAutor(
            libro.getTitulo(), libro.getAutor()
        );
        // Crear tarjeta para cada opinión y agregarla al contenedor
        for (Opinion op : lista) {
            VBox card = new VBox(5);
            card.getStyleClass().add("detail-opinion-card");
            Label hdr = new Label(op.getAutor() + " — " + op.getNota() + "/10");
            hdr.getStyleClass().add("detail-opinion-header");
            Label body = new Label(op.getContenido());
            body.getStyleClass().add("detail-opinion-body");
            card.getChildren().setAll(hdr, body);
            opinionesContainer.getChildren().add(card);
        }
    }

    /**
     * Descarga la imagen desde una URL y la asigna al ImageView
     * sin bloquear la interfaz, usando un hilo de fondo.
     *
     * @param urlStr URL de la imagen de portada
     */
    private void cargarImagenLibro(String urlStr) {
        if (urlStr == null || urlStr.isEmpty()) return;
        new Thread(() -> {
            try {
                URL url = URI.create(urlStr).toURL();
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (InputStream in = conn.getInputStream()) {
                    Image img = new Image(in);
                    // Actualizar UI en el hilo de JavaFX
                    Platform.runLater(() -> bookImage.setImage(img));
                }
            } catch (Exception ignored) {
                // Ignorar fallos de carga de imagen
            }
        }).start();
    }

    /**
     * Guarda en la base de datos todos los cambios realizados
     * sobre el objeto Libro (estado, fechas, comentario, préstamo).
     */
    @FXML private void handleSaveToLibrary() {
        // Actualizar metadatos en el modelo
        libro.setEstado(estadoCombo.getValue());
        libro.setFechaLectura(fechaLecturaPicker.getValue());
        libro.setComentario(comentarioArea.getText());
        libro.setPrestadoA(prestadoAField.getText());
        libro.setFechaPrestamo(fechaPrestamoPicker.getValue());
        libro.setDevuelto(devueltoCheck.isSelected());
        libro.setUsuario(SessionManager.getUsuarioActual());
        // Persistir cambios
        LibroDAO.guardarLibro(libro);
    }

    /**
     * Añade o actualiza la opinión del usuario actual sobre el libro.
     * <p>
     * Si ya existe, edita la nota y contenido; si no, crea una nueva opinión.
     * Finalmente refresca la lista de opiniones y resetea los controles.
     * </p>
     */
    @FXML private void handleAgregarOpinion() {
        Opinion exist = OpinionDAO.obtenerOpinionPorLibroYUsuario(libro, SessionManager.getUsuarioActual());
        int nota = notaOpinionSpinner.getValue();
        String texto = contenidoOpinionArea.getText().trim();
        if (texto.isEmpty()) {
            showAlert("Escribe tu opinión.");
            return;
        }

        if (exist != null) {
            // Actualizar opinión existente
            exist.setNota(nota);
            exist.setContenido(texto);
            OpinionDAO.actualizarOpinion(exist);
        } else {
            // Crear y guardar nueva opinión
            Opinion op = new Opinion(SessionManager.getUsuarioActual().getEmail(), texto, nota, libro);
            OpinionDAO.guardarOpinion(op);
        }

        // Refrescar lista de opiniones en pantalla
        mostrarOpiniones();
        // Limpiar campos de entrada
        contenidoOpinionArea.clear();
        notaOpinionSpinner.getValueFactory().setValue(5);
    }

    /**
     * Carga el logo del menú lateral desde una URL externa
     * de forma similar a cargarImagenLibro.
     */
    private void cargarLogo() {
        try {
            URL logoUrl = URI.create(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/"
              + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png"
            ).toURL();
            URLConnection conn = logoUrl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = conn.getInputStream()) {
                drawerLogo.setImage(new Image(in));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra una alerta de tipo WARNING con un mensaje personalizado.
     *
     * @param msg texto a mostrar en la alerta
     */
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Aviso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
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
     * Cierra la sesión actual y retorna a la pantalla de login.
     * <p>
     * Carga el FXML de login y lo establece en el Stage principal.
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
            e.printStackTrace();
        }
    }

    /**
     * Helper para cambiar la pantalla activa sin recargar el Stage.
     *
     * @param ruta ruta relativa al recurso FXML deseado
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
