package com.dafi.desktop.application.catalog;

import com.dafi.desktop.domain.shared.CatalogEntry;

import java.util.List;

/**
 * Generic output port for catalog entry persistence, implemented by outbound adapters.
 *
 * @param <E> catalog entry type
 */
public interface CatalogEntryRepositoryPort<E extends CatalogEntry> {

    /**
     * Retrieves all entries currently stored in the catalog.
     *
     * @return list of catalog entries (never {@code null})
     */
    List<E> findAll();

    /**
     * Inserts or updates a single catalog entry.
     *
     * @param entry entry to persist
     */
    void save(E entry);

    /**
     * Replaces the full contents of the catalog with the given entries.
     *
     * @param entries entries to persist
     */
    void saveAll(List<E> entries);
}
