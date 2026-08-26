package com.dafi.desktop.application.security;

/**
 * Output port for encryption/decryption operations, implemented by outbound adapters.
 */
public interface EncryptionPort {

    /**
     * Encrypts a plaintext string.
     *
     * @param plaintext text to encrypt
     * @param key       Base64-encoded encryption key
     * @return ciphertext in the format {@code Base64(iv):Base64(ciphertext+tag)}
     */
    String encrypt(String plaintext, String key);

    /**
     * Decrypts a ciphertext string.
     *
     * @param ciphertext ciphertext in the format {@code Base64(iv):Base64(ciphertext+tag)}
     * @param key        Base64-encoded encryption key
     * @return decrypted plaintext
     */
    String decrypt(String ciphertext, String key);
}
