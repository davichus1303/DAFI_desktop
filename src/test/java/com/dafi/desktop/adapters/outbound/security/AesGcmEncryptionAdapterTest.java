package com.dafi.desktop.adapters.outbound.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el adaptador de cifrado AES-256-GCM.
 */
class AesGcmEncryptionAdapterTest {

    private AesGcmEncryptionAdapter adapter;
    private String testKey;

    @BeforeEach
    void setUp() {
        adapter = new AesGcmEncryptionAdapter();
        byte[] keyBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(keyBytes);
        testKey = Base64.getEncoder().encodeToString(keyBytes);
    }

    @Test
    void testEncryptDecrypt() {
        String plaintext = "Datos sensibles del cliente";

        String encrypted = adapter.encrypt(plaintext, testKey);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);

        String decrypted = adapter.decrypt(encrypted, testKey);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testEncryptDecryptWithDifferentData() {
        String[] testData = {
                "Juan Pérez García",
                "35 años",
                "Colonia Centro",
                "500.00",
                ""
        };

        for (String plaintext : testData) {
            String encrypted = adapter.encrypt(plaintext, testKey);
            String decrypted = adapter.decrypt(encrypted, testKey);
            assertEquals(plaintext, decrypted);
        }
    }

    @Test
    void testDifferentCiphertextForSamePlaintext() {
        String plaintext = "Test data";

        String encrypted1 = adapter.encrypt(plaintext, testKey);
        String encrypted2 = adapter.encrypt(plaintext, testKey);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testInvalidCiphertextFormat() {
        assertThrows(RuntimeException.class, () -> {
            adapter.decrypt("invalid-format", testKey);
        });
    }

    @Test
    void testDecryptWithWrongKey() {
        String plaintext = "Secret data";
        String encrypted = adapter.encrypt(plaintext, testKey);

        byte[] wrongKeyBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(wrongKeyBytes);
        String wrongKey = Base64.getEncoder().encodeToString(wrongKeyBytes);

        assertThrows(RuntimeException.class, () -> {
            adapter.decrypt(encrypted, wrongKey);
        });
    }
}
