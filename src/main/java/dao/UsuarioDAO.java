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
     * @param usuario el usuario a eliminar
     */
    public static void deleteUsuario(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.contains(usuario) ? usuario : session.merge(usuario));
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

}
