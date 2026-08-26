package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.catalog.CatalogEntryRepositoryPort;
import com.dafi.desktop.domain.shared.CatalogEntry;
import com.dafi.desktop.adapters.outbound.CryptoUtils;
import com.dafi.desktop.shared.utils.JsonObjectReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Outbound adapter implementing {@link CatalogEntryRepositoryPort} on top of an
 * AES-GCM encrypted JSON file located in the application data directory
 * (typically ~/.dafi/data). Subclasses only define where to store data and how
 * to map it.
 *
 * @param <E> the catalog entry type handled by this repository
 */
public abstract class AbstractJsonCatalogRepositoryAdapter<E extends CatalogEntry>
        implements CatalogEntryRepositoryPort<E> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJsonCatalogRepositoryAdapter.class);

    private final Path filePath;
    private final String arrayKey;
    private final CryptoUtils cryptoUtils;
    private final Gson gson;

    /**
     * Creates an adapter bound to a single encrypted JSON catalog file.
     *
     * @param dataDirectory   directory where the catalog file is stored
     * @param cryptoUtils     helper used to encrypt and decrypt the file contents
     * @param arrayKey        JSON property name wrapping the entries array
     * @param storageFileName file name of the catalog within {@code dataDirectory}
     */
    protected AbstractJsonCatalogRepositoryAdapter(Path dataDirectory,
                                                   CryptoUtils cryptoUtils,
                                                   String arrayKey,
                                                   String storageFileName) {
        this.filePath = dataDirectory.resolve(storageFileName);
        this.arrayKey = arrayKey;
        this.cryptoUtils = cryptoUtils;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Loads and decrypts the catalog file, returning every stored entry.
     *
     * @return all persisted entries, or an empty list if the file does not exist yet
     */
    @Override
    public List<E> findAll() {
        String json = cryptoUtils.loadEncryptedData(filePath);
        if (json == null) {
            return new ArrayList<>();
        }
        return parseEntries(json);
    }

    /**
     * Inserts or updates an entry, replacing any existing entry with the same id,
     * and rewrites the whole encrypted file.
     *
     * @param entry entry to persist
     */
    @Override
    public void save(E entry) {
        List<E> entries = findAll();
        entries.removeIf(existing -> existing.getId().equals(entry.getId()));
        entries.add(entry);
        saveAll(entries);
    }

    /**
     * Serializes the given entries under the configured array key and writes
     * them to the encrypted JSON file, replacing its previous contents.
     *
     * @param entries entries to persist
     */
    @Override
    public void saveAll(List<E> entries) {
        JsonArray entriesArray = new JsonArray();
        for (E entry : entries) {
            entriesArray.add(toJson(entry));
        }

        JsonObject wrapper = new JsonObject();
        wrapper.add(arrayKey, entriesArray);

        cryptoUtils.saveEncryptedData(gson.toJson(wrapper), filePath);
    }

    private List<E> parseEntries(String json) {
        try {
            JsonObject wrapper = JsonParser.parseString(json).getAsJsonObject();
            if (!wrapper.has(arrayKey) || wrapper.get(arrayKey).isJsonNull()) {
                log.warn("Clave '{}' no encontrada en el archivo JSON del catálogo", arrayKey);
                return new ArrayList<>();
            }
            JsonArray entriesArray = wrapper.getAsJsonArray(arrayKey);

            List<E> entries = new ArrayList<>();
            for (JsonElement element : entriesArray) {
                try {
                    entries.add(fromJson(element.getAsJsonObject()));
                } catch (Exception e) {
                    log.error("Error al deserializar entrada del catálogo, omitiendo: {}", element, e);
                }
            }
            return entries;
        } catch (Exception e) {
            log.error("Error al parsear el JSON del catálogo, retornando lista vacía", e);
            return new ArrayList<>();
        }
    }

    /**
     * Maps a JSON object to a catalog entry. Default implementation reads
     * {@code id}, {@code name} and {@code description} fields using the
     * {@link CatalogEntry} contract.
     *
     * @param obj JSON object read from the entries array
     * @return the domain entry built from the JSON data
     */
    protected E fromJson(JsonObject obj) {
        return buildEntry(
                JsonObjectReader.getStringOrEmpty(obj, "id"),
                JsonObjectReader.getStringOrEmpty(obj, "name"),
                JsonObjectReader.getStringOrEmpty(obj, "description"));
    }

    /**
     * Maps a catalog entry to a JSON object. Default implementation writes
     * {@code id}, {@code name} and {@code description} fields using the
     * {@link CatalogEntry} contract.
     *
     * @param entry entry to serialize
     * @return the JSON representation stored in the entries array
     */
    protected JsonObject toJson(E entry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", entry.getId());
        obj.addProperty("name", entry.getName() != null ? entry.getName() : "");
        obj.addProperty("description", entry.getDescription() != null ? entry.getDescription() : "");
        return obj;
    }

    /**
     * Creates a new domain entry from its basic fields. Subclasses must
     * implement this single method to provide the concrete type.
     *
     * @param id          entry identifier
     * @param name        entry display name
     * @param description entry description
     * @return a new instance of the concrete catalog entry
     */
    protected abstract E buildEntry(String id, String name, String description);
}
