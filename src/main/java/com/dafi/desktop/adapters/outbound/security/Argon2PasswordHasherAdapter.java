package com.dafi.desktop.adapters.outbound.security;

import com.dafi.desktop.application.auth.PasswordHasherPort;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Outbound adapter implementing {@link PasswordHasherPort}, backed by the
 * argon2-jvm binding. Uses the library's default factory instance (Argon2i,
 * 16-byte salt and 32-byte hash length) with cost parameters of 3 iterations,
 * 65536 KiB of memory and a parallelism of 1.
 */
public class Argon2PasswordHasherAdapter implements PasswordHasherPort {

    private final Argon2 argon2;

    /**
     * Creates the adapter with the library's default Argon2 instance.
     */
    public Argon2PasswordHasherAdapter() {
        this.argon2 = Argon2Factory.create();
    }

    /**
     * Hashes a password with cost parameters (3 iterations, 65536 KiB of memory,
     * parallelism 1). The returned result contains the PHC-formatted hash, which
     * embeds its own random salt, plus an independently generated 16-byte
     * Base64-encoded salt kept for storage purposes.
     *
     * @param password password to hash
     * @return the encoded hash together with a freshly generated salt
     */
    @Override
    public HashResult hash(String password) {
        String hash = argon2.hash(3, 65536, 1, password.toCharArray());
        String salt = java.util.Base64.getEncoder().encodeToString(
                java.security.SecureRandom.getSeed(16));
        return new HashResult(hash, salt);
    }

    /**
     * Verifies a password against a previously generated hash. Verification
     * relies on the salt embedded in the encoded hash; the salt argument is not used.
     *
     * @param password password to check
     * @param hash     PHC-formatted hash previously produced by {@link #hash(String)}
     * @param salt     stored salt (ignored during verification)
     * @return true if the password matches the hash
     */
    @Override
    public boolean verify(String password, String hash, String salt) {
        return argon2.verify(hash, password.toCharArray());
    }
}
