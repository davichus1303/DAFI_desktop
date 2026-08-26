package com.dafi.desktop.domain.shared;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable value object that represents a validated email address.
 * Null or blank values are treated as "no email" ({@code isEmpty()} returns
 * {@code true}); non-blank values are validated against a standard regex
 * and rejected with an {@link IllegalArgumentException} on creation.
 */
public final class Email {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    /**
     * Creates an email from the given raw string.
     *
     * @param raw raw email string; {@code null} or blank is treated as empty
     * @throws IllegalArgumentException if the value is non-blank but does
     *         not match the expected email format
     */
    public Email(String raw) {
        String trimmed = raw == null ? null : raw.trim();
        if (trimmed == null || trimmed.isBlank()) {
            this.value = null;
        } else {
            if (!PATTERN.matcher(trimmed).matches()) {
                throw new IllegalArgumentException(
                        "El correo electrónico no tiene un formato válido: " + trimmed);
            }
            this.value = trimmed;
        }
    }

    /** Returns the validated email string, or {@code null} if empty. */
    public String value() { return value; }

    /** Returns {@code true} when no email was provided. */
    public boolean isEmpty() { return value == null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value != null ? value : ""; }
}
