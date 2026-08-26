package com.dafi.desktop.domain.shared;

/**
 * Common contract for entities that belong to a configurable catalog.
 */
public interface CatalogEntry {

    /**
     * Returns the unique identifier of the catalog entry.
     *
     * @return the entry identifier
     */
    String getId();

    /**
     * Returns the display name of the catalog entry.
     *
     * @return the entry name
     */
    String getName();

    /**
     * Returns the description of the catalog entry.
     *
     * @return the entry description
     */
    String getDescription();
}
