package com.dafi.desktop.application.auth;

/**
 * Output port for credential storage, implemented by outbound adapters.
 */
public interface CredentialsStoragePort {

    /**
     * Retrieves the stored password hash for a user.
     *
     * @param username username to look up
     * @return stored hash, or {@code null} if the user does not exist
     */
    String getStoredHash(String username);

    /**
     * Retrieves the stored salt for a user.
     *
     * @param username username to look up
     * @return Base64-encoded salt, or {@code null} if the user does not exist
     */
    String getStoredSalt(String username);

    /**
     * Persists the credentials of a user.
     *
     * @param username       username
     * @param hashedPassword hashed password
     * @param salt           Base64-encoded salt
     */
    void storeCredentials(String username, String hashedPassword, String salt);

    /**
     * Checks whether at least one user is configured.
     *
     * @return {@code true} if a configured user exists
     */
    boolean hasConfiguredUser();
}
