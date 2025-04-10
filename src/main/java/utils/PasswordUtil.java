package utils;

import models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static Usuario loggedUser;

    // Hash de la contraseña
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Verificar la contraseña
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public static Usuario getLoggedUser() {
        return loggedUser;
    }

    public static void setLoggedUser(Usuario usuario) {
        PasswordUtil.loggedUser = usuario;
    }
}
