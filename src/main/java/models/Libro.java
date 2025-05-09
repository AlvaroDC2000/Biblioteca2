package models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String titulo;
    private String autor;
    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    private String editorial;

    // Nuevos campos
    private String estado; // colección, leído, pendiente, prestado, deseado

    @Column(name = "fecha_lectura")
    private LocalDate fechaLectura;

    private Integer nota;

    private String comentario;

    @Column(name = "prestado_a")
    private String prestadoA;

    @Column(name = "fecha_prestamo")
    private LocalDate fechaPrestamo;

    private Boolean devuelto;

    // Constructor vacío
    public Libro() {}

    // Constructor completo actualizado
    public Libro(String titulo, String autor, String descripcion, String imagenUrl, String editorial) {
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.editorial = editorial;
        this.estado = "colección";
        this.devuelto = true;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(LocalDate fechaLectura) { this.fechaLectura = fechaLectura; }

    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getPrestadoA() { return prestadoA; }
    public void setPrestadoA(String prestadoA) { this.prestadoA = prestadoA; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public Boolean getDevuelto() { return devuelto; }
    public void setDevuelto(Boolean devuelto) { this.devuelto = devuelto; }

    @Override
    public String toString() {
        return "Libro{" +
                "titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                ", editorial='" + editorial + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaLectura=" + fechaLectura +
                ", nota=" + nota +
                ", comentario='" + comentario + '\'' +
                ", prestadoA='" + prestadoA + '\'' +
                ", fechaPrestamo=" + fechaPrestamo +
                ", devuelto=" + devuelto +
                '}';
    }
}
