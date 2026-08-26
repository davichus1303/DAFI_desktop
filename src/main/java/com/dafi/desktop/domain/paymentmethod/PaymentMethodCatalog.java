package com.dafi.desktop.domain.paymentmethod;

import com.dafi.desktop.domain.shared.AbstractCatalogEntry;

/**
 * Entity that represents a payment method entry in the catalog.
 */
public class PaymentMethodCatalog extends AbstractCatalogEntry {

    /**
     * Creates a payment method catalog entry.
     *
     * @param id          unique identifier of the entry, must not be null
     * @param name        display name of the entry, must not be blank
     * @param description optional description of the entry
     */
    public PaymentMethodCatalog(String id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public String toString() {
        return "PaymentMethodCatalog{id='" + getId() + "', name='" + getName() + "'}";
    }
}
