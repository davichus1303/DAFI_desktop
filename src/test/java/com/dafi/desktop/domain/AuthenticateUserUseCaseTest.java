package com.dafi.desktop.domain;

import com.dafi.desktop.application.auth.AuthenticateUserUseCase;
import com.dafi.desktop.application.auth.CredentialsStoragePort;
import com.dafi.desktop.application.auth.PasswordHasherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests para el caso de uso de autenticación.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private CredentialsStoragePort credentialsStorage;

    @Mock
    private PasswordHasherPort passwordHasher;

    private AuthenticateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateUserUseCase(credentialsStorage, passwordHasher);
    }

    @Test
    void testSuccessfulAuthentication() {
        String username = "admin";
        String password = "securePassword123";
        String storedHash = "hashed_password";
        String storedSalt = "salt_value";

        when(credentialsStorage.getStoredHash(username)).thenReturn(storedHash);
        when(credentialsStorage.getStoredSalt(username)).thenReturn(storedSalt);
        when(passwordHasher.verify(password, storedHash, storedSalt)).thenReturn(true);

        boolean result = useCase.authenticate(username, password);

        assertTrue(result);
    }

    @Test
    void testFailedAuthentication() {
        String username = "admin";
        String password = "wrongPassword";
        String storedHash = "hashed_password";
        String storedSalt = "salt_value";

        when(credentialsStorage.getStoredHash(username)).thenReturn(storedHash);
        when(credentialsStorage.getStoredSalt(username)).thenReturn(storedSalt);
        when(passwordHasher.verify(password, storedHash, storedSalt)).thenReturn(false);

        boolean result = useCase.authenticate(username, password);

        assertFalse(result);
    }

    @Test
    void testAuthenticationWithNonexistentUser() {
        String username = "nonexistent";
        String password = "password";

        when(credentialsStorage.getStoredHash(username)).thenReturn(null);
        when(credentialsStorage.getStoredSalt(username)).thenReturn(null);

        boolean result = useCase.authenticate(username, password);

        assertFalse(result);
    }

    @Test
    void testHasConfiguredUser() {
        when(credentialsStorage.hasConfiguredUser()).thenReturn(true);

        assertTrue(useCase.hasConfiguredUser());
    }

    @Test
    void testRegisterAdmin() {
        String username = "admin";
        String password = "password123";
        String hashedPassword = "hashed";
        String salt = "salt";

        PasswordHasherPort.HashResult hashResult = new PasswordHasherPort.HashResult(hashedPassword, salt);
        when(passwordHasher.hash(password)).thenReturn(hashResult);

        useCase.registerAdmin(username, password);

        org.mockito.Mockito.verify(credentialsStorage).storeCredentials(username, hashedPassword, salt);
    }
}
