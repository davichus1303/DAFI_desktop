package com.dafi.desktop.adapters.outbound.security;

import com.dafi.desktop.application.auth.PasswordHasherPort;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Implementación del puerto de hashing de contraseñas utilizando Argon2id.
 * Argon2id es el algoritmo recomendado para hashing de contraseñas.
 */
public class Argon2PasswordHasherAdapter implements PasswordHasherPort {

    private final Argon2 argon2;

    /**
     * Constructor del adaptador de hashing Argon2.
     */
    public Argon2PasswordHasherAdapter() {
        this.argon2 = Argon2Factory.create();
    }

    @Override
    public HashResult hash(String password) {
        String hash = argon2.hash(3, 65536, 1, password.toCharArray());
        String salt = java.util.Base64.getEncoder().encodeToString(
                java.security.SecureRandom.getSeed(16));
        return new HashResult(hash, salt);
    }

    @Override
    public boolean verify(String password, String hash, String salt) {
        return argon2.verify(hash, password.toCharArray());
    }
}
