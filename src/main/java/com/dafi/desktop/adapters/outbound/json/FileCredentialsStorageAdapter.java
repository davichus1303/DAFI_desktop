package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.auth.CredentialsStoragePort;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Adaptador de almacenamiento de credenciales en archivo local.
 */
public class FileCredentialsStorageAdapter implements CredentialsStoragePort {

    private static final String CREDENTIALS_FILE = "credentials.properties";
    private final Path configDirectory;

    /**
     * Constructor del adaptador.
     *
     * @param configDirectory directorio de configuración de la aplicación
     */
    public FileCredentialsStorageAdapter(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    @Override
    public String getStoredHash(String username) {
        Properties props = loadProperties();
        return props.getProperty(username + ".hash");
    }

    @Override
    public String getStoredSalt(String username) {
        Properties props = loadProperties();
        return props.getProperty(username + ".salt");
    }

    @Override
    public void storeCredentials(String username, String hashedPassword, String salt) {
        try {
            Files.createDirectories(configDirectory);
            Path credentialsPath = configDirectory.resolve(CREDENTIALS_FILE);

            Properties props = new Properties();
            if (Files.exists(credentialsPath)) {
                try (InputStream in = Files.newInputStream(credentialsPath)) {
                    props.load(in);
                }
            }

            props.setProperty(username + ".hash", hashedPassword);
            props.setProperty(username + ".salt", salt);

            try (OutputStream out = Files.newOutputStream(credentialsPath)) {
                props.store(out, "DAFI Credentials");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar credenciales", e);
        }
    }

    @Override
    public boolean hasConfiguredUser() {
        Properties props = loadProperties();
        return !props.isEmpty();
    }

    private Properties loadProperties() {
        Path credentialsPath = configDirectory.resolve(CREDENTIALS_FILE);
        Properties props = new Properties();

        if (Files.exists(credentialsPath)) {
            try (InputStream in = Files.newInputStream(credentialsPath)) {
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Error al leer credenciales", e);
            }
        }

        return props;
    }
}
