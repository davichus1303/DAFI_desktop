package com.dafi.desktop.infrastructure;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;

/**
 * Servicio para cargar etiquetas de internacionalización desde JSON.
 */
public class I18n {

    private static I18n instance;
    private JsonObject labels;

    private I18n() {
        loadLanguage("es");
    }

    /**
     * Obtiene la instancia única del servicio I18n.
     *
     * @return instancia de I18n
     */
    public static synchronized I18n getInstance() {
        if (instance == null) {
            instance = new I18n();
        }
        return instance;
    }

    /**
     * Carga un idioma desde el recurso JSON.
     *
     * @param language código del idioma (ej: "es", "en")
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
     * Obtiene una etiqueta por su clave.
     *
     * @param key clave de la etiqueta (ej: "app.title")
     * @return texto de la etiqueta
     * @throws MissingResourceException si la clave no existe
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
     * Obtiene una etiqueta con un valor por defecto si no existe.
     *
     * @param key          clave de la etiqueta
     * @param defaultValue valor por defecto
     * @return texto de la etiqueta o el valor por defecto
     */
    public String getOrDefault(String key, String defaultValue) {
        try {
            return get(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }
}
