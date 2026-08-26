package com.dafi.desktop.application.paymentmethod;

import com.dafi.desktop.application.catalog.CatalogEntryRepositoryPort;
import com.dafi.desktop.domain.paymentmethod.PaymentMethodCatalog;

/**
 * Output port for payment method catalog persistence.
 * Marker specialization of {@link CatalogEntryRepositoryPort}.
 */
public interface PaymentMethodCatalogRepositoryPort extends CatalogEntryRepositoryPort<PaymentMethodCatalog> {
}
