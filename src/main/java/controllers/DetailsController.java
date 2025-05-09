package controllers;

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
import dao.LibroDAO;
import dao.OpinionDAO;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class DetailsController {

  // Referencias a los elementos de la interfaz FXML
  @FXML private VBox detailsContainer;
  @FXML private ImageView drawerLogo;
  @FXML private ImageView bookImage;
  @FXML private Label titleLabel;
  @FXML private Label authorLabel;
  @FXML private Label editorialLabel;
  @FXML private TextArea descriptionArea;

  @FXML private VBox opinionesContainer;

  @FXML private ComboBox<String> estadoCombo;
  @FXML private DatePicker fechaLecturaPicker;
  @FXML private TextArea comentarioArea;

  @FXML private TextField prestadoAField;
  @FXML private DatePicker fechaPrestamoPicker;
  @FXML private CheckBox devueltoCheck;

  @FXML private TextField autorOpinionField;
  @FXML private Spinner<Integer> notaOpinionSpinner;
  @FXML private TextArea contenidoOpinionArea;

  private Libro libro;

  // Método que se llama al cargar un libro, y actualiza la interfaz con sus datos
  public void setLibro(Libro libro) {
      this.libro = libro;

      // Asignar los datos del libro a los campos de la vista
      titleLabel.setText(libro.getTitulo());
      authorLabel.setText("Autor: " + libro.getAutor());
      editorialLabel.setText("Editorial: " + (libro.getEditorial() != null ? libro.getEditorial() : "Desconocida"));
      descriptionArea.setText(libro.getDescripcion());

      // Rellenar campos si el libro ya tiene datos guardados
      if (libro.getEstado() != null)
          estadoCombo.setValue(libro.getEstado());

      if (libro.getFechaLectura() != null)
          fechaLecturaPicker.setValue(libro.getFechaLectura());

      if (libro.getComentario() != null)
          comentarioArea.setText(libro.getComentario());

      if (libro.getPrestadoA() != null)
          prestadoAField.setText(libro.getPrestadoA());

      if (libro.getFechaPrestamo() != null)
          fechaPrestamoPicker.setValue(libro.getFechaPrestamo());

      if (libro.getDevuelto() != null)
          devueltoCheck.setSelected(libro.getDevuelto());

      // Mostrar imagen del libro
      cargarImagenLibro(libro.getImagenUrl());

      // Mostrar opiniones de todos los usuarios para este libro
      mostrarOpiniones();

      // Rellenar automáticamente el campo del autor de la opinión con el email del usuario actual
      Usuario actual = SessionManager.getUsuarioActual();
      autorOpinionField.setText(actual.getEmail());
      autorOpinionField.setDisable(true);
  }

  // Muestra en la interfaz todas las opiniones que se han hecho sobre el libro
  public void mostrarOpiniones() {
    opinionesContainer.getChildren().clear();
    List<Opinion> opiniones = OpinionDAO.obtenerOpinionesPorTituloYAutor(libro.getTitulo(), libro.getAutor());

    for (Opinion opinion : opiniones) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setStyle("-fx-background-color: #2a4d7a; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

        Label autorLabel = new Label(opinion.getAutor() + " (" + opinion.getNota() + "/10):");
        autorLabel.setStyle("-fx-font-weight: bold;");
        Label contenidoLabel = new Label(opinion.getContenido());

        card.getChildren().addAll(autorLabel, contenidoLabel);
        opinionesContainer.getChildren().add(card);
    }
}

  // Carga la imagen del libro desde su URL
  private void cargarImagenLibro(String urlStr) {
      try {
          if (urlStr != null && !urlStr.isEmpty()) {
              URL url = URI.create(urlStr).toURL();
              URLConnection connection = url.openConnection();
              connection.setRequestProperty("User-Agent", "Mozilla/5.0");
              try (InputStream stream = connection.getInputStream()) {
                  bookImage.setImage(new Image(stream));
              }
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Inicialización del controlador: se configura el combo y el spinner
  @FXML
  public void initialize() {
      cargarLogo();
      estadoCombo.getItems().addAll("Leído", "Pendiente", "Prestado", "Deseo comprar");
      notaOpinionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5));
  }

  // Método que se ejecuta cuando el usuario quiere dejar una opinión
  @FXML
  private void handleAgregarOpinion() {
      Integer nota = notaOpinionSpinner.getValue();
      String contenido = contenidoOpinionArea.getText().trim();

      if (contenido.isEmpty()) {
          showAlert("Por favor, escribe un comentario.");
          return;
      }

      Usuario actual = SessionManager.getUsuarioActual();
      Opinion existente = OpinionDAO.obtenerOpinionPorLibroYUsuario(libro, actual);

      if (existente != null) {
          // Si ya había opinión, la actualiza
          existente.setNota(nota);
          existente.setContenido(contenido);
          OpinionDAO.actualizarOpinion(existente);
      } else {
          // Si no había opinión, la guarda nueva
          Opinion nueva = new Opinion(actual.getEmail(), contenido, nota, libro);
          nueva.setUsuario(actual);
          OpinionDAO.guardarOpinion(nueva);
      }

      // Refresca la lista de opiniones
      mostrarOpiniones();
      contenidoOpinionArea.clear();
      notaOpinionSpinner.getValueFactory().setValue(5);
  }

  // Guarda los datos del libro (estado, fechas, nota, etc.) en la base de datos
  @FXML
  private void handleSaveToLibrary() {
      if (libro != null) {
          libro.setEstado(estadoCombo.getValue());
          libro.setFechaLectura(fechaLecturaPicker.getValue());
          libro.setNota(notaOpinionSpinner.getValue());
          libro.setComentario(comentarioArea.getText());
          libro.setPrestadoA(prestadoAField.getText());
          libro.setFechaPrestamo(fechaPrestamoPicker.getValue());
          libro.setDevuelto(devueltoCheck.isSelected());
          libro.setUsuario(SessionManager.getUsuarioActual());
          LibroDAO.guardarLibro(libro);
      }
  }

  // Carga el logo desde una URL externa
  private void cargarLogo() {
      try {
          URL logoUrl = URI.create("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png").toURL();
          URLConnection connection = logoUrl.openConnection();
          connection.setRequestProperty("User-Agent", "Mozilla/5.0");
          try (InputStream stream = connection.getInputStream()) {
              drawerLogo.setImage(new Image(stream));
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Método auxiliar para cambiar entre pantallas usando rutas FXML
  private void cambiarPantalla(String rutaFXML) {
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
          AnchorPane root = loader.load();
          Stage stage = (Stage) drawerLogo.getScene().getWindow();
          stage.getScene().setRoot(root);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Muestra una alerta sencilla al usuario
  private void showAlert(String msg) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Aviso");
      alert.setHeaderText(null);
      alert.setContentText(msg);
      alert.showAndWait();
  }

  // Métodos para navegación del menú lateral
  @FXML
  private void handleShowBuscar() {
      cambiarPantalla("/views/HomePane.fxml");
  }

  @FXML
  private void handleShowRecomendaciones() {
      cambiarPantalla("/views/Recomendations.fxml");
  }

  @FXML
  private void handleShowBiblioteca() {
      cambiarPantalla("/views/UserLibrary.fxml");
  }

  @FXML
  private void handleShowPerfil() {
      cambiarPantalla("/views/Profile.fxml");
  }

  // Cierra sesión y vuelve a la pantalla de login
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
}
