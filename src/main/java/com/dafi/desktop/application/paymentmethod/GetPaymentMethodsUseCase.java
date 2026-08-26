package com.dafi.desktop.application.paymentmethod;

import com.dafi.desktop.domain.paymentmethod.PaymentMethodCatalog;
import java.util.List;

/**
 * Use case that retrieves payment method entries from the catalog.
 */
public class GetPaymentMethodsUseCase {

    private final PaymentMethodCatalogRepositoryPort repository;

    /**
     * Creates the use case.
     *
     * @param repository payment method catalog port
     */
    public GetPaymentMethodsUseCase(PaymentMethodCatalogRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Returns all payment methods currently stored in the catalog.
     *
     * @return list of payment methods (never {@code null})
     */
    public List<PaymentMethodCatalog> getAll() {
        return repository.findAll();
    }
}
