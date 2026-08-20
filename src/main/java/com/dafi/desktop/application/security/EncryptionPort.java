package com.dafi.desktop.application.security;

/**
 * Puerto de salida para operaciones de cifrado/descifrado.
 */
public interface EncryptionPort {

    /**
     * Cifra un texto plano.
     *
     * @param plaintext texto a cifrar
     * @param key       clave de cifrado en formato Base64
     * @return texto cifrado en formato Base64(iv):Base64(ciphertext+tag)
     */
    String encrypt(String plaintext, String key);

    /**
     * Descifra un texto cifrado.
     *
     * @param ciphertext texto cifrado en formato Base64(iv):Base64(ciphertext+tag)
     * @param key        clave de cifrado en formato Base64
     * @return texto descifrado
     */
    String decrypt(String ciphertext, String key);
}
