package com.dafi.desktop.domain.shared;

import java.util.Objects;

/**
 * Immutable base implementation of a catalog entry. Both {@code ContractTypeCatalog}
 * and {@code PaymentMethodCatalog} share identical structure; this class
 * centralizes the fields, getters and value semantics so that concrete
 * subclasses carry only a distinguishing type name.
 * <p>
 * Instances can only be created through the parameterized constructor;
 * there are no public mutators.
 */
public abstract class AbstractCatalogEntry implements CatalogEntry {

    private final String id;
    private final String name;
    private final String description;

    /**
     * Creates a catalog entry with the given data.
     *
     * @param id          unique identifier, must not be null
     * @param name        display name of the entry, must not be blank
     * @param description optional description, may be null
     * @throws NullPointerException     if {@code id} is null
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    protected AbstractCatalogEntry(String id, String name, String description) {
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.name = name;
        this.description = description;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCatalogEntry that = (AbstractCatalogEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
