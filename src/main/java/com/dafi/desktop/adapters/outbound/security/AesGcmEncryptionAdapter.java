package com.dafi.desktop.adapters.outbound.security;

import com.dafi.desktop.application.security.EncryptionPort;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Outbound adapter implementing {@link EncryptionPort} using AES-GCM
 * (AES/GCM/NoPadding). Every operation uses a fresh random 12-byte IV and a
 * 128-bit GCM authentication tag, providing confidentiality plus integrity and
 * authenticity of the ciphertext. Keys are supplied by the caller as
 * Base64-encoded bytes (the application generates a 256-bit key on first run);
 * results are formatted as "Base64(IV):Base64(ciphertext)".
 */
public class AesGcmEncryptionAdapter implements EncryptionPort {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String SEPARATOR = ":";
    private static final String KEY_ALGORITHM = "AES";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts UTF-8 plaintext with the given key.
     *
     * @param plaintext text to encrypt
     * @param key       Base64-encoded AES key
     * @return the token "Base64(IV):Base64(ciphertext)"
     * @throws IllegalStateException if encryption fails or the key is invalid
     */
    @Override
    public String encrypt(String plaintext, String key) {
        try {
            SecretKey secretKey = decodeSecretKey(key);
            byte[] iv = generateInitializationVector();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return encode(iv) + SEPARATOR + encode(ciphertext);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Error al cifrar datos", e);
        }
    }

    /**
     * Decrypts a token produced by {@link #encrypt(String, String)}.
     *
     * @param ciphertext token in "Base64(IV):Base64(ciphertext)" format
     * @param key        Base64-encoded AES key used for the encryption
     * @return the decrypted UTF-8 text
     * @throws IllegalStateException if the token format is invalid, the key is
     *                               wrong or the data was tampered with
     */
    @Override
    public String decrypt(String ciphertext, String key) {
        try {
            String[] parts = ciphertext.split(SEPARATOR);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato de texto cifrado inválido");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);
            SecretKey secretKey = decodeSecretKey(key);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Error al descifrar datos", e);
        }
    }

    private SecretKey decodeSecretKey(String encodedKey) {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    private byte[] generateInitializationVector() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
