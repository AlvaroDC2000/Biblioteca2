package controllers;

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
 * Obtiene el género favorito del usuario de sesión, consulta la API de Google Books
 * para buscar libros por ese género, y muestra las recomendaciones en forma de tarjetas.
 * También gestiona la carga del logo y la navegación lateral.
 * </p>
 */
public class RecomendationsController implements Initializable {

    /** Logo que aparece en el menú lateral. */
    @FXML private ImageView drawerLogo;

    /** Contenedor VBox donde se insertan las tarjetas de recomendación. */
    @FXML private VBox resultsContainer;

    /** URL base de la API de Google Books para búsquedas por género. */
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=";

    /**
     * Inicializa el controlador tras cargar el FXML.
     * <ol>
     *   <li>Carga el logo de la aplicación.</li>
     *   <li>Recupera el género favorito del usuario actual.</li>
     *   <li>Lanza la búsqueda de recomendaciones para ese género.</li>
     * </ol>
     *
     * @param location  ubicación del recurso FXML (no utilizado)
     * @param resources bundle de recursos (no utilizado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarLogo();
        String genero = SessionManager.getUsuarioActual().getGeneroFavorito();
        buscarRecomendaciones(genero);
    }

    /**
     * Descarga y asigna la imagen del logo al ImageView {@link #drawerLogo}.
     * <p>
     * Añade cabecera User-Agent para evitar bloqueos por parte del servidor.
     * </p>
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
            // Imprimir traza si falla la carga del logo
            e.printStackTrace();
        }
    }

    /**
     * Realiza la consulta a la API de Google Books para recomendaciones basadas en género,
     * parsea la respuesta JSON y actualiza la UI con las tarjetas de resultado.
     *
     * @param genero género literario favorito del usuario
     */
    private void buscarRecomendaciones(String genero) {
        // Limpia resultados previos
        resultsContainer.getChildren().clear();

        // Construye la consulta con el parámetro “subject:”
        String consulta = "subject:" + genero;
        String urlStr = GOOGLE_BOOKS_API
            + URI.create(consulta.replace(" ", "+")).toString()
            + "&maxResults=20&langRestrict=es";

        // Ejecuta la petición en un hilo aparte para no bloquear la UI
        new Thread(() -> {
            try {
                URI uri = new URI(urlStr);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("GET");

                // Lee toda la respuesta en un StringBuilder
                StringBuilder sb = new StringBuilder();
                try (BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                }

                // Parseo del JSON y obtención del array “items”
                JSONObject json = new JSONObject(sb.toString());
                JSONArray items = json.optJSONArray("items");

                // Actualiza la interfaz en el hilo de JavaFX
                Platform.runLater(() -> {
                    if (items != null && items.length() > 0) {
                        // Crea y añade una tarjeta por cada elemento
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject vi = items.getJSONObject(i).getJSONObject("volumeInfo");

                            String title = vi.optString("title", "Sin título");
                            String authors = vi.has("authors")
                                ? String.join(", ",
                                    vi.getJSONArray("authors")
                                      .toList().stream()
                                      .map(Object::toString)
                                      .toArray(String[]::new))
                                : "Autor desconocido";
                            String publisher = vi.optString("publisher", "Desconocida");
                            String desc = vi.optString("description", "Sin descripción");
                            String imgUrl = vi.has("imageLinks")
                                ? vi.getJSONObject("imageLinks")
                                      .optString("smallThumbnail","")
                                      .replace("http://","https://")
                                : "";

                            // Construye la tarjeta para el libro y la añade
                            VBox card = crearTarjeta(new Libro(title, authors, desc, imgUrl, publisher));
                            resultsContainer.getChildren().add(card);
                        }
                    } else {
                        // Mensaje si no hay recomendaciones
                        resultsContainer.getChildren().add(
                            new Label("No se encontraron recomendaciones para tu género.")
                        );
                    }
                });
            } catch (Exception e) {
                // En caso de error, mostrar mensaje en la UI
                e.printStackTrace();
                Platform.runLater(() ->
                    resultsContainer.getChildren().add(
                        new Label("Error al cargar recomendaciones.")
                    )
                );
            }
        }).start();
    }

    /**
     * Crea una tarjeta visual (VBox) para un libro dado.
     * <p>
     * Incluye portada, título, autor y editorial. Permite hacer clic
     * en el título para navegar a la vista de detalles.
     * </p>
     *
     * @param libro instancia de {@link Libro} con datos a mostrar
     * @return VBox estilizado representando el libro
     */
    private VBox crearTarjeta(Libro libro) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card-libro");
        card.setPrefWidth(660);

        // ImageView para la portada
        ImageView portada = new ImageView();
        portada.setFitWidth(100);
        portada.setPreserveRatio(true);
        if (libro.getImagenUrl() != null && !libro.getImagenUrl().isEmpty()) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(libro.getImagenUrl()))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();
                HttpResponse<InputStream> resp = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofInputStream());
                portada.setImage(new Image(resp.body()));
            } catch (Exception ignored) {
                // Ignora fallos al cargar la imagen
            }
        }

        // Etiqueta de título con evento de clic para detalles
        Label t = new Label("Título: " + libro.getTitulo());
        t.getStyleClass().add("libro-title");
        t.setOnMouseClicked(evt -> mostrarDetalles(libro));

        // Autor y editorial
        Label a = new Label("Autor: " + libro.getAutor());
        a.getStyleClass().add("libro-author");
        Label p = new Label("Editorial: " + libro.getEditorial());
        p.getStyleClass().add("libro-editorial");

        // Organiza portada y datos en un HBox
        VBox info = new VBox(3, t, a, p);
        HBox content = new HBox(15, portada, info);
        content.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(content);

        return card;
    }

    /**
     * Navega a la vista de detalles para el libro seleccionado.
     *
     * @param libro libro cuya vista de detalle se debe mostrar
     */
    private void mostrarDetalles(Libro libro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DetailsPane.fxml"));
            AnchorPane pane = loader.load();
            controllers.DetailsController ctrl = loader.getController();
            ctrl.setLibro(libro);

            Stage st = (Stage) drawerLogo.getScene().getWindow();
            st.getScene().setRoot(pane);
        } catch (Exception e) {
            // Imprime traza si falla la navegación
            e.printStackTrace();
        }
    }

    /** Navega a la vista de búsqueda de libros. */
    @FXML private void handleShowBuscar() {
        cambiarPantalla("/views/HomePane.fxml");
    }

    /** Navega a la vista de recomendaciones (esta misma pantalla). */
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
     * Cierra la sesión actual y vuelve a la pantalla de login.
     * <p>
     * Carga el FXML de login y lo establece como la escena del Stage.
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
     * Método helper para cambiar la raíz de la escena actual sin recargar el Stage.
     *
     * @param ruta ruta relativa al recurso FXML dentro de /views
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
