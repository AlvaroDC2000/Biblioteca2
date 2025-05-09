package dao;

import models.Libro;
import models.Opinion;
import models.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

public class OpinionDAO {

  /**
   * Devuelve una lista de opiniones asociadas a un libro específico.
   * Este método consulta todas las opiniones existentes en la base de datos 
   * que están vinculadas al libro recibido como parámetro.
   */
  public static List<Opinion> obtenerOpinionesPorLibro(Libro libro) {
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
          // Se asegura que el libro esté gestionado por Hibernate dentro de la sesión
          Libro libroPersistido = session.get(Libro.class, libro.getId());

          // Consulta HQL para obtener todas las opiniones del libro
          return session.createQuery("FROM Opinion WHERE libro = :libro", Opinion.class)
                        .setParameter("libro", libroPersistido)
                        .list();
      } catch (Exception e) {
          System.err.println(" Error al obtener opiniones del libro:");
          e.printStackTrace();
          return List.of(); // Devuelve lista vacía en caso de error
      }
  }

  /**
   * Guarda una nueva opinión en la base de datos.
   * Este método recibe un objeto Opinion y lo persiste dentro de una transacción.
   */
  public static void guardarOpinion(Opinion opinion) {
      Transaction tx = null;
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
          tx = session.beginTransaction();

          // Asegurar que el libro esté gestionado por la sesión
          Libro libroAdjunto = session.get(Libro.class, opinion.getLibro().getId());
          opinion.setLibro(libroAdjunto); // Se asigna el libro gestionado al objeto Opinion

          // Se guarda la nueva opinión
          session.persist(opinion);
          tx.commit();
      } catch (Exception e) {
          if (tx != null) tx.rollback();
          System.err.println(" Error al guardar la opinión:");
          e.printStackTrace();
      }
  }

  /**
   * Devuelve una opinión específica realizada por un usuario sobre un libro concreto.
   * Esto se utiliza para evitar duplicar opiniones de un mismo usuario sobre un mismo libro.
   */
  public static Opinion obtenerOpinionPorLibroYUsuario(Libro libro, Usuario usuario) {
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
          // Adjuntar las entidades a la sesión actual
          Libro libroAdjunto = session.get(Libro.class, libro.getId());
          Usuario usuarioAdjunto = session.get(Usuario.class, usuario.getId());

          // Buscar si ya existe una opinión con esa combinación
          return session.createQuery("FROM Opinion WHERE libro = :libro AND usuario = :usuario", Opinion.class)
                        .setParameter("libro", libroAdjunto)
                        .setParameter("usuario", usuarioAdjunto)
                        .uniqueResult();
      }
  }

  /**
   * Actualiza una opinión existente en la base de datos.
   * Utiliza el método merge de Hibernate para aplicar los cambios a una opinión previamente guardada.
   */
  public static void actualizarOpinion(Opinion opinion) {
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
          Transaction tx = session.beginTransaction();
          session.merge(opinion); // Actualiza la entidad
          tx.commit();
      }
  }
  
  public static List<Opinion> obtenerOpinionesPorTituloYAutor(String titulo, String autor) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        return session.createQuery("FROM Opinion WHERE libro.titulo = :titulo AND libro.autor = :autor", Opinion.class)
                     .setParameter("titulo", titulo)
                     .setParameter("autor", autor)
                     .list();
    }
  }


}
