package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.security.KeyStoragePort;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Adaptador de almacenamiento de claves en archivo local.
 * En una implementación futura podría utilizarse el keyring del sistema operativo.
 */
public class FileKeyStorageAdapter implements KeyStoragePort {

    private static final String KEY_FILE = "dafi.key";
    private final Path configDirectory;

    /**
     * Constructor del adaptador.
     *
     * @param configDirectory directorio de configuración de la aplicación
     */
    public FileKeyStorageAdapter(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

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

    @Override
    public boolean hasStoredKey() {
        return Files.exists(configDirectory.resolve(KEY_FILE));
    }
}
