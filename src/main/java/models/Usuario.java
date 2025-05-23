package models;

import jakarta.persistence.*;

/**
 * Representa un usuario dentro del sistema Biblioteca.
 * <p>
 * Entidad JPA mapeada a la tabla {@code usuarios}, que almacena
 * las credenciales de acceso y la preferencia de género literario
 * de cada usuario registrado.
 * </p>
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    /** Identificador único del usuario, generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Correo electrónico del usuario; debe ser único y no puede ser nulo. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Contraseña cifrada del usuario; no puede ser nula. */
    @Column(nullable = false)
    private String password;

    /** Género literario favorito del usuario. */
    @Column(name = "genero_favorito")
    private String generoFavorito;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Usuario() {}

    /**
     * @return el ID único asignado al usuario
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id establece el ID del usuario
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return el correo electrónico del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email asigna el correo electrónico del usuario
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return la contraseña cifrada del usuario
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password establece la contraseña cifrada del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return el género literario favorito del usuario
     */
    public String getGeneroFavorito() {
        return generoFavorito;
    }

    /**
     * @param generoFavorito fija el género literario favorito del usuario
     */
    public void setGeneroFavorito(String generoFavorito) {
        this.generoFavorito = generoFavorito;
    }
}
