package com.dafi.desktop.adapters.outbound.security;

import com.dafi.desktop.application.security.KeyStoragePort;
import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;

/**
 * Outbound adapter implementing {@link KeyStoragePort}; it stores the
 * Base64-encoded AES data-encryption key in the operating system credential
 * vault instead of a plain file:
 * <ul>
 *   <li>Windows: Credential Manager (DPAPI-backed)</li>
 *   <li>Linux: Secret Service API (GNOME Keyring) or KWallet</li>
 *   <li>macOS: Keychain</li>
 * </ul>
 * The constructor throws {@link BackendNotSupportedException} when no system
 * backend is available, allowing the composition root to fall back to
 * file-based storage.
 */
public class OsKeyringKeyStorageAdapter implements KeyStoragePort, AutoCloseable {

    private static final String SERVICE = "dafi-desktop";
    private static final String ACCOUNT = "data-encryption-key";

    private final Keyring keyring;

    /**
     * Opens a connection to the operating system keyring.
     *
     * @throws BackendNotSupportedException if the current OS provides no
     *                                       supported credential backend
     */
    public OsKeyringKeyStorageAdapter() throws BackendNotSupportedException {
        this.keyring = Keyring.create();
    }

    /**
     * Reads the encryption key from the system keyring.
     *
     * @return the Base64-encoded key, or null when no key is stored yet
     * @throws RuntimeException if the keyring cannot be accessed
     */
    @Override
    public String getEncryptionKey() {
        try {
            return keyring.getPassword(SERVICE, ACCOUNT);
        } catch (PasswordAccessException e) {
            return null;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al acceder al keyring del sistema", e);
        }
    }

    /**
     * Stores or replaces the encryption key in the system keyring.
     *
     * @param key Base64-encoded key to persist
     * @throws RuntimeException if the keyring rejects the write
     */
    @Override
    public void storeEncryptionKey(String key) {
        try {
            keyring.setPassword(SERVICE, ACCOUNT, key);
        } catch (PasswordAccessException e) {
            throw new RuntimeException("Error al almacenar la clave en el keyring del sistema", e);
        }
    }

    /**
     * Indicates whether an encryption key is stored in the system keyring.
     *
     * @return true if the keyring contains an entry for this application
     */
    @Override
    public boolean hasStoredKey() {
        return getEncryptionKey() != null;
    }

    /**
     * Releases the keyring connection and its background threads. Must be
     * called on application shutdown, otherwise the non-daemon DBus threads
     * keep the JVM alive after the last window closes.
     */
    @Override
    public void close() {
        try {
            keyring.close();
        } catch (Exception e) {
            // best-effort release during shutdown; nothing sensible to do
        }
    }
}
