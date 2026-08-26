package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.domain.shared.CatalogEntry;

/**
 * Row presentation model shared by every catalog table.
 */
public class CatalogEntryViewModel {

    private final String id;
    private final String name;
    private final String description;

    /**
     * Creates a view model from a domain catalog entry.
     *
     * @param entry domain entry to project
     * @return an immutable row model for the entry
     */
    public static CatalogEntryViewModel fromEntry(CatalogEntry entry) {
        return new CatalogEntryViewModel(entry.getId(), entry.getName(), entry.getDescription());
    }

    private CatalogEntryViewModel(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
