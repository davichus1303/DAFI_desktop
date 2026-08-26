package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.security.KeyStoragePort;

import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Properties;

/**
 * Outbound adapter implementing {@link KeyStoragePort}; it persists the
 * Base64-encoded AES data-encryption key in a dafi.key properties file under
 * the application configuration directory (typically ~/.dafi/config).
 * Used as fallback when the OS keyring
 * ({@link com.dafi.desktop.adapters.outbound.security.OsKeyringKeyStorageAdapter})
 * is not available.
 */
public class FileKeyStorageAdapter implements KeyStoragePort {

    private static final String KEY_FILE = "dafi.key";
    private final Path configDirectory;

    /**
     * Creates the adapter.
     *
     * @param configDirectory application configuration directory where
     *                        dafi.key is stored
     */
    public FileKeyStorageAdapter(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    /**
     * Reads the stored encryption key.
     *
     * @return the Base64-encoded key, or null if no key has been stored yet
     * @throws RuntimeException if the key file exists but cannot be read
     */
    @Override
    public String getEncryptionKey() {
        Path keyPath = configDirectory.resolve(KEY_FILE);
        if (!Files.exists(keyPath)) {
            return null;
        }

        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(keyPath)) {
                props.load(in);
            }
            return props.getProperty("encryption.key");
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la clave de cifrado", e);
        }
    }

    /**
     * Stores the given encryption key, creating the configuration directory
     * if needed and replacing any previously stored key.
     *
     * @param key Base64-encoded key to persist
     * @throws RuntimeException if the key file cannot be written
     */
    @Override
    public void storeEncryptionKey(String key) {
        try {
            Files.createDirectories(configDirectory);
            Path keyPath = configDirectory.resolve(KEY_FILE);

            Properties props = new Properties();
            props.setProperty("encryption.key", key);

            try (OutputStream out = Files.newOutputStream(keyPath)) {
                props.store(out, "DAFI Encryption Key");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar la clave de cifrado", e);
        }
    }

    /**
     * Indicates whether an encryption key has been stored.
     *
     * @return true if the key file exists
     */
    @Override
    public boolean hasStoredKey() {
        return Files.exists(configDirectory.resolve(KEY_FILE));
    }

    /**
     * Overwrites the key file with random bytes and deletes it, so the
     * plaintext key does not remain on disk after a migration to the OS
     * keyring.
     *
     * @return true if a key file existed and was removed
     * @throws RuntimeException if the file cannot be overwritten or deleted
     */
    public boolean deleteStoredKeyFileSecurely() {
        Path keyPath = configDirectory.resolve(KEY_FILE);
        if (!Files.exists(keyPath)) {
            return false;
        }

        try {
            byte[] junk = new byte[(int) Math.max(Files.size(keyPath), 16)];
            new SecureRandom().nextBytes(junk);
            Files.write(keyPath, junk);
            Files.delete(keyPath);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo de clave", e);
        }
    }
}
