package com.dafi.desktop.infrastructure;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;

/**
 * Service that loads internationalization labels from JSON resources.
 */
public class I18n {

    private static I18n instance;
    private JsonObject labels;

    private I18n() {
        loadLanguage("es");
    }

    /**
     * Returns the singleton instance of the I18n service.
     *
     * @return the shared I18n instance
     */
    public static synchronized I18n getInstance() {
        if (instance == null) {
            instance = new I18n();
        }
        return instance;
    }

    /**
     * Loads a language from its JSON resource.
     *
     * @param language language code (e.g. "es", "en")
     * @throws RuntimeException if the resource cannot be read or parsed
     */
    public void loadLanguage(String language) {
        String path = "/i18n/" + language + ".json";
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream(path), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            labels = gson.fromJson(reader, JsonObject.class);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Error al cargar idioma: " + language, e);
        }
    }

    /**
     * Returns the label for the given key.
     *
     * @param key label key (e.g. "app.title")
     * @return the label text
     * @throws MissingResourceException if the key does not exist
     */
    public String get(String key) {
        if (labels == null || !labels.has(key)) {
            throw new MissingResourceException(
                    "Etiqueta no encontrada: " + key,
                    "i18n",
                    key
            );
        }
        return labels.get(key).getAsString();
    }

    /**
     * Returns the label for the given key, or a default value when missing.
     *
     * @param key          label key
     * @param defaultValue value returned when the key does not exist
     * @return the label text or the default value
     */
    public String getOrDefault(String key, String defaultValue) {
        try {
            return get(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }
}
