package com.dafi.desktop.domain;

/**
 * Base exception for domain-layer errors.
 */
public class DomainException extends RuntimeException {

    /**
     * Creates an exception with a descriptive message.
     *
     * @param message detail message explaining the domain error
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a descriptive message and an underlying cause.
     *
     * @param message detail message explaining the domain error
     * @param cause   the original exception that caused this error
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
