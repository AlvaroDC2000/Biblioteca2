package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.net.URL;
import java.net.URI;
import java.net.URLConnection;
import java.io.InputStream;
import java.util.ResourceBundle;

public class UserLibraryController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private VBox libraryContainer;

    @FXML
    private ImageView drawerLogo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar logo
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png";
        try {
            URL url = URI.create(imageUrl).toURL();
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
        String query = searchField.getText().trim().toLowerCase();
        libraryContainer.getChildren().clear();

        // Simulación de resultados
        for (int i = 1; i <= 3; i++) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: #2a4d7a; -fx-padding: 10; -fx-background-radius: 10;");
            card.setPrefWidth(650);

            Label title = new Label("📘 Libro " + i + ": El título del libro");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label author = new Label("Autor: Autor Ejemplo");
            author.setStyle("-fx-text-fill: white;");

            Button removeButton = new Button("Quitar de mi biblioteca");
            removeButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
            removeButton.setOnAction(e -> libraryContainer.getChildren().remove(card));

            card.getChildren().addAll(title, author, removeButton);
            libraryContainer.getChildren().add(card);
        }
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
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Recomendations.fxml"));
        AnchorPane homePane = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
        stage.getScene().setRoot(homePane);
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

    @FXML
    private void handleShowBiblioteca() {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserLibrary.fxml"));
        AnchorPane homePane = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
        stage.getScene().setRoot(homePane);
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

    @FXML
    private void handleShowPerfil() {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Profile.fxml"));
        AnchorPane homePane = loader.load();
        javafx.stage.Stage stage = (javafx.stage.Stage) searchField.getScene().getWindow();
        stage.getScene().setRoot(homePane);
    } catch (Exception e) {
        e.printStackTrace();
    }
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
