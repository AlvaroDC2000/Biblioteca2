package dao;

import models.Libro;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LibroDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void guardarLibro(Libro libro) {
        String sql = "INSERT INTO libros (titulo, autor, descripcion, imagen_url) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setString(3, libro.getDescripcion());
            stmt.setString(4, libro.getImagenUrl());

            stmt.executeUpdate();
            System.out.println(" Libro guardado correctamente en la base de datos.");

        } catch (SQLException e) {
            System.err.println(" Error al guardar el libro:");
            e.printStackTrace();
        }
    }
}
