package com.dafi.desktop.application.auth;

/**
 * Caso de uso para autenticación de usuarios.
 */
public class AuthenticateUserUseCase {

    private final CredentialsStoragePort credentialsStorage;
    private final PasswordHasherPort passwordHasher;

    /**
     * Constructor del caso de uso.
     *
     * @param credentialsStorage puerto de almacenamiento de credenciales
     * @param passwordHasher     puerto de hashing de contraseñas
     */
    public AuthenticateUserUseCase(CredentialsStoragePort credentialsStorage,
                                   PasswordHasherPort passwordHasher) {
        this.credentialsStorage = credentialsStorage;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Autentica al usuario con las credenciales proporcionadas.
     *
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     * @return true si la autenticación es exitosa
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
     * Verifica si existe un usuario configurado en el sistema.
     *
     * @return true si existe al menos un usuario
     */
    public boolean hasConfiguredUser() {
        return credentialsStorage.hasConfiguredUser();
    }

    /**
     * Registra un nuevo usuario administrador.
     *
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     */
    public void registerAdmin(String username, String password) {
        PasswordHasherPort.HashResult hashResult = passwordHasher.hash(password);
        credentialsStorage.storeCredentials(username, hashResult.hash(), hashResult.salt());
    }
}
