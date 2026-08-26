package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.auth.CredentialsStoragePort;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Outbound adapter implementing {@link CredentialsStoragePort}; it persists
 * per-user password hashes and salts as &lt;username&gt;.hash / &lt;username&gt;.salt
 * entries in a plain credentials.properties file under the application
 * configuration directory (typically ~/.dafi/config). The file itself is not
 * encrypted; secrecy relies on passwords being stored only as Argon2 hashes.
 */
public class FileCredentialsStorageAdapter implements CredentialsStoragePort {

    private static final String CREDENTIALS_FILE = "credentials.properties";
    private final Path configDirectory;

    /**
     * Creates the adapter.
     *
     * @param configDirectory application configuration directory where
     *                        credentials.properties is stored
     */
    public FileCredentialsStorageAdapter(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    /**
     * Returns the stored password hash for a user.
     *
     * @param username user whose hash is retrieved
     * @return the stored hash, or null if the user is unknown
     */
    @Override
    public String getStoredHash(String username) {
        Properties props = loadProperties();
        return props.getProperty(username + ".hash");
    }

    /**
     * Returns the stored salt for a user.
     *
     * @param username user whose salt is retrieved
     * @return the stored salt, or null if the user is unknown
     */
    @Override
    public String getStoredSalt(String username) {
        Properties props = loadProperties();
        return props.getProperty(username + ".salt");
    }

    /**
     * Stores or replaces the credentials of a user, creating the configuration
     * directory if needed and preserving any other users already present in the file.
     *
     * @param username       user name
     * @param hashedPassword hashed password to store
     * @param salt           salt associated with the hash
     * @throws RuntimeException if the credentials file cannot be written
     */
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

    /**
     * Indicates whether any user has been configured.
     *
     * @return true if the credentials file contains at least one user
     */
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
