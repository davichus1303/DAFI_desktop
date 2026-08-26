package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.paymentmethod.PaymentMethodCatalogRepositoryPort;
import com.dafi.desktop.domain.paymentmethod.PaymentMethodCatalog;
import com.dafi.desktop.adapters.outbound.CryptoUtils;

import java.nio.file.Path;

/**
 * Thin outbound adapter implementing {@link PaymentMethodCatalogRepositoryPort};
 * it persists payment method catalog entries as an AES-GCM encrypted JSON file
 * (paymentMethods.json) in the application data directory (typically ~/.dafi/data).
 * Serialization is inherited from {@link AbstractJsonCatalogRepositoryAdapter}.
 */
public class JsonPaymentMethodCatalogRepositoryAdapter
        extends AbstractJsonCatalogRepositoryAdapter<PaymentMethodCatalog>
        implements PaymentMethodCatalogRepositoryPort {

    private static final String ARRAY_KEY = "paymentMethods";
    private static final String FILE_NAME = "paymentMethods.json";

    public JsonPaymentMethodCatalogRepositoryAdapter(Path dataDirectory, CryptoUtils cryptoUtils) {
        super(dataDirectory, cryptoUtils, ARRAY_KEY, FILE_NAME);
    }

    @Override
    protected PaymentMethodCatalog buildEntry(String id, String name, String description) {
        return new PaymentMethodCatalog(id, name, description);
    }
}
