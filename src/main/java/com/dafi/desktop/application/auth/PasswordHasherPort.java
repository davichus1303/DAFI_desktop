package com.dafi.desktop.application.auth;

/**
 * Puerto de salida para el hashing de contraseñas.
 */
public interface PasswordHasherPort {

    /**
     * Genera un hash seguro de la contraseña.
     *
     * @param password contraseña en texto plano
     * @return objeto HashResult con el hash y el salt
     */
    HashResult hash(String password);

    /**
     * Verifica si la contraseña coincide con el hash almacenado.
     *
     * @param password contraseña en texto plano
     * @param hash     hash almacenado
     * @param salt     salt utilizado
     * @return true si la contraseña es válida
     */
    boolean verify(String password, String hash, String salt);

    /**
     * Resultado de una operación de hashing.
     */
    record HashResult(String hash, String salt) {}
}
