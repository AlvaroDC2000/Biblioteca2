package dao;

import models.Libro;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class LibroDAO {

    /**
     * Guarda un libro en la base de datos.
     * Si el libro ya tiene ID, se actualiza; si no, se inserta uno nuevo.
     */
    public static void guardarLibro(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            if (libro.getId() == 0) {
                session.persist(libro); // Guardar nuevo libro
            } else {
                session.merge(libro);   // Actualizar libro existente
            }

            tx.commit();
            System.out.println(" Libro guardado correctamente en la base de datos.");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println(" Error al guardar el libro:");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene todos los libros de la base de datos.
     */
    public static List<Libro> obtenerTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Libro", Libro.class).list();
        }
    }

    /**
     * Elimina un libro de la base de datos.
     */
    public static void eliminarLibro(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.merge(libro)); // merge por si el objeto no está sincronizado
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println(" Error al eliminar el libro:");
            e.printStackTrace();
        }
    }
    
    public static List<Libro> obtenerPorUsuario(models.Usuario usuario) {
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
          models.Usuario usuarioAdjunto = session.get(models.Usuario.class, usuario.getId());

          return session.createQuery("FROM Libro WHERE usuario = :usuario", Libro.class)
                       .setParameter("usuario", usuarioAdjunto)
                       .list();
      }
  }

}
