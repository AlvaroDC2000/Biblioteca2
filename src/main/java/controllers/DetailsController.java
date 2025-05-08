package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.Libro;
import dao.LibroDAO;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;

public class DetailsController {

    @FXML
    private VBox detailsContainer;

    @FXML
    private ImageView drawerLogo;

    @FXML
    private ImageView bookImage;

    @FXML
    private Label titleLabel;

    @FXML
    private Label authorLabel;

    @FXML
    private Label descriptionLabel;

    private Libro libro;

    public void setLibro(Libro libro) {
        this.libro = libro;

        titleLabel.setText(libro.getTitulo());
        authorLabel.setText("Autor: " + libro.getAutor());
        descriptionLabel.setText(libro.getDescripcion());

        try {
            if (libro.getImagenUrl() != null && !libro.getImagenUrl().isEmpty()) {
                URL url = URI.create(libro.getImagenUrl()).toURL();
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

    @FXML
    public void initialize() {
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

    @FXML
    private void handleSaveToLibrary() {
        if (libro != null) {
            LibroDAO.guardarLibro(libro);
        }
    }

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
            Stage stage = (Stage) drawerLogo.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(loginPane));
            stage.setTitle("Biblioteca");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
