package com.dafi.desktop.application.security;

/**
 * Output port for secure encryption key storage.
 * Abstracts the underlying mechanism (OS keyring, secure file, etc.).
 */
public interface KeyStoragePort {

    /**
     * Retrieves the stored encryption key.
     *
     * @return Base64-encoded key, or {@code null} if none exists
     */
    String getEncryptionKey();

    /**
     * Persists the encryption key.
     *
     * @param key Base64-encoded key
     */
    void storeEncryptionKey(String key);

    /**
     * Checks whether an encryption key is already stored.
     *
     * @return {@code true} if a key exists
     */
    boolean hasStoredKey();
}
