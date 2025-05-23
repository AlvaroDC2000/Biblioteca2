package models;

import jakarta.persistence.*;

/**
 * Representa una opinión de un usuario sobre un libro.
 * <p>
 * Entidad JPA mapeada a la tabla {@code opiniones}. Contiene información
 * sobre el autor de la opinión, contenido textual, valoración numérica,
 * y las relaciones con {@link Usuario} y {@link Libro}.
 * </p>
 */
@Entity
@Table(name = "opiniones")
public class Opinion {

    /** Identificador único generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Usuario que emite la opinión. */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    /** Nombre o pseudónimo del autor de la opinión. */
    private String autor;
    
    /** Texto completo de la opinión o reseña. */
    private String contenido;
    
    /** Valoración numérica asignada al libro (por ejemplo, de 1 a 10). */
    private int nota;

    /** Libro al que se refiere esta opinión. */
    @ManyToOne
    @JoinColumn(name = "libro_id")
    private Libro libro;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Opinion() {}

    /**
     * Constructor para crear una nueva opinión.
     * 
     * @param autor     nombre o alias del autor de la opinión
     * @param contenido texto de la reseña u opinión
     * @param nota      valoración numérica del libro
     * @param libro     instancia de {@link Libro} asociada
     */
    public Opinion(String autor, String contenido, int nota, Libro libro) {
        this.autor = autor;
        this.contenido = contenido;
        this.nota = nota;
        this.libro = libro;
    }

    /**
     * @return el ID único de la opinión
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return el nombre o alias del autor de la opinión
     */
    public String getAutor() {
        return autor;
    }
    
    /**
     * Establece el autor o alias de la opinión.
     * 
     * @param autor nombre o pseudónimo del autor
     */
    public void setAutor(String autor) {
        this.autor = autor;
    }

    /**
     * @return el contenido completo de la opinión
     */
    public String getContenido() {
        return contenido;
    }
    
    /**
     * Establece el texto de la reseña u opinión.
     * 
     * @param contenido texto de la opinión
     */
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    /**
     * @return la valoración numérica asignada al libro
     */
    public int getNota() {
        return nota;
    }
    
    /**
     * Asigna una valoración numérica al libro.
     * 
     * @param nota puntuación del libro (por ejemplo, 1–10)
     */
    public void setNota(int nota) {
        this.nota = nota;
    }

    /**
     * @return la entidad {@link Libro} asociada a esta opinión
     */
    public Libro getLibro() {
        return libro;
    }
    
    /**
     * Establece el libro al que se refiere esta opinión.
     * 
     * @param libro instancia de {@link Libro}
     */
    public void setLibro(Libro libro) {
        this.libro = libro;
    }
    
    /**
     * @return el {@link Usuario} que emitió la opinión
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Asocia un usuario a esta opinión.
     * 
     * @param usuario instancia de {@link Usuario}
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
