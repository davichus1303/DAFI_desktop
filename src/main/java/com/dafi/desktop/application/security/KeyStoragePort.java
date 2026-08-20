package com.dafi.desktop.application.security;

/**
 * Puerto de salida para el almacenamiento seguro de claves cifrado.
 * Permite abstraer el mecanismo de almacenamiento (keyring del SO, archivo seguro, etc.)
 */
public interface KeyStoragePort {

    /**
     * Obtiene la clave de cifrado almacenada.
     *
     * @return clave en formato Base64 o null si no existe
     */
    String getEncryptionKey();

    /**
     * Almacena la clave de cifrado.
     *
     * @param key clave en formato Base64
     */
    void storeEncryptionKey(String key);

    /**
     * Verifica si existe una clave de cifrado almacenada.
     *
     * @return true si existe una clave
     */
    boolean hasStoredKey();
}
