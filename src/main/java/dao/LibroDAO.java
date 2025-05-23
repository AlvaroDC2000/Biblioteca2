package dao;

import models.Libro;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * DAO para la entidad {@link Libro}.
 * <p>
 * Proporciona métodos estáticos para operaciones CRUD sobre la tabla de libros
 * utilizando Hibernate como framework de persistencia.
 * </p>
 */
public class LibroDAO {

    /**
     * Guarda o actualiza un libro en la base de datos.
     * <p>
     * Si el libro no tiene ID (valor 0), se persiste como nuevo registro.
     * En caso contrario, se hace merge para actualizar el registro existente.
     * </p>
     *
     * @param libro instancia de {@link Libro} a guardar o actualizar
     */
    public static void guardarLibro(Libro libro) {
        Transaction tx = null;
        // Abrir sesión y comenzar transacción
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            if (libro.getId() == 0) {
                session.persist(libro); // Inserta un nuevo libro
            } else {
                session.merge(libro);   // Actualiza un libro existente
            }

            tx.commit();
            System.out.println("Libro guardado correctamente en la base de datos.");
        } catch (Exception e) {
            // Si hay error, deshacer la transacción
            if (tx != null) tx.rollback();
            System.err.println("Error al guardar el libro:");
            e.printStackTrace();
        }
    }

    /**
     * Recupera todos los libros almacenados en la base de datos.
     *
     * @return lista de todos los {@link Libro} existentes
     */
    public static List<Libro> obtenerTodos() {
        // Sesión en try-with-resources para autocierre
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Ejecuta HQL para obtener todos los libros
            return session.createQuery("FROM Libro", Libro.class).list();
        }
    }

    /**
     * Elimina un libro de la base de datos.
     * <p>
     * Realiza merge del objeto por si no está en el contexto de persistencia
     * y luego lo remueve.
     * </p>
     *
     * @param libro instancia de {@link Libro} a eliminar
     */
    public static void eliminarLibro(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            // Merge para asegurar entidad gestionada, luego remove
            session.remove(session.merge(libro));
            tx.commit();
            System.out.println("Libro eliminado correctamente de la base de datos.");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error al eliminar el libro:");
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene todos los libros asociados a un usuario dado.
     *
     * @param usuario instancia de {@link models.Usuario} propietario de los libros
     * @return lista de {@link Libro} correspondientes al usuario
     */
    public static List<Libro> obtenerPorUsuario(models.Usuario usuario) {
        // Abrir sesión para recuperar usuario y sus libros
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Asegurar que el usuario esté en el contexto de Hibernate
            models.Usuario usuarioAdjunto = session.get(models.Usuario.class, usuario.getId());

            // Consulta HQL con parámetro para filtrar por usuario
            return session.createQuery(
                    "FROM Libro WHERE usuario = :usuario",
                    Libro.class
                )
                .setParameter("usuario", usuarioAdjunto)
                .list();
        }
    }

}
