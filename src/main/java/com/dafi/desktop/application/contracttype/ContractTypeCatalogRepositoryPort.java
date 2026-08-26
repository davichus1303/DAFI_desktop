package com.dafi.desktop.application.contracttype;

import com.dafi.desktop.application.catalog.CatalogEntryRepositoryPort;
import com.dafi.desktop.domain.contracttype.ContractTypeCatalog;

/**
 * Output port for contract type catalog persistence.
 * Marker specialization of {@link CatalogEntryRepositoryPort}.
 */
public interface ContractTypeCatalogRepositoryPort extends CatalogEntryRepositoryPort<ContractTypeCatalog> {
}
