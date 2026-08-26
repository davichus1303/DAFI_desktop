package com.dafi.desktop.application.auth;

/**
 * Output port for password hashing, implemented by outbound adapters.
 */
public interface PasswordHasherPort {

    /**
     * Generates a secure hash of the given plaintext password.
     *
     * @param password plaintext password
     * @return a {@link HashResult} containing the hash and the salt
     */
    HashResult hash(String password);

    /**
     * Verifies whether a plaintext password matches a stored hash.
     *
     * @param password plaintext password
     * @param hash     stored hash
     * @param salt     salt used when the hash was generated
     * @return {@code true} if the password is valid
     */
    boolean verify(String password, String hash, String salt);

    /**
     * Result of a hashing operation.
     *
     * @param hash generated password hash
     * @param salt salt associated with the hash
     */
    record HashResult(String hash, String salt) {}
}
