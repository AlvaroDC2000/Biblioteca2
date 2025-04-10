package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import utils.ApiUtils;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomePaneController {

    @FXML
    private TextField searchField;

    @FXML
    private VBox resultsContainer;

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        // Limpiar resultados anteriores
        resultsContainer.getChildren().clear();

        new Thread(() -> {
            String jsonResponse = ApiUtils.buscarLibros(query);
            if (jsonResponse != null) {
                Platform.runLater(() -> mostrarResultados(jsonResponse));
            }
        }).start();
    }

    private void mostrarResultados(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray items = json.optJSONArray("items");

            if (items == null || items.isEmpty()) {
                resultsContainer.getChildren().add(new Label("No se encontraron resultados."));
                return;
            }

            for (int i = 0; i < items.length(); i++) {
                JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
                String title = volumeInfo.optString("title", "Sin título");
                String authors = volumeInfo.has("authors") ? volumeInfo.getJSONArray("authors").join(", ").replaceAll("\"", "") : "Autor desconocido";

                Label resultado = new Label("º" + title + " — " + authors);
                resultado.getStyleClass().add("search-result");
                resultsContainer.getChildren().add(resultado);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultsContainer.getChildren().add(new Label("Error al procesar la respuesta."));
        }
    }
}
