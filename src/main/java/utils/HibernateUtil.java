package utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utilidad para inicializar y proporcionar acceso al SessionFactory de Hibernate.
 */
public class HibernateUtil {

    /**
     * SessionFactory creado al iniciar la aplicación.
     */
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Construye el SessionFactory usando la configuración por defecto de Hibernate.
     *
     * @return instancia de SessionFactory
     * @throws ExceptionInInitializerError si falla la creación
     */
    private static SessionFactory buildSessionFactory() {
        try {
            // Carga hibernate.cfg.xml y construye el SessionFactory
            return new Configuration()
                    .configure()
                    .buildSessionFactory();
        } catch (Throwable ex) {
            // Imprime el error y propaga la excepción
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Devuelve el SessionFactory compartido.
     *
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Cierra el SessionFactory liberando recursos.
     */
    public static void shutdown() {
        getSessionFactory().close();
    }
}
