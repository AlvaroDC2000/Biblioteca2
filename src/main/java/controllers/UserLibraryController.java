package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Libro;
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
        cargarLogo();
        mostrarLibrosGuardados("");
    }

    private void cargarLogo() {
        try {
            URL url = new URI("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Calibre_logo_3.png/640px-Calibre_logo_3.png").toURL();
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
        mostrarLibrosGuardados(query);
    }

    private void mostrarLibrosGuardados(String filtro) {
        libraryContainer.getChildren().clear();

        for (Libro libro : dao.LibroDAO.obtenerTodos()) {
            boolean coincide = filtro.isEmpty() ||
                    libro.getTitulo().toLowerCase().contains(filtro) ||
                    libro.getAutor().toLowerCase().contains(filtro) ||
                    (libro.getEditorial() != null && libro.getEditorial().toLowerCase().contains(filtro));

            if (!coincide) continue;

            VBox card = new VBox(10);
            card.getStyleClass().add("card-libro");
            card.setPrefWidth(660);

            Label title = new Label("Título: " + libro.getTitulo());
            title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label author = new Label("Autor: " + libro.getAutor());
            author.setStyle("-fx-text-fill: #e0e0e0;");

            Label editorial = new Label("Editorial: " + (libro.getEditorial() != null ? libro.getEditorial() : "Desconocida"));
            editorial.setStyle("-fx-text-fill: #b0bec5;");

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

            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_LEFT);

            Button likeButton = new Button("❤ Me gusta");
            likeButton.getStyleClass().add("button-like");
            likeButton.setOnAction(e -> {
                System.out.println(" Libro marcado como favorito: " + libro.getTitulo());
            });

            Button removeButton = new Button("Eliminar");
            removeButton.getStyleClass().add("button-remove");
            removeButton.setOnAction(e -> {
                dao.LibroDAO.eliminarLibro(libro);
                libraryContainer.getChildren().remove(card);
            });

            actions.getChildren().addAll(likeButton, removeButton);

            VBox detailsBox = new VBox(5, title, author, editorial, actions);
            HBox content = new HBox(15, portada, detailsBox);
            card.getChildren().add(content);
            libraryContainer.getChildren().add(card);
        }

        if (libraryContainer.getChildren().isEmpty()) {
            Label noResults = new Label("No se encontraron resultados.");
            noResults.setStyle("-fx-text-fill: white;");
            libraryContainer.getChildren().add(noResults);
        }
    }

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

    private void cambiarPantalla(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            AnchorPane root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) drawerLogo.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

