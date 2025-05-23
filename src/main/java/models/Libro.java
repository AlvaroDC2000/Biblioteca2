package models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Representa un libro dentro de la aplicación Biblioteca.
 * <p>
 * Entidad JPA mapeada a la tabla {@code libros}. Contiene información
 * básica del libro, estado de lectura, datos de préstamo y la relación
 * con las opiniones de los usuarios.
 * </p>
 */
@Entity
@Table(name = "libros")
public class Libro {

    /** Identificador único generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Título del libro. */
    private String titulo;

    /** Autor o autores del libro. */
    private String autor;
    
    /** Descripción o sinopsis del contenido. */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Usuario propietario o creador de este registro de libro. */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** URL de la imagen de portada o ilustración del libro. */
    @Column(name = "imagen_url")
    private String imagenUrl;

    /** Nombre de la editorial que publicó el libro. */
    private String editorial;

    /** Estado actual del libro (e.g., colección, leído, pendiente, prestado, deseado). */
    private String estado;

    /** Fecha en que se terminó de leer el libro (si aplica). */
    @Column(name = "fecha_lectura")
    private LocalDate fechaLectura;

    /** Valoración numérica asignada al libro (1–10). */
    private Integer nota;

    /** Comentario personal o reseña del lector. */
    private String comentario;

    /** Nombre de la persona a quien se prestó el libro. */
    @Column(name = "prestado_a")
    private String prestadoA;

    /** Fecha en que se prestó el libro por última vez. */
    @Column(name = "fecha_prestamo")
    private LocalDate fechaPrestamo;

    /** Indica si el libro prestado ya ha sido devuelto. */
    private Boolean devuelto;

    /** Lista de opiniones asociadas a este libro. */
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Opinion> opiniones;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Libro() {}

    /**
     * Constructor básico para creación de un libro nuevo.
     * <p>
     * Inicializa {@code estado} a "colección" y {@code devuelto} a {@code true}.
     * </p>
     *
     * @param titulo     título del libro
     * @param autor      autor(es) del libro
     * @param descripcion breve sinopsis o descripción
     * @param imagenUrl  URL de la portada o imagen asociada
     * @param editorial  nombre de la editorial
     */
    public Libro(String titulo, String autor, String descripcion, String imagenUrl, String editorial) {
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.editorial = editorial;
        this.estado = "colección";
        this.devuelto = true;
    }

    /** @return el ID único del libro */
    public int getId() {
        return id;
    }

    /** @param id el nuevo ID a asignar */
    public void setId(int id) {
        this.id = id;
    }

    /** @return el título del libro */
    public String getTitulo() {
        return titulo;
    }

    /** @param titulo establece el título del libro */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /** @return el autor o autores del libro */
    public String getAutor() {
        return autor;
    }

    /** @param autor establece el autor del libro */
    public void setAutor(String autor) {
        this.autor = autor;
    }

    /** @return la descripción o sinopsis */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion establece la descripción del libro */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return la URL de la imagen del libro */
    public String getImagenUrl() {
        return imagenUrl;
    }

    /** @param imagenUrl establece la URL de la imagen de portada */
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    /** @return la editorial que publicó el libro */
    public String getEditorial() {
        return editorial;
    }

    /** @param editorial establece la editorial del libro */
    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    /** @return el estado de lectura o colección */
    public String getEstado() {
        return estado;
    }

    /** @param estado establece el estado del libro */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /** @return la fecha en que se leyó el libro */
    public LocalDate getFechaLectura() {
        return fechaLectura;
    }

    /** @param fechaLectura fija la fecha de lectura */
    public void setFechaLectura(LocalDate fechaLectura) {
        this.fechaLectura = fechaLectura;
    }

    /** @return la valoración numérica del libro */
    public Integer getNota() {
        return nota;
    }

    /** @param nota establece la valoración del libro */
    public void setNota(Integer nota) {
        this.nota = nota;
    }

    /** @return el comentario o reseña del lector */
    public String getComentario() {
        return comentario;
    }

    /** @param comentario establece un comentario personal sobre el libro */
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    /** @return la persona a quien se prestó el libro */
    public String getPrestadoA() {
        return prestadoA;
    }

    /** @param prestadoA asigna el nombre del prestatario */
    public void setPrestadoA(String prestadoA) {
        this.prestadoA = prestadoA;
    }

    /** @return la fecha de préstamo del libro */
    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    /** @param fechaPrestamo fija la fecha en que se prestó */
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    /** @return {@code true} si el libro ha sido devuelto; {@code false} en caso contrario */
    public Boolean getDevuelto() {
        return devuelto;
    }

    /** @param devuelto marca si el libro fue devuelto o no */
    public void setDevuelto(Boolean devuelto) {
        this.devuelto = devuelto;
    }

    /** @return lista de opiniones asociadas a este libro */
    public List<Opinion> getOpiniones() {
        return opiniones;
    }

    /** @param opiniones establece la lista de opiniones del libro */
    public void setOpiniones(List<Opinion> opiniones) {
        this.opiniones = opiniones;
    }

    /** @return el usuario propietario de este libro */
    public Usuario getUsuario() {
        return usuario;
    }

    /** @param usuario asigna el propietario del libro */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Representación en texto de la entidad Libro.
     * <p>
     * Incluye los atributos más relevantes, excepto la lista de opiniones.
     * </p>
     *
     * @return cadena con los detalles principales del libro
     */
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
