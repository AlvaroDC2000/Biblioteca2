package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Libro;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controlador de la vista principal de búsqueda de libros.
 * <p>
 * Gestiona la carga del logo, la captura de la consulta de búsqueda,
 * la ejecución de peticiones a la API de Google Books, el parseo de
 * resultados JSON y la renderización de tarjetas de libro en la UI.
 * También proporciona navegación entre las distintas pantallas.
 * </p>
 */
public class HomePaneController implements Initializable {

    /** Campo de texto donde el usuario ingresa la consulta de búsqueda. */
    @FXML private TextField searchField;

    /** Contenedor VBox donde se añaden las tarjetas de resultados. */
    @FXML private VBox resultsContainer;

    /** Imagen del logo que se muestra en el menú lateral. */
    @FXML private ImageView drawerLogo;

    /**
     * Se ejecuta al inicializar el controlador.
     * <p>
     * Carga el logo desde una URL, y lanza una búsqueda por defecto
     * para mostrar libros populares al inicio.
     * </p>
     *
     * @param location  ubicación del recurso FXML (no usado)
     * @param resources bundle de recursos (no usado)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar logo de la aplicación
        try {
            URL url = new URI(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/"
              + "c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png"
            ).toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream inputStream = connection.getInputStream()) {
                drawerLogo.setImage(new Image(inputStream));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mostrar inicialmente libros por relevancia
        buscarLibros("bestseller&orderBy=relevance");
    }

    /**
     * Manejador del botón de búsqueda.
     * <p>
     * Toma el texto ingresado, lo valida y llama al método de búsqueda.
     * </p>
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            buscarLibros(query);
        }
    }

    /**
     * Ejecuta una búsqueda en Google Books y renderiza los resultados.
     * <p>
     * Construye la URL con parámetros, lanza la petición en un hilo aparte
     * para no bloquear la UI, parsea el JSON y añade tarjetas al VBox.
     * </p>
     *
     * @param consulta cadena de búsqueda (título, autor, etc.)
     */
    private void buscarLibros(String consulta) {
        // Limpia resultados previos
        resultsContainer.getChildren().clear();

        // Monta la URL con parámetros de búsqueda y encoding UTF-8
        String urlStr = "https://www.googleapis.com/books/v1/volumes?q=" +
            URLEncoder.encode(consulta, StandardCharsets.UTF_8) +
            "&maxResults=20&langRestrict=es";

        // Nueva hebra para la petición HTTP
        new Thread(() -> {
            try {
                // Abrir conexión y leer respuesta
                URL url = URI.create(urlStr).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Parsear JSON de la respuesta
                    JSONObject json = new JSONObject(result.toString());
                    JSONArray items = json.optJSONArray("items");

                    // Actualizar UI en hilo de JavaFX
                    Platform.runLater(() -> {
                        if (items != null && items.length() > 0) {
                            // Para cada elemento de items, crear tarjeta
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject volumeInfo = items
                                    .getJSONObject(i)
                                    .getJSONObject("volumeInfo");

                                // Extraer datos básicos
                                String title = volumeInfo.optString("title", "Sin título");
                                String editorial = volumeInfo.optString("publisher", "Desconocida");
                                String descripcion = volumeInfo.optString("description", "Sin descripción.");
                                String imagenUrl = "";
                                if (volumeInfo.has("imageLinks")) {
                                    imagenUrl = volumeInfo
                                        .getJSONObject("imageLinks")
                                        .optString("smallThumbnail", "")
                                        .replace("http://", "https://")
                                        .replace("&edge=curl", "")
                                        .replace("zoom=1", "zoom=2");
                                }

                                // Extraer lista de autores
                                String tempAuthors = "Autor desconocido";
                                if (volumeInfo.has("authors")) {
                                    JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                                    List<String> authorList = new ArrayList<>();
                                    for (int j = 0; j < authorsArray.length(); j++) {
                                        authorList.add(authorsArray.getString(j));
                                    }
                                    tempAuthors = String.join(", ", authorList);
                                }

                                // Variables finales inmutables para la lambda
                                final String authors = tempAuthors;
                                final String finalEditorial = editorial;
                                final String finalDescripcion = descripcion;
                                final String finalImagenUrl = imagenUrl;

                                // Crear tarjeta (VBox) y estilos
                                VBox card = new VBox(5);
                                card.getStyleClass().add("card-libro");
                                card.setPrefWidth(640);

                                // Imagen de portada
                                ImageView portada = new ImageView();
                                portada.setFitWidth(100);
                                portada.setPreserveRatio(true);
                                if (!finalImagenUrl.isEmpty()) {
                                    // Carga de imagen vía HttpClient
                                    try {
                                        URI uri = URI.create(finalImagenUrl);
                                        HttpRequest request = HttpRequest.newBuilder(uri)
                                            .header("User-Agent", "Mozilla/5.0")
                                            .build();
                                        HttpClient client = HttpClient.newHttpClient();
                                        HttpResponse<InputStream> response =
                                            client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                                        portada.setImage(new Image(response.body()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Etiquetas de texto
                                Label titleLabel = new Label("Título: " + title);
                                titleLabel.getStyleClass().add("libro-title");
                                Label authorLabel = new Label("Autor: " + authors);
                                authorLabel.getStyleClass().add("libro-author");
                                Label editorialLabel = new Label("Editorial: " + finalEditorial);
                                editorialLabel.getStyleClass().add("libro-editorial");

                                // Organizar contenido en HBox y VBox
                                VBox infoBox = new VBox(5, titleLabel, authorLabel, editorialLabel);
                                HBox content = new HBox(15, portada, infoBox);
                                card.getChildren().add(content);

                                // Evento al clicar la tarjeta: ir a detalles
                                card.setOnMouseClicked(e -> {
                                    try {
                                        // Crear entidad Libro con datos de búsqueda
                                        Libro libro = new Libro(
                                            title, authors, finalDescripcion,
                                            finalImagenUrl, finalEditorial
                                        );
                                        // Cargar vista de detalles
                                        FXMLLoader loader = new FXMLLoader(
                                            getClass().getResource("/views/DetailsPane.fxml")
                                        );
                                        AnchorPane detailsPane = loader.load();
                                        // Pasar modelo al controlador de detalles
                                        controllers.DetailsController controller =
                                            loader.getController();
                                        controller.setLibro(libro);
                                        // Cambiar root de la escena actual
                                        Stage stage = (Stage) searchField.getScene().getWindow();
                                        stage.getScene().setRoot(detailsPane);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });

                                // Añadir tarjeta al contenedor principal
                                resultsContainer.getChildren().add(card);
                            }
                        } else {
                            // Mostrar mensaje si no hay resultados
                            resultsContainer.getChildren()
                                .add(new Label("No se encontraron resultados."));
                        }
                    });
                }
            } catch (Exception e) {
                // En caso de error de conexión, mostrar mensaje en UI
                Platform.runLater(() -> {
                    resultsContainer.getChildren()
                        .add(new Label("Error al conectar con Google Books."));
                });
                e.printStackTrace();
            }
        }).start();
    }

    /** Navega a la vista de búsqueda (esta misma pantalla). */
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
     * Cierra la sesión actual y vuelve a la pantalla de login.
     * <p>
     * Recarga el FXML de login y lo muestra en el Stage principal.
     * </p>
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/LoginDataPane.fxml")
            );
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
     * Helper para cambiar de pantalla sin recargar el Stage.
     *
     * @param ruta ruta relativa al recurso FXML destino
     */
    private void cambiarPantalla(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(ruta)
            );
            AnchorPane root = loader.load();
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
