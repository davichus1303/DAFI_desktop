package com.dafi.desktop.application.auth;

/**
 * Use case for user authentication against stored credentials.
 */
public class AuthenticateUserUseCase {

    private final CredentialsStoragePort credentialsStorage;
    private final PasswordHasherPort passwordHasher;

    /**
     * Creates the use case.
     *
     * @param credentialsStorage credential storage port
     * @param passwordHasher     password hashing port
     */
    public AuthenticateUserUseCase(CredentialsStoragePort credentialsStorage,
                                   PasswordHasherPort passwordHasher) {
        this.credentialsStorage = credentialsStorage;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Authenticates a user with the given credentials.
     *
     * @param username username
     * @param password plaintext password
     * @return {@code true} if authentication succeeds
     */
    public boolean authenticate(String username, String password) {
        String storedHash = credentialsStorage.getStoredHash(username);
        String storedSalt = credentialsStorage.getStoredSalt(username);

        if (storedHash == null || storedSalt == null) {
            return false;
        }

        return passwordHasher.verify(password, storedHash, storedSalt);
    }

    /**
     * Checks whether an administrator user is already configured in the system.
     *
     * @return {@code true} if at least one user exists
     */
    public boolean hasConfiguredUser() {
        return credentialsStorage.hasConfiguredUser();
    }

    /**
     * Registers a new administrator user with the given credentials.
     *
     * @param username username
     * @param password plaintext password
     */
    public void registerAdmin(String username, String password) {
        PasswordHasherPort.HashResult hashResult = passwordHasher.hash(password);
        credentialsStorage.storeCredentials(username, hashResult.hash(), hashResult.salt());
    }
}
