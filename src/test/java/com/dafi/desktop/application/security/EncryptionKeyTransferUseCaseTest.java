package com.dafi.desktop.application.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for exporting and importing the encryption key through a fake
 * storage port, verifying round-trip fidelity and validation of bad files.
 */
class EncryptionKeyTransferUseCaseTest {

    @TempDir
    Path tempDir;

    private FakeKeyStoragePort keyStoragePort;
    private ExportEncryptionKeyUseCase exportUseCase;
    private ImportEncryptionKeyUseCase importUseCase;
    private String sampleKey;

    @BeforeEach
    void setUp() {
        keyStoragePort = new FakeKeyStoragePort();
        exportUseCase = new ExportEncryptionKeyUseCase(keyStoragePort);
        importUseCase = new ImportEncryptionKeyUseCase(keyStoragePort);

        byte[] rawKey = new byte[32];
        new SecureRandom().nextBytes(rawKey);
        sampleKey = Base64.getEncoder().encodeToString(rawKey);
    }

    @Test
    void exportsStoredKeyAsSingleTextLine() throws Exception {
        keyStoragePort.storeEncryptionKey(sampleKey);
        Path destination = tempDir.resolve("clave.txt");

        exportUseCase.exportTo(destination);

        String content = Files.readString(destination).trim();
        assertEquals(sampleKey, content);
    }

    @Test
    void exportFailsWhenNoKeyIsStored() {
        Path destination = tempDir.resolve("clave.txt");

        assertThrows(IllegalStateException.class, () -> exportUseCase.exportTo(destination));
    }

    @Test
    void importsExportedFileAndStoresTheSameKey() throws Exception {
        keyStoragePort.storeEncryptionKey(sampleKey);
        Path exported = tempDir.resolve("clave.txt");
        exportUseCase.exportTo(exported);
        keyStoragePort.reset();

        importUseCase.importFrom(exported);

        assertEquals(sampleKey, keyStoragePort.storedKey);
    }

    @Test
    void rejectsFilesWithoutValidBase64Content() throws Exception {
        Path invalid = tempDir.resolve("invalida.txt");
        Files.writeString(invalid, "esto no es una clave!!!");

        assertThrows(IllegalArgumentException.class, () -> importUseCase.importFrom(invalid));
        assertEquals(null, keyStoragePort.storedKey);
    }

    @Test
    void rejectsBase64ContentThatDoesNotDecodeToAesKeyLength() throws Exception {
        Path wrongLength = tempDir.resolve("corta.txt");
        Files.writeString(wrongLength, Base64.getEncoder().encodeToString(new byte[10]));

        assertThrows(IllegalArgumentException.class, () -> importUseCase.importFrom(wrongLength));
        assertEquals(null, keyStoragePort.storedKey);
    }

    /** Minimal in-memory stand-in for the real key storage adapters. */
    private static class FakeKeyStoragePort implements KeyStoragePort {
        private String storedKey;

        @Override
        public String getEncryptionKey() {
            return storedKey;
        }

        @Override
        public void storeEncryptionKey(String key) {
            this.storedKey = key;
        }

        @Override
        public boolean hasStoredKey() {
            return storedKey != null;
        }

        void reset() {
            storedKey = null;
        }
    }
}
