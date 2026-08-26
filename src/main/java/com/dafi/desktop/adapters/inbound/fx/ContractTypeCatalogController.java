package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.contracttype.ContractTypeCatalogRepositoryPort;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import com.dafi.desktop.application.contracttype.GetContractTypesUseCase;
import com.dafi.desktop.domain.contracttype.ContractTypeCatalog;


/**
 * Controller for the contract type catalog view.
 * Concrete {@link AbstractCatalogEntryController} backed by the contract type
 * use case and repository port.
 */
public class ContractTypeCatalogController extends AbstractCatalogEntryController<ContractTypeCatalog> {

    private GetContractTypesUseCase getContractTypesUseCase;
    private ContractTypeCatalogRepositoryPort repositoryPort;
    private KeyToolsHelper keyToolsHelper;

    @FXML
    private Button keyToolsButton;

    @Override
    protected String i18nPrefix() {
        return "contractTypes";
    }

    @Override
    protected java.util.List<ContractTypeCatalog> fetchEntries() {
        return getContractTypesUseCase.getAll();
    }

    @Override
    protected void persistEntry(ContractTypeCatalog entry) {
        repositoryPort.save(entry);
    }

    @Override
    protected ContractTypeCatalog buildEntity(String id, String name, String description) {
        return new ContractTypeCatalog(id, name, description);
    }

    /**
     * Injects the query use case, persistence port and encryption-key
     * transfer use cases, then loads the entries.
     *
     * @param getContractTypesUseCase use case listing contract types
     * @param repositoryPort port persisting contract type changes
     * @param exportEncryptionKeyUseCase use case exporting the encryption key
     * @param importEncryptionKeyUseCase use case importing the encryption key
     */
    public void setDependencies(GetContractTypesUseCase getContractTypesUseCase,
                                ContractTypeCatalogRepositoryPort repositoryPort,
                                com.dafi.desktop.application.security.ExportEncryptionKeyUseCase exportUseCase,
                                com.dafi.desktop.application.security.ImportEncryptionKeyUseCase importUseCase) {
        this.getContractTypesUseCase = getContractTypesUseCase;
        this.repositoryPort = repositoryPort;
        this.keyToolsHelper = new KeyToolsHelper(exportUseCase, importUseCase, keyToolsButton);
        loadData();
    }


}
