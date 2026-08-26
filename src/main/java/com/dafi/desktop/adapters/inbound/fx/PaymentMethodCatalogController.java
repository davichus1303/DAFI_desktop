package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.paymentmethod.GetPaymentMethodsUseCase;
import com.dafi.desktop.application.paymentmethod.PaymentMethodCatalogRepositoryPort;
import com.dafi.desktop.domain.paymentmethod.PaymentMethodCatalog;

import java.util.List;

/**
 * Controller for the payment method catalog view.
 * Concrete {@link AbstractCatalogEntryController} backed by the payment method
 * use case and repository port.
 */
public class PaymentMethodCatalogController extends AbstractCatalogEntryController<PaymentMethodCatalog> {

    private GetPaymentMethodsUseCase getPaymentMethodsUseCase;
    private PaymentMethodCatalogRepositoryPort repositoryPort;

    @Override
    protected String i18nPrefix() {
        return "paymentTypes";
    }

    @Override
    protected List<PaymentMethodCatalog> fetchEntries() {
        return getPaymentMethodsUseCase.getAll();
    }

    @Override
    protected void persistEntry(PaymentMethodCatalog entry) {
        repositoryPort.save(entry);
    }

    @Override
    protected PaymentMethodCatalog buildEntity(String id, String name, String description) {
        return new PaymentMethodCatalog(id, name, description);
    }

    /**
     * Injects the query use case and persistence port, then loads the entries.
     *
     * @param getPaymentMethodsUseCase use case listing payment methods
     * @param repositoryPort port persisting payment method changes
     */
    public void setDependencies(GetPaymentMethodsUseCase getPaymentMethodsUseCase,
                                PaymentMethodCatalogRepositoryPort repositoryPort) {
        this.getPaymentMethodsUseCase = getPaymentMethodsUseCase;
        this.repositoryPort = repositoryPort;
        loadData();
    }
}
