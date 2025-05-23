package utils;

import models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para el manejo de contraseñas y del usuario autenticado.
 */
public class PasswordUtil {

    /** Usuario actualmente autenticado en la aplicación. */
    private static Usuario loggedUser;

    /**
     * Genera un hash seguro a partir de una contraseña en texto plano.
     *
     * @param plainPassword contraseña en texto plano
     * @return hash generado mediante BCrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifica si una contraseña en texto plano coincide con el hash almacenado.
     *
     * @param plainPassword   contraseña en texto plano proporcionada
     * @param hashedPassword  hash almacenado con el que comparar
     * @return {@code true} si la contraseña coincide con el hash, {@code false} en caso contrario
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Devuelve el usuario actualmente autenticado.
     *
     * @return instancia de {@link Usuario} logueado, o {@code null} si no hay ninguno
     */
    public static Usuario getLoggedUser() {
        return loggedUser;
    }

    /**
     * Define el usuario actualmente autenticado en la aplicación.
     *
     * @param usuario instancia de {@link Usuario} que se marca como logueado
     */
    public static void setLoggedUser(Usuario usuario) {
        PasswordUtil.loggedUser = usuario;
    }
}
