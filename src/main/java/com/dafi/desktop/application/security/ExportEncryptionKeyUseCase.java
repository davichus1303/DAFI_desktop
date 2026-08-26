package com.dafi.desktop.application.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exports the stored encryption key to a plain text file chosen by the user.
 * The exported file is independent of the operating-system account: it only
 * contains the Base64 key, so it can later be imported on this or another
 * machine regardless of which system user owns the keyring entry.
 */
public class ExportEncryptionKeyUseCase {

    private final KeyStoragePort keyStoragePort;

    /**
     * Creates the export use case.
     *
     * @param keyStoragePort port holding the current encryption key
     */
    public ExportEncryptionKeyUseCase(KeyStoragePort keyStoragePort) {
        this.keyStoragePort = keyStoragePort;
    }

    /**
     * Writes the stored key to the given destination file.
     *
     * @param destination file where the key will be saved
     * @throws IllegalStateException if no encryption key is stored yet
     * @throws IOException if the destination file cannot be written
     */
    public void exportTo(Path destination) throws IOException {
        String key = retrieveStoredKey();
        writeKeyFile(destination, key);
    }

    private String retrieveStoredKey() {
        String key = keyStoragePort.getEncryptionKey();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Aún no existe una clave de cifrado para exportar");
        }
        return key.trim();
    }

    private void writeKeyFile(Path destination, String key) throws IOException {
        Files.writeString(destination, key + System.lineSeparator(), StandardCharsets.UTF_8);
    }
}
