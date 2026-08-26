package com.dafi.desktop.shared.utils;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Utility for safely reading typed values from a Gson {@link JsonObject}.
 * All methods log a warning when a fallback value is returned due to missing
 * or corrupt data, so that silent data corruption is always observable.
 */
public final class JsonObjectReader {

    private static final Logger log = LoggerFactory.getLogger(JsonObjectReader.class);

    private JsonObjectReader() {
    }

    /**
     * Returns the string value for the given key, or an empty string when
     * the key is missing or null.
     *
     * @param obj JSON object to read from
     * @param key member name to read
     * @return the string value, or an empty string as fallback
     */
    public static String getStringOrEmpty(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    /**
     * Returns the decimal value for the given key, falling back to zero when
     * the value is missing, null, or not a valid number.
     *
     * @param obj JSON object to read from
     * @param key member name to read
     * @return the parsed value, or {@link BigDecimal#ZERO} as fallback
     */
    public static BigDecimal getBigDecimalOrZero(JsonObject obj, String key) {
        String value = getStringOrEmpty(obj, key);
        if (value.isEmpty()) {
            log.warn("Campo '{}' no encontrado o vacío, usando 0 como valor por defecto", key);
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Campo '{}' tiene valor no numérico '{}', usando 0 como valor por defecto", key, value);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Returns the ISO-8601 date for the given key, or null when the value
     * is missing or unparsable.
     *
     * @param obj JSON object to read from
     * @param key member name to read
     * @return the parsed date, or null as fallback
     */
    public static LocalDate getDateOrNull(JsonObject obj, String key) {
        String value = getStringOrEmpty(obj, key);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            log.warn("Campo '{}' tiene fecha inválida '{}', usando null como valor por defecto", key, value);
            return null;
        }
    }

    /**
     * Returns the integer value for the given key, or zero when the key is
     * missing, null, or not a valid integer.
     *
     * @param obj JSON object to read from
     * @param key member name to read
     * @return the integer value, or 0 as fallback
     */
    public static int getIntOrZero(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            log.warn("Campo '{}' no encontrado o nulo, usando 0 como valor por defecto", key);
            return 0;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (NumberFormatException | UnsupportedOperationException e) {
            log.warn("Campo '{}' tiene valor no entero '{}', usando 0 como valor por defecto",
                    key, obj.get(key));
            return 0;
        }
    }
}
