package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Libro;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class UserLibraryController implements Initializable {

  // Elementos de la vista
  @FXML private TextField searchField;
  @FXML private VBox libraryContainer;
  @FXML private ImageView drawerLogo;

  // Al inicializar la pantalla, se carga el logo y se muestran los libros guardados
  @Override
  public void initialize(URL location, ResourceBundle resources) {
      cargarLogo();
      mostrarLibrosGuardados(""); // Mostrar todos los libros sin filtrar al inicio
  }

  // Carga el logo desde una URL externa
  private void cargarLogo() {
      try {
          URL url = new URI("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png").toURL();
          URLConnection connection = url.openConnection();
          connection.setRequestProperty("User-Agent", "Mozilla/5.0");
          try (InputStream inputStream = connection.getInputStream()) {
              drawerLogo.setImage(new Image(inputStream));
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Acción del campo de búsqueda para filtrar libros
  @FXML
  private void handleSearch() {
      String query = searchField.getText().trim().toLowerCase();
      mostrarLibrosGuardados(query);
  }

  // Muestra los libros guardados que coincidan con el filtro
  private void mostrarLibrosGuardados(String filtro) {
      libraryContainer.getChildren().clear(); // Limpiar resultados anteriores

      models.Usuario actual = utils.SessionManager.getUsuarioActual();
      for (Libro libro : dao.LibroDAO.obtenerPorUsuario(actual)) {
          // Verificamos si el libro coincide con el filtro por título, autor o editorial
          boolean coincide = filtro.isEmpty() ||
              libro.getTitulo().toLowerCase().contains(filtro) ||
              libro.getAutor().toLowerCase().contains(filtro) ||
              (libro.getEditorial() != null && libro.getEditorial().toLowerCase().contains(filtro));

          if (!coincide) continue;

          // Crear una tarjeta para cada libro
          VBox card = new VBox(10);
          card.getStyleClass().add("card-libro");
          card.setPrefWidth(660);

          // Mostrar imagen del libro si está disponible
          ImageView portada = new ImageView();
          portada.setFitWidth(100);
          portada.setPreserveRatio(true);
          if (libro.getImagenUrl() != null && !libro.getImagenUrl().isEmpty()) {
              try {
                  URL url = new URI(libro.getImagenUrl()).toURL();
                  URLConnection conn = url.openConnection();
                  conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                  try (InputStream input = conn.getInputStream()) {
                      portada.setImage(new Image(input));
                  }
              } catch (Exception e) {
                  System.out.println("No se pudo cargar la imagen del libro: " + libro.getTitulo());
              }
          }

          // Crear etiquetas con la información del libro
          Label title = new Label("Título: " + libro.getTitulo());
          title.getStyleClass().add("libro-title");
          // Agregar estilos al pasar el mouse y permitir ver detalles al hacer clic
          title.setOnMouseEntered(e -> title.getStyleClass().add("hovered"));
          title.setOnMouseExited(e -> title.getStyleClass().remove("hovered"));
          title.setOnMouseClicked(e -> mostrarDetalles(libro));

          Label author = new Label("Autor: " + libro.getAutor());
          author.getStyleClass().add("libro-author");

          Label editorial = new Label("Editorial: " + (libro.getEditorial() != null ? libro.getEditorial() : "Desconocida"));
          editorial.getStyleClass().add("libro-editorial");

          Label estado = new Label("Estado: " + (libro.getEstado() != null ? libro.getEstado() : "Sin especificar"));
          estado.getStyleClass().add("libro-estado");

          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

          Label fechaLectura = new Label("Leído el: " +
              (libro.getFechaLectura() != null ? libro.getFechaLectura().format(formatter) : "No leído"));
          fechaLectura.getStyleClass().add("libro-fecha");

          Label comentario = new Label("Comentario: " + (libro.getComentario() != null ? libro.getComentario() : "Sin comentario"));
          comentario.setWrapText(true);
          comentario.setMaxWidth(500);
          comentario.getStyleClass().add("libro-comentario");

          Label prestado = new Label("Prestado a: " +
              (libro.getPrestadoA() != null ? libro.getPrestadoA() : "No prestado"));
          prestado.getStyleClass().add("libro-prestamo");

          Label fechaPrestamo = new Label("Fecha préstamo: " +
              (libro.getFechaPrestamo() != null ? libro.getFechaPrestamo().format(formatter) : "N/A"));
          fechaPrestamo.getStyleClass().add("libro-prestamo");

          Label devuelto = new Label("¿Devuelto?: " +
              (libro.getDevuelto() != null && libro.getDevuelto() ? "Sí" : "No"));
          devuelto.getStyleClass().add("libro-prestamo");

          // Botón de "Me gusta" (estético)
          Button likeButton = new Button("❤ Me gusta");
          likeButton.getStyleClass().add("button-like");

          // Botón para eliminar el libro de la biblioteca
          Button removeButton = new Button("Eliminar");
          removeButton.getStyleClass().add("button-remove");
          removeButton.setOnAction(e -> {
              dao.LibroDAO.eliminarLibro(libro);
              libraryContainer.getChildren().remove(card);
          });

          // Contenedor para botones de acción
          HBox actions = new HBox(10, likeButton, removeButton);
          actions.setAlignment(Pos.CENTER_LEFT);

          // Caja de detalles del libro
          VBox detailsBox = new VBox(5, title, author, editorial, estado, fechaLectura, comentario, prestado, fechaPrestamo, devuelto, actions);
          HBox content = new HBox(15, portada, detailsBox);

          card.getChildren().add(content);
          libraryContainer.getChildren().add(card);
      }

      // Si no hay resultados, mostrar mensaje
      if (libraryContainer.getChildren().isEmpty()) {
          Label noResults = new Label("No se encontraron resultados.");
          noResults.getStyleClass().add("no-results");
          libraryContainer.getChildren().add(noResults);
      }
  }

  // Abre la pantalla de detalles del libro seleccionado
  private void mostrarDetalles(Libro libro) {
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Details.fxml"));
          AnchorPane detailsPane = loader.load();
          DetailsController controller = loader.getController();
          controller.setLibro(libro);

          Stage stage = (Stage) drawerLogo.getScene().getWindow();
          stage.getScene().setRoot(detailsPane);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Métodos de navegación desde el Drawer
  @FXML private void handleShowBuscar() { 
      cambiarPantalla("/views/HomePane.fxml");
  }

  @FXML private void handleShowRecomendaciones() { 
      cambiarPantalla("/views/Recomendations.fxml");
  }

  @FXML private void handleShowBiblioteca() { 
      cambiarPantalla("/views/UserLibrary.fxml");
  }

  @FXML private void handleShowPerfil() { 
      cambiarPantalla("/views/Profile.fxml");
  }

  // Cierra sesión y vuelve a la pantalla de login
  @FXML
  private void handleLogout() {
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
          AnchorPane loginPane = loader.load();
          Stage stage = (Stage) drawerLogo.getScene().getWindow();
          stage.setScene(new javafx.scene.Scene(loginPane));
          stage.setTitle("Biblioteca");
          stage.show();
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Método reutilizable para cambiar entre pantallas
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
