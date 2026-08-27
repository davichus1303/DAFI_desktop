package com.dafi.desktop.shared.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Resolves the application directories following the XDG Base Directory
 * specification so that DAFI Desktop behaves correctly inside sandboxed
 * environments (Flatpak) and portable installations alike.
 *
 * <p>Resolutions performed:
 * <ul>
 *   <li>Data directory: {@code $XDG_DATA_HOME/dafi}, defaulting to
 *       {@code ~/.local/share/dafi}. For backward compatibility with earlier
 *       versions, the legacy {@code ~/.dafi/data} location is kept when no
 *       XDG directory exists yet.</li>
 *   <li>Config directory: {@code $XDG_CONFIG_HOME/dafi}, defaulting to
 *       {@code ~/.config/dafi}.</li>
 * </ul>
 */
public final class BaseDirectory {

    private static final Logger log = LoggerFactory.getLogger(BaseDirectory.class);

    private static final String APP_DIR_NAME = "dafi";
    private static final String LEGACY_HOME_DIR = ".dafi";

    private BaseDirectory() {
    }

    /**
     * Resolves the configuration directory, creating it when it does not exist
     * yet. Prefers the XDG location, falling back to the legacy
     * {@code ~/.dafi/config} when it already holds credentials.
     *
     * @return the absolute configuration directory path
     * @throws IllegalStateException when the directory cannot be created
     */
    public static Path configDir() {
        return configDir(userHome(), name -> System.getenv(name));
    }

    /**
     * Package-private overload that resolves the config directory against an
     * explicit home and environment, allowing deterministic testing.
     */
    static Path configDir(Path home, Function<String, String> env) {
        Path xdg = Path.of(valueOrFallback(env, "XDG_CONFIG_HOME", home, ".config"), APP_DIR_NAME);
        if (Files.isDirectory(xdg)) {
            return xdg;
        }
        Path legacy = home.resolve(LEGACY_HOME_DIR).resolve("config");
        if (Files.isDirectory(legacy)) {
            log.info("Usando directorio de configuracion heredado: {}", legacy);
            return legacy;
        }
        return createIfNeeded(xdg, "config");
    }

    /**
     * Resolves the data directory, creating it when it does not exist yet.
     * When the XDG location does not exist but the legacy {@code ~/.dafi/data}
     * directory holds data, the legacy content is migrated once into the XDG
     * location so existing installations keep their data.
     *
     * @return the absolute data directory path
     * @throws IllegalStateException when the directory cannot be created
     */
    public static Path dataDir() {
        return dataDir(userHome(), name -> System.getenv(name));
    }

    /**
     * Package-private overload that resolves the data directory against an
     * explicit home and environment, allowing deterministic testing.
     */
    static Path dataDir(Path home, Function<String, String> env) {
        Path xdg = Path.of(valueOrFallback(env, "XDG_DATA_HOME", home, ".local/share"), APP_DIR_NAME);
        if (!Files.isDirectory(xdg)) {
            migrateLegacyDataIfPresent(home, xdg);
        }
        return createIfNeeded(xdg, "data");
    }

    /**
     * Copies the contents of the legacy {@code ~/.dafi/data} directory into
     * the XDG data directory on first startup, leaving the original in place
     * as a backup. Silent (only logged) because the app must still start when
     * the legacy directory is empty or unreadable.
     */
    private static void migrateLegacyDataIfPresent(Path home, Path xdgData) {
        Path legacy = home.resolve(LEGACY_HOME_DIR).resolve("data");
        if (!Files.isDirectory(legacy)) {
            return;
        }
        try {
            Files.createDirectories(xdgData);
            try (var entries = Files.list(legacy)) {
                entries.filter(Files::isRegularFile).forEach(file -> {
                    try {
                        Files.copy(file, xdgData.resolve(file.getFileName()));
                    } catch (IOException e) {
                        log.warn("No se pudo migrar el archivo heredado {}", file, e);
                    }
                });
            }
            log.info("Datos migrados de {} a {}", legacy, xdgData);
        } catch (IOException e) {
            log.warn("No se pudo migrar el directorio de datos heredado {}", legacy, e);
        }
    }

    /**
     * Returns the {@code XDG_DATA_HOME} value, defaulting to
     * {@code ~/.local/share} when the environment variable is unset or empty.
     *
     * @return the resolved XDG data home
     */
    public static String xdgDataHome() {
        return valueOrFallback(name -> System.getenv(name), "XDG_DATA_HOME",
                userHome(), ".local/share");
    }

    /**
     * Returns the {@code XDG_CONFIG_HOME} value, defaulting to
     * {@code ~/.config} when the environment variable is unset or empty.
     *
     * @return the resolved XDG config home
     */
    public static String xdgConfigHome() {
        return valueOrFallback(name -> System.getenv(name), "XDG_CONFIG_HOME",
                userHome(), ".config");
    }

    private static Path userHome() {
        return Path.of(System.getProperty("user.home"));
    }

    private static String valueOrFallback(Function<String, String> env, String envVar,
                                          Path home, String homeRelative) {
        String value = env.apply(envVar);
        if (value == null || value.isBlank()) {
            return home.resolve(homeRelative).toString();
        }
        return value;
    }

    private static Path createIfNeeded(Path directory, String label) {
        try {
            Files.createDirectories(directory);
            return directory;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de " + label + ": " + directory, e);
        }
    }
}