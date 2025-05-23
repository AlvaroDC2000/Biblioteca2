package dao;

import models.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

/**
 * Operaciones CRUD básicas para Usuario.
 */
public class UsuarioDAO {

    /**
     * Actualiza los datos de un usuario existente.
     * @param usuario el usuario modificado
     */
    public static void updateUsuario(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(usuario);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Elimina un usuario de la base de datos.
     * <ol>
     *   <li>Primero borra todos los libros asociados a ese usuario.</li>
     *   <li>Luego elimina la entidad Usuario.</li>
     * </ol>
     * @param usuario el usuario a eliminar
     */
    public static void deleteUsuario(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // 1) Eliminar todos los libros que refieran a este usuario usando MutationQuery
            org.hibernate.query.MutationQuery deleteLibros = session.createMutationQuery(
                "delete from Libro l where l.usuario = :u"
            );
            deleteLibros.setParameter("u", usuario);
            deleteLibros.executeUpdate();

            // 2) Eliminar el propio usuario
            session.remove(
                session.contains(usuario)
                    ? usuario
                    : session.merge(usuario)
            );

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

}
