package com.dafi.desktop.domain.client;

/**
 * Enum que representa los tipos de contrato disponibles.
 */
public enum ContractType {
    BASIC("Básico"),
    STANDARD("Estándar"),
    PREMIUM("Premium");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
