package com.dafi.desktop.shared.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests para el resolutor de directorios base XDG de la aplicacion.
 */
class BaseDirectoryTest {

    @TempDir
    Path home;

    private Function<String, String> env(String dataHome, String configHome) {
        return name -> Map.of(
                "XDG_DATA_HOME", dataHome == null ? "" : dataHome,
                "XDG_CONFIG_HOME", configHome == null ? "" : configHome
        ).getOrDefault(name, "");
    }

    @Test
    void dataDirUsesXdgWhenSet() {
        Path xdgData = home.resolve("data-home");
        Path resolved = BaseDirectory.dataDir(home, env(xdgData.toString(), ""));

        assertEquals(xdgData.resolve("dafi"), resolved);
        assertTrue(Files.isDirectory(resolved));
    }

    @Test
    void dataDirFallsBackToHomeLocalShare() {
        Path resolved = BaseDirectory.dataDir(home, env(null, null));

        assertEquals(home.resolve(".local/share/dafi"), resolved);
        assertTrue(Files.isDirectory(resolved));
    }

    @Test
    void configDirUsesXdgWhenSet() {
        Path xdgConfig = home.resolve("config-home");
        Path resolved = BaseDirectory.configDir(home, env("", xdgConfig.toString()));

        assertEquals(xdgConfig.resolve("dafi"), resolved);
        assertTrue(Files.isDirectory(resolved));
    }

    @Test
    void configDirFallsBackToHomeDotConfig() {
        Path resolved = BaseDirectory.configDir(home, env(null, null));

        assertEquals(home.resolve(".config/dafi"), resolved);
        assertTrue(Files.isDirectory(resolved));
    }

    @Test
    void configDirKeepsLegacyWhenItExists() throws IOException {
        Path legacy = home.resolve(".dafi/config");
        Files.createDirectories(legacy);

        Path xdgConfig = home.resolve("config-home");
        Path resolved = BaseDirectory.configDir(home, env("", xdgConfig.toString()));

        assertEquals(legacy, resolved);
    }

    @Test
    void dataDirMigratesLegacyDafiDataOnce() throws IOException {
        Path legacyData = home.resolve(".dafi/data");
        Files.createDirectories(legacyData);
        Path clientFile = legacyData.resolve("clientes.json");
        Files.writeString(clientFile, "[]");

        Path xdgData = home.resolve("data-home");
        Path resolved = BaseDirectory.dataDir(home, env(xdgData.toString(), ""));

        assertTrue(Files.exists(resolved.resolve("clientes.json")));
        assertEquals("[]", Files.readString(resolved.resolve("clientes.json")));
        assertTrue(Files.exists(clientFile), "el origen legacy debe conservarse como respaldo");
    }

    @Test
    void dataDirCreatesEmptyXdgWhenNoLegacy() {
        Path xdgData = home.resolve("data-home");
        Path resolved = BaseDirectory.dataDir(home, env(xdgData.toString(), ""));

        assertTrue(Files.isDirectory(resolved));
        assertFalse(Files.exists(home.resolve(".dafi")));
    }

    @Test
    void dataDirDoesNotReMigrateWhenXdgAlreadyPresent() throws IOException {
        Path xdgData = home.resolve("data-home");
        Files.createDirectories(xdgData.resolve("dafi"));
        Files.writeString(xdgData.resolve("dafi/usuarios.json"), "[]");

        Path legacyData = home.resolve(".dafi/data");
        Files.createDirectories(legacyData);
        Files.writeString(legacyData.resolve("clientes.json"), "[]");

        BaseDirectory.dataDir(home, env(xdgData.toString(), ""));

        assertFalse(Files.exists(xdgData.resolve("dafi/clientes.json")),
                "una vez el directorio XDG existe, no debe haber migracion");
    }
}