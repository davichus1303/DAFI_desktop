package com.dafi.desktop.adapters.outbound;

import com.dafi.desktop.application.security.EncryptionPort;
import com.dafi.desktop.application.security.KeyStoragePort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reusable utility for encryption and storage of encrypted data.
 * Centralizes encrypt/decrypt logic and reading/writing of encrypted files.
 * It has no knowledge of JSON structure or domain models.
 */
public class CryptoUtils {

    private final EncryptionPort encryption;
    private final KeyStoragePort keyStorage;

    /**
     * Creates a utility bound to the given encryption and key storage ports.
     *
     * @param encryption port used to perform encryption and decryption
     * @param keyStorage port used to obtain the stored encryption key
     */
    public CryptoUtils(EncryptionPort encryption, KeyStoragePort keyStorage) {
        this.encryption = encryption;
        this.keyStorage = keyStorage;
    }

    /**
     * Encrypts plain text using the stored key.
     *
     * @param plaintext text to encrypt
     * @return the encrypted text
     */
    public String encrypt(String plaintext) {
        String key = getKeyOrThrow();
        return encryption.encrypt(plaintext, key);
    }

    /**
     * Decrypts cipher text using the stored key.
     *
     * @param ciphertext text to decrypt
     * @return the decrypted text
     */
    public String decrypt(String ciphertext) {
        String key = getKeyOrThrow();
        return encryption.decrypt(ciphertext, key);
    }

    /**
     * Encrypts the given JSON and writes it to the specified file,
     * creating parent directories if they do not exist.
     *
     * @param json     JSON already serialized as a String
     * @param filePath path of the destination file
     */
    public void saveEncryptedData(String json, Path filePath) {
        try {
            String encrypted = encrypt(json);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, encrypted);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar datos cifrados en " + filePath, e);
        }
    }

    /**
     * Reads the encrypted file and decrypts its contents.
     *
     * @param filePath path of the encrypted file
     * @return the decrypted JSON as a String, or null if the file does not exist
     */
    public String loadEncryptedData(Path filePath) {
        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            String encryptedContent = Files.readString(filePath);
            return decrypt(encryptedContent);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer datos cifrados de " + filePath, e);
        }
    }

    private String getKeyOrThrow() {
        String key = keyStorage.getEncryptionKey();
        if (key == null) {
            throw new RuntimeException("No se encontró la clave de cifrado");
        }
        return key;
    }
}
