package utils;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ApiUtils {

    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=";

    public static String buscarLibros(String query) {
        StringBuilder response = new StringBuilder();
        try {
          String urlStr = GOOGLE_BOOKS_API + query.replace(" ", "+");
          URI uri = new URI(urlStr);
          HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
          conn.setRequestMethod("GET");


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
