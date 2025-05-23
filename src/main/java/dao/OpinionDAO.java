package dao;

import models.Libro;
import models.Opinion;
import models.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

/**
 * DAO para la entidad {@link Opinion}.
 * <p>
 * Proporciona métodos estáticos para realizar operaciones CRUD
 * relacionadas con opiniones de libros en la base de datos,
 * utilizando Hibernate como framework de persistencia.
 * </p>
 */
public class OpinionDAO {

    /**
     * Recupera todas las opiniones asociadas a un libro determinado.
     * <p>
     * Se asegura de que la instancia de {@link Libro} esté gestionada por
     * Hibernate antes de ejecutar la consulta HQL que filtra por la entidad.
     * </p>
     *
     * @param libro instancia de {@link Libro} cuyas opiniones se desean obtener
     * @return lista de {@link Opinion} vinculadas al libro; lista vacía si no hay coincidencias o en caso de error
     */
    public static List<Opinion> obtenerOpinionesPorLibro(Libro libro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Libro libroPersistido = session.get(Libro.class, libro.getId());
            return session.createQuery(
                    "FROM Opinion WHERE libro = :libro",
                    Opinion.class
                )
                .setParameter("libro", libroPersistido)
                .list();
        } catch (Exception e) {
            System.err.println("Error al obtener opiniones del libro:");
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Persiste una nueva opinión en la base de datos.
     * <p>
     * Antes de guardar, se asegura de que la referencia al {@link Libro}
     * esté gestionada por la sesión de Hibernate.
     * </p>
     *
     * @param opinion instancia de {@link Opinion} a persistir
     */
    public static void guardarOpinion(Opinion opinion) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Libro libroAdjunto = session.get(Libro.class, opinion.getLibro().getId());
            opinion.setLibro(libroAdjunto);

            session.persist(opinion);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error al guardar la opinión:");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la opinión que un usuario ha realizado sobre un libro específico.
     * <p>
     * Útil para evitar duplicar múltiples opiniones de un mismo usuario
     * sobre el mismo libro.
     * </p>
     *
     * @param libro   instancia de {@link Libro} objeto de la opinión
     * @param usuario instancia de {@link Usuario} autor de la opinión
     * @return la {@link Opinion} existente, o {@code null} si no se encontró ninguna
     */
    public static Opinion obtenerOpinionPorLibroYUsuario(Libro libro, Usuario usuario) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Libro libroAdjunto = session.get(Libro.class, libro.getId());
            Usuario usuarioAdjunto = session.get(Usuario.class, usuario.getId());
            return session.createQuery(
                    "FROM Opinion WHERE libro = :libro AND usuario = :usuario",
                    Opinion.class
                )
                .setParameter("libro", libroAdjunto)
                .setParameter("usuario", usuarioAdjunto)
                .uniqueResult();
        }
    }

    /**
     * Actualiza una opinión existente en la base de datos.
     * <p>
     * Utiliza {@code merge} de Hibernate para sincronizar los cambios
     * de la instancia proporcionada con el registro persistido.
     * </p>
     *
     * @param opinion instancia de {@link Opinion} con los cambios a aplicar
     */
    public static void actualizarOpinion(Opinion opinion) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(opinion);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error al actualizar la opinión:");
            e.printStackTrace();
        }
    }

    /**
     * Recupera opiniones filtrando por el título y el autor del libro.
     * <p>
     * Ejecuta una consulta HQL que comprueba los campos {@code libro.titulo}
     * y {@code libro.autor} para obtener las opiniones correspondientes.
     * </p>
     *
     * @param titulo título del libro cuyas opiniones se desean obtener
     * @param autor  autor del libro cuyas opiniones se desean obtener
     * @return lista de {@link Opinion} que coinciden con el título y autor; lista vacía si no hay coincidencias
     */
    public static List<Opinion> obtenerOpinionesPorTituloYAutor(String titulo, String autor) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Opinion WHERE libro.titulo = :titulo AND libro.autor = :autor",
                    Opinion.class
                )
                .setParameter("titulo", titulo)
                .setParameter("autor", autor)
                .list();
        } catch (Exception e) {
            System.err.println("Error al obtener opiniones por título y autor:");
            e.printStackTrace();
            return List.of();
        }
    }

}
