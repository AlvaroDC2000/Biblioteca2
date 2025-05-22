package utils;

import models.Usuario;

/**
 * Manejador de sesión del usuario en la aplicación.
 * Permite almacenar, recuperar y limpiar la información del usuario actualmente autenticado.
 */
public class SessionManager {

    /** Usuario actualmente autenticado. */
    private static Usuario usuarioActual;

    /**
     * Establece el usuario como el usuario actualmente autenticado en la sesión.
     *
     * @param usuario el {@link Usuario} que acaba de autenticarse
     */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Devuelve el usuario actualmente autenticado en la sesión.
     *
     * @return el {@link Usuario} actual, o {@code null} si no hay ninguno
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Cierra la sesión del usuario actual.
     * Elimina cualquier referencia al usuario actualmente autenticado.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
    }

    /**
     * Alias de {@link #cerrarSesion()}.
     * Limpia todos los datos de sesión (por si en el futuro se añaden más campos a la sesión).
     */
    public static void clear() {
        cerrarSesion();
    }
}
