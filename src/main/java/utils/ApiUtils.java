package utils;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Utilidades para interactuar con la API de Google Books.
 */
public class ApiUtils {

    /** URL base de la API de Google Books para búsquedas. */
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=";

    /**
     * Realiza una búsqueda de libros en Google Books.
     *
     * @param query texto de búsqueda (título, autor, ISBN, etc.)
     * @return respuesta JSON de la API como cadena, o {@code null} en caso de error
     */
    public static String buscarLibros(String query) {
        StringBuilder response = new StringBuilder();
        try {
            // Construye la URL de la solicitud substituyendo espacios por '+'
            String urlStr = GOOGLE_BOOKS_API + query.replace(" ", "+");
            URI uri = new URI(urlStr);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");

            // Lee la respuesta de entrada línea a línea
            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return response.toString();
    }
}
