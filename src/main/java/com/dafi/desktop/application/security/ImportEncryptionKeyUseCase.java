package com.dafi.desktop.application.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Imports an encryption key from a plain text file previously produced by
 * {@link ExportEncryptionKeyUseCase} and stores it as the active key.
 * The file is user-independent: only the Base64 key content is read, so the
 * same file works for any operating-system account.
 */
public class ImportEncryptionKeyUseCase {

    private final KeyStoragePort keyStoragePort;

    /**
     * Creates the import use case.
     *
     * @param keyStoragePort port where the imported key will be stored
     */
    public ImportEncryptionKeyUseCase(KeyStoragePort keyStoragePort) {
        this.keyStoragePort = keyStoragePort;
    }

    /**
     * Reads the key from the given source file, validates its format and
     * stores it as the active encryption key.
     *
     * @param source file containing the Base64 encryption key
     * @throws IllegalArgumentException if the file does not contain a valid key
     * @throws IOException if the source file cannot be read
     */
    public void importFrom(Path source) throws IOException {
        String key = readKeyFile(source);
        storeKey(key);
    }

    private String readKeyFile(Path source) throws IOException {
        String content = Files.readString(source, StandardCharsets.UTF_8);
        return validateFormat(content.trim());
    }

    /**
     * Validates that the content is a Base64 AES key (16, 24 or 32 bytes).
     *
     * @param candidate trimmed file content
     * @return the same content when valid
     * @throws IllegalArgumentException if the content is not a valid key
     */
    private String validateFormat(String candidate) {
        try {
            byte[] decoded = Base64.getDecoder().decode(candidate);
            if (!isSupportedKeyLength(decoded.length)) {
                throw new IllegalArgumentException("La clave decodificada no tiene un tamaño válido");
            }
            return candidate;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "El archivo no contiene una clave de cifrado válida", e);
        }
    }

    private boolean isSupportedKeyLength(int decodedLength) {
        return decodedLength == 16 || decodedLength == 24 || decodedLength == 32;
    }

    private void storeKey(String key) {
        keyStoragePort.storeEncryptionKey(key);
    }
}
