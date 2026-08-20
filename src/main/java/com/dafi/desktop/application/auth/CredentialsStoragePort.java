package com.dafi.desktop.application.auth;

/**
 * Puerto de salida para el almacenamiento de credenciales.
 */
public interface CredentialsStoragePort {

    /**
     * Obtiene el hash almacenado del usuario.
     *
     * @param username nombre de usuario
     * @return hash del password o null si no existe
     */
    String getStoredHash(String username);

    /**
     * Obtiene el salt almacenado del usuario.
     *
     * @param username nombre de usuario
     * @return salt en formato Base64 o null si no existe
     */
    String getStoredSalt(String username);

    /**
     * Guarda las credenciales del usuario.
     *
     * @param username    nombre de usuario
     * @param hashedPassword hash del password
     * @param salt        salt en formato Base64
     */
    void storeCredentials(String username, String hashedPassword, String salt);

    /**
     * Verifica si existe un usuario configurado.
     *
     * @return true si existe al menos un usuario
     */
    boolean hasConfiguredUser();
}
