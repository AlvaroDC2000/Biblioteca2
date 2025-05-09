package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Libro;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HomePaneController implements Initializable {

  // Campo de búsqueda, contenedor de resultados y logo lateral
  @FXML private TextField searchField;
  @FXML private VBox resultsContainer;
  @FXML private ImageView drawerLogo;

  // Método que se ejecuta al cargar la vista
  @Override
  public void initialize(URL location, ResourceBundle resources) {
      // Cargar el logo de la aplicación desde una URL externa
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

      // Al iniciar, mostrar libros populares por defecto
      buscarLibros("bestseller&orderBy=relevance");
  }

  // Acción del botón de búsqueda: toma el texto del campo y lanza la búsqueda
  @FXML
  private void handleSearch() {
      String query = searchField.getText().trim();
      if (!query.isEmpty()) {
          buscarLibros(query);
      }
  }

  // Realiza la búsqueda de libros usando la API de Google Books
  private void buscarLibros(String consulta) {
      resultsContainer.getChildren().clear(); // Limpiar resultados anteriores

      // Construcción de la URL de búsqueda
      String urlStr = "https://www.googleapis.com/books/v1/volumes?q=" +
              URLEncoder.encode(consulta, StandardCharsets.UTF_8) +
              "&maxResults=20&langRestrict=es";

      // Petición HTTP en un hilo separado para no bloquear la interfaz
      new Thread(() -> {
          try {
              URL url = URI.create(urlStr).toURL();
              HttpURLConnection conn = (HttpURLConnection) url.openConnection();
              conn.setRequestMethod("GET");

              BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
              StringBuilder result = new StringBuilder();
              String line;
              while ((line = reader.readLine()) != null) {
                  result.append(line);
              }
              reader.close();

              // Parsear JSON de la respuesta
              JSONObject json = new JSONObject(result.toString());
              JSONArray items = json.optJSONArray("items");

              // Mostrar los resultados en la interfaz
              Platform.runLater(() -> {
                  if (items != null && items.length() > 0) {
                      for (int i = 0; i < items.length(); i++) {
                          JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");

                          // Obtener datos básicos del libro
                          String title = volumeInfo.optString("title", "Sin título");
                          String editorial = volumeInfo.optString("publisher", "Desconocida");
                          String descripcion = volumeInfo.optString("description", "Sin descripción.");
                          String imagenUrl = "";

                          // Obtener miniatura si está disponible
                          if (volumeInfo.has("imageLinks")) {
                              imagenUrl = volumeInfo.getJSONObject("imageLinks")
                                          .optString("smallThumbnail", "")
                                          .replace("http://", "https://")
                                          .replace("&edge=curl", "")
                                          .replace("zoom=1", "zoom=2");
                          }

                          // Obtener autores si hay
                          String tempAuthors = "Autor desconocido";
                          if (volumeInfo.has("authors")) {
                              JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                              List<String> authorList = new ArrayList<>();
                              for (int j = 0; j < authorsArray.length(); j++) {
                                  authorList.add(authorsArray.getString(j));
                              }
                              tempAuthors = String.join(", ", authorList);
                          }

                          // Variables finales para usar dentro del hilo UI
                          final String authors = tempAuthors;
                          final String finalEditorial = editorial;
                          final String finalDescripcion = descripcion;
                          final String finalImagenUrl = imagenUrl;

                          // Crear una tarjeta visual para el libro
                          VBox card = new VBox(5);
                          card.getStyleClass().add("card-libro");
                          card.setPrefWidth(640);

                          ImageView portada = new ImageView();
                          portada.setFitWidth(100);
                          portada.setPreserveRatio(true);

                          // Cargar imagen desde la URL si existe
                          if (finalImagenUrl != null && !finalImagenUrl.isEmpty()) {
                              try {
                                  URI uri = URI.create(finalImagenUrl);
                                  HttpRequest request = HttpRequest.newBuilder(uri)
                                          .header("User-Agent", "Mozilla/5.0")
                                          .build();

                                  HttpClient client = HttpClient.newHttpClient();
                                  HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                                  portada.setImage(new Image(response.body()));
                              } catch (Exception e) {
                                  e.printStackTrace();
                              }
                          }

                          // Crear etiquetas de información
                          Label titleLabel = new Label("Título: " + title);
                          titleLabel.getStyleClass().add("libro-title");

                          Label authorLabel = new Label("Autor: " + authors);
                          authorLabel.getStyleClass().add("libro-author");

                          Label editorialLabel = new Label("Editorial: " + finalEditorial);
                          editorialLabel.getStyleClass().add("libro-editorial");

                          VBox infoBox = new VBox(5, titleLabel, authorLabel, editorialLabel);
                          HBox content = new HBox(15, portada, infoBox);
                          card.getChildren().add(content);

                          // Acción al hacer clic sobre la tarjeta: ir a la pantalla de detalles
                          card.setOnMouseClicked(e -> {
                              try {
                                  // Crear instancia de Libro con la información recuperada
                                  Libro libro = new Libro(title, authors, finalDescripcion, finalImagenUrl, finalEditorial);

                                  FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Details.fxml"));
                                  AnchorPane detailsPane = loader.load();

                                  controllers.DetailsController controller = loader.getController();
                                  controller.setLibro(libro);

                                  Stage stage = (Stage) searchField.getScene().getWindow();
                                  stage.getScene().setRoot(detailsPane);
                              } catch (Exception ex) {
                                  ex.printStackTrace();
                              }
                          });

                          // Añadir la tarjeta al contenedor principal
                          resultsContainer.getChildren().add(card);
                      }
                  } else {
                      resultsContainer.getChildren().add(new Label("No se encontraron resultados."));
                  }
              });

          } catch (Exception e) {
              Platform.runLater(() -> {
                  resultsContainer.getChildren().add(new Label("Error al conectar con Google Books."));
              });
              e.printStackTrace();
          }
      }).start();
  }

  // Métodos para navegación desde el Drawer
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

  // Cambia la vista actual por la indicada (usado para la navegación)
  private void cambiarPantalla(String ruta) {
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
          AnchorPane root = loader.load();
          Stage stage = (Stage) searchField.getScene().getWindow();
          stage.getScene().setRoot(root);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}
