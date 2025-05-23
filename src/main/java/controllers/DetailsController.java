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
 * Controlador para la vista de detalles de un libro.
 * <p>
 * Carga los datos básicos, permite editar estado/comentarios,
 * muestra opiniones previas y publica nuevas opiniones.
 * </p>
 */
public class DetailsController {

    @FXML private VBox detailsContainer;
    @FXML private ImageView drawerLogo;
    @FXML private ImageView bookImage;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label editorialLabel;
    @FXML private TextArea descriptionArea;

    @FXML private ComboBox<String> estadoCombo;
    @FXML private DatePicker fechaLecturaPicker;
    @FXML private TextArea comentarioArea;
    @FXML private TextField prestadoAField;
    @FXML private DatePicker fechaPrestamoPicker;
    @FXML private CheckBox devueltoCheck;

    @FXML private VBox opinionesContainer;
    @FXML private Spinner<Integer> notaOpinionSpinner;
    @FXML private TextArea contenidoOpinionArea;

    private Libro libro;

    /** Límite de palabras para la descripción */
    private static final int DESC_WORD_LIMIT = 300;

    /**
     * Inicialización del controlador:
     * - Carga el logo del menú lateral.
     * - Población del combo de estados y spinner de nota.
     */
    @FXML
    public void initialize() {
        cargarLogo();
        estadoCombo.getItems().setAll("Leído", "Pendiente", "Prestado", "Deseo comprar");
        notaOpinionSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5)
        );
    }

    /**
     * Recibe el libro seleccionado y actualiza todos los campos de la vista.
     * @param libro modelo de libro seleccionado
     */
    public void setLibro(Libro libro) {
        this.libro = libro;
        titleLabel.setText(libro.getTitulo());
        authorLabel.setText("Autor: " + libro.getAutor());
        editorialLabel.setText("Editorial: " +
            (libro.getEditorial() != null ? libro.getEditorial() : "Desconocida")
        );
        descriptionArea.setText(truncateWords(libro.getDescripcion(), DESC_WORD_LIMIT));

        if (libro.getEstado() != null)      estadoCombo.setValue(libro.getEstado());
        if (libro.getFechaLectura() != null) fechaLecturaPicker.setValue(libro.getFechaLectura());
        if (libro.getComentario() != null)   comentarioArea.setText(libro.getComentario());
        if (libro.getPrestadoA() != null)    prestadoAField.setText(libro.getPrestadoA());
        if (libro.getFechaPrestamo() != null) fechaPrestamoPicker.setValue(libro.getFechaPrestamo());
        if (libro.getDevuelto() != null)     devueltoCheck.setSelected(libro.getDevuelto());

        cargarImagenLibro(libro.getImagenUrl());
        mostrarOpiniones();

        Usuario actual = SessionManager.getUsuarioActual();
        contenidoOpinionArea.setPromptText("Opinión de " + actual.getEmail());
    }

    /**
     * Corta un texto tras N palabras, agregando “…” si supera el límite.
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
     * Muestra en la interfaz todas las opiniones que se han hecho sobre el libro,
     * desplazadas al final para mayor claridad.
     */
    private void mostrarOpiniones() {
        opinionesContainer.getChildren().clear();
        List<Opinion> lista = OpinionDAO.obtenerOpinionesPorTituloYAutor(
            libro.getTitulo(), libro.getAutor()
        );
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
     * Descarga y coloca la imagen del libro desde su URL
     * en un hilo para no bloquear la UI.
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
                    Platform.runLater(() -> bookImage.setImage(img));
                }
            } catch (Exception ignored) { }
        }).start();
    }

    /** Guarda todos los cambios del libro en la base de datos. */
    @FXML private void handleSaveToLibrary() {
        libro.setEstado(estadoCombo.getValue());
        libro.setFechaLectura(fechaLecturaPicker.getValue());
        libro.setComentario(comentarioArea.getText());
        libro.setPrestadoA(prestadoAField.getText());
        libro.setFechaPrestamo(fechaPrestamoPicker.getValue());
        libro.setDevuelto(devueltoCheck.isSelected());
        libro.setUsuario(SessionManager.getUsuarioActual());
        LibroDAO.guardarLibro(libro);
    }

    /**
     * Publica o actualiza la opinión del usuario actual
     * y refresca la sección de opiniones al final.
     */
    @FXML private void handleAgregarOpinion() {
        Opinion exist = OpinionDAO.obtenerOpinionPorLibroYUsuario(libro, SessionManager.getUsuarioActual());
        int nota = notaOpinionSpinner.getValue();
        String texto = contenidoOpinionArea.getText().trim();
        if (texto.isEmpty()) { showAlert("Escribe tu opinión."); return; }

        if (exist != null) {
            exist.setNota(nota);
            exist.setContenido(texto);
            OpinionDAO.actualizarOpinion(exist);
        } else {
            Opinion op = new Opinion(SessionManager.getUsuarioActual().getEmail(), texto, nota, libro);
            OpinionDAO.guardarOpinion(op);
        }
        mostrarOpiniones();
        contenidoOpinionArea.clear();
        notaOpinionSpinner.getValueFactory().setValue(5);
    }

    /** Carga el logo del menú lateral desde una URL externa. */
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

    /** Muestra una alerta sencilla al usuario. */
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Aviso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /** Navegación lateral idéntica a otros controladores. */
    @FXML private void handleShowBuscar()          {
      cambiarPantalla("/views/HomePane.fxml");
      }
    
    @FXML private void handleShowRecomendaciones() {
      cambiarPantalla("/views/Recomendations.fxml");
      }
    
    @FXML private void handleShowBiblioteca()      {
      cambiarPantalla("/views/UserLibrary.fxml");
      }
    
    @FXML private void handleShowPerfil()          {
      cambiarPantalla("/views/Profile.fxml");
      }

    /** Cierra sesión y vuelve a login. */
    @FXML private void handleLogout() {
        try {
            FXMLLoader ld = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
            AnchorPane p = ld.load();
            Stage s = (Stage) drawerLogo.getScene().getWindow();
            s.setScene(new Scene(p));
            s.setTitle("Biblioteca"); s.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Helper para cambiar la vista dentro del mismo Stage. */
    private void cambiarPantalla(String fxml) {
        try {
            FXMLLoader ld = new FXMLLoader(getClass().getResource(fxml));
            AnchorPane r = ld.load();
            Stage s = (Stage) drawerLogo.getScene().getWindow();
            s.getScene().setRoot(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
