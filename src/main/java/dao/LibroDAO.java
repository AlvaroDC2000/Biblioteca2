package dao;

import models.Libro;
import utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class LibroDAO {

    public static void guardarLibro(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            if (libro.getId() == 0) {
                session.persist(libro); // Nuevo libro
            } else {
                session.merge(libro);   // Actualización
            }

            tx.commit();
            System.out.println(" Libro guardado correctamente en la base de datos.");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println(" Error al guardar el libro:");
            e.printStackTrace();
        }
    }

    public static List<Libro> obtenerTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Libro", Libro.class).list();
        }
    }

    public static void eliminarLibro(Libro libro) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.merge(libro));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println(" Error al eliminar el libro:");
            e.printStackTrace();
        }
    }
}

