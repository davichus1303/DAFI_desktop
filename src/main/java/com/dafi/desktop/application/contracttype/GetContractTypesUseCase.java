package com.dafi.desktop.application.contracttype;

import com.dafi.desktop.domain.contracttype.ContractTypeCatalog;
import java.util.List;

/**
 * Use case that retrieves contract type entries from the catalog.
 */
public class GetContractTypesUseCase {

    private final ContractTypeCatalogRepositoryPort repository;

    /**
     * Creates the use case.
     *
     * @param repository contract type catalog port
     */
    public GetContractTypesUseCase(ContractTypeCatalogRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Returns all contract types currently stored in the catalog.
     *
     * @return list of contract types (never {@code null})
     */
    public List<ContractTypeCatalog> getAll() {
        return repository.findAll();
    }
}
