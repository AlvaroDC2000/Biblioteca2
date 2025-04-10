package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import java.net.URLConnection;
import java.io.InputStream;

import java.net.URL;
import java.util.ResourceBundle;

public class HomePaneController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private VBox resultsContainer;

    @FXML
    private ImageView drawerLogo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Cargar logo
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png";
        try {
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStream inputStream = connection.getInputStream()) {
                Image image = new Image(inputStream);
                drawerLogo.setImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        resultsContainer.getChildren().clear();

        new Thread(() -> {
            String urlStr = "https://www.googleapis.com/books/v1/volumes?q=" + query.replace(" ", "+");
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONArray items = json.optJSONArray("items");

                Platform.runLater(() -> {
                    if (items != null && items.length() > 0) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
                            String title = volumeInfo.optString("title", "Sin título");
                            String authors = "Autor desconocido";
                            if (volumeInfo.has("authors")) {
                                JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                                List<String> authorList = new ArrayList<>();
                                for (int j = 0; j < authorsArray.length(); j++) {
                                    authorList.add(authorsArray.getString(j));
                                }
                                authors = String.join(", ", authorList);
                            }

                            Label resultLabel = new Label("º " + title + " — " + authors);
                            resultLabel.setStyle("-fx-padding: 5;");
                            resultsContainer.getChildren().add(resultLabel);
                        }
                    } else {
                        resultsContainer.getChildren().add(new Label(" No se encontraron resultados."));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultsContainer.getChildren().add(new Label(" Error al conectar con Google Books."));
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleShowBuscar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HomePane.fxml"));
            AnchorPane homePane = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
            stage.getScene().setRoot(homePane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowRecomendaciones() {
    }

    @FXML
    private void handleShowBiblioteca() {
    }

    @FXML
    private void handleShowPerfil() {
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginDataPane.fxml"));
            AnchorPane loginPane = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) drawerLogo.getScene().getWindow();

            javafx.scene.Scene scene = new javafx.scene.Scene(loginPane);
            stage.setScene(scene);
            stage.setTitle("Biblioteca");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
