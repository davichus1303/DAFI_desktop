package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.contracttype.ContractTypeCatalogRepositoryPort;
import com.dafi.desktop.domain.contracttype.ContractTypeCatalog;
import com.dafi.desktop.adapters.outbound.CryptoUtils;

import java.nio.file.Path;

/**
 * Thin outbound adapter implementing {@link ContractTypeCatalogRepositoryPort};
 * it persists contract type catalog entries as an AES-GCM encrypted JSON file
 * (contractTypes.json) in the application data directory (typically ~/.dafi/data).
 * Serialization is inherited from {@link AbstractJsonCatalogRepositoryAdapter}.
 */
public class JsonContractTypeCatalogRepositoryAdapter
        extends AbstractJsonCatalogRepositoryAdapter<ContractTypeCatalog>
        implements ContractTypeCatalogRepositoryPort {

    private static final String ARRAY_KEY = "contractTypes";
    private static final String FILE_NAME = "contractTypes.json";

    public JsonContractTypeCatalogRepositoryAdapter(Path dataDirectory, CryptoUtils cryptoUtils) {
        super(dataDirectory, cryptoUtils, ARRAY_KEY, FILE_NAME);
    }

    @Override
    protected ContractTypeCatalog buildEntry(String id, String name, String description) {
        return new ContractTypeCatalog(id, name, description);
    }
}
