package com.dafi.desktop.domain.contracttype;

import com.dafi.desktop.domain.shared.AbstractCatalogEntry;

/**
 * Entity that represents a contract type entry in the catalog.
 */
public class ContractTypeCatalog extends AbstractCatalogEntry {

    /**
     * Creates a contract type catalog entry.
     *
     * @param id          unique identifier of the entry, must not be null
     * @param name        display name of the entry, must not be blank
     * @param description optional description of the entry
     */
    public ContractTypeCatalog(String id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public String toString() {
        return "ContractTypeCatalog{id='" + getId() + "', name='" + getName() + "'}";
    }
}
