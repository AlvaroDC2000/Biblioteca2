package controllers;

import dao.LibroDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Libro;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.SessionManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de recomendaciones.
 * <p>
 * Obtiene el género favorito del usuario y realiza una búsqueda
 * en Google Books API para mostrarle recomendaciones.
 * </p>
 */
public class RecomendationsController implements Initializable {

    /** Logo que aparece en el menú lateral */
    @FXML private ImageView drawerLogo;

    /** Contenedor donde se insertan las tarjetas de libro */
    @FXML private VBox resultsContainer;

    /** Google Books API base URL */
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        String genero = SessionManager.getUsuarioActual().getGeneroFavorito();
        buscarRecomendaciones(genero);
    }

    /**
     * Descarga y asigna la imagen del logo al {@link #drawerLogo}.
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
     * Realiza la llamada a Google Books API buscando libros por género
     * y actualiza la UI con los resultados.
     *
     * @param genero El género favorito del usuario
     */
    private void buscarRecomendaciones(String genero) {
        resultsContainer.getChildren().clear();
        String consulta = "subject:" + genero;
        String urlStr = GOOGLE_BOOKS_API
            + URI.create(consulta.replace(" ", "+")).toString()
            + "&maxResults=20&langRestrict=es";

        new Thread(() -> {
          try {
            URI uri = new URI(urlStr);
            // Abrimos la conexión a partir del URI
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            }
                JSONObject json = new JSONObject(sb.toString());
                JSONArray items = json.optJSONArray("items");

                Platform.runLater(() -> {
                    if (items != null && items.length() > 0) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject vi = items.getJSONObject(i).getJSONObject("volumeInfo");
                            String title = vi.optString("title", "Sin título");
                            String authors = vi.has("authors")
                                ? String.join(", ", vi.getJSONArray("authors").toList().stream()
                                    .map(Object::toString).toArray(String[]::new))
                                : "Autor desconocido";
                            String publisher = vi.optString("publisher", "Desconocida");
                            String desc = vi.optString("description", "Sin descripción");
                            String imgUrl = vi.has("imageLinks")
                                ? vi.getJSONObject("imageLinks")
                                      .optString("smallThumbnail","")
                                      .replace("http://","https://")
                                : "";

                            VBox card = crearTarjeta(new Libro(title, authors, desc, imgUrl, publisher));
                            resultsContainer.getChildren().add(card);
                        }
                    } else {
                        resultsContainer.getChildren().add(
                            new Label("No se encontraron recomendaciones para tu género.")
                        );
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> resultsContainer.getChildren()
                    .add(new Label("Error al cargar recomendaciones.")));
            }
        }).start();
    }

    /**
     * Construye una tarjeta visual para un libro.
     *
     * @param libro Modelo de libro
     * @return Un VBox estilizado representando el libro
     */
    private VBox crearTarjeta(Libro libro) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card-libro");
        card.setPrefWidth(660);

        ImageView portada = new ImageView();
        portada.setFitWidth(100);
        portada.setPreserveRatio(true);
        if (libro.getImagenUrl() != null && !libro.getImagenUrl().isEmpty()) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(libro.getImagenUrl()))
                    .header("User-Agent","Mozilla/5.0").build();
                HttpResponse<InputStream> resp =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofInputStream());
                portada.setImage(new Image(resp.body()));
            } catch (Exception ignored) { }
        }

        Label t = new Label("Título: " + libro.getTitulo());
        t.getStyleClass().add("libro-title");
        t.setOnMouseClicked(evt -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/Details.fxml")
                );
                AnchorPane pane = loader.load();
                controllers.DetailsController ctrl = loader.getController();
                ctrl.setLibro(libro);
                Stage st = (Stage) drawerLogo.getScene().getWindow();
                st.getScene().setRoot(pane);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Label a = new Label("Autor: " + libro.getAutor());
        a.getStyleClass().add("libro-author");
        Label p = new Label("Editorial: " + libro.getEditorial());
        p.getStyleClass().add("libro-editorial");

        VBox info = new VBox(3, t, a, p);
        HBox content = new HBox(15, portada, info);
        content.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(content);
        return card;
    }


    @FXML private void handleShowBuscar()          {
      cambiarPantalla("/views/HomePane.fxml");
      }
    
    @FXML private void handleShowRecomendaciones() {
      cambiarPantalla("/views/Recomendation.fxml");
      }
    
    @FXML private void handleShowBiblioteca()      {
      cambiarPantalla("/views/UserLibrary.fxml");
      }
    
    @FXML private void handleShowPerfil()          {
      changingToProfile();
      }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        cambiarPantalla("/views/LoginDataPane.fxml");
    }

    /** Helper para navegación a perfil (usamos fx:id distinto) */
    private void changingToProfile() {
        cambiarPantalla("/views/Profile.fxml");
    }

    /**
     * Carga la nueva vista dentro del mismo Stage.
     *
     * @param ruta ruta al FXML objetivo
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
