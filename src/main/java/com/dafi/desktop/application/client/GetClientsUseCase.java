package com.dafi.desktop.application.client;

import com.dafi.desktop.domain.client.Client;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Use case that retrieves clients from the repository in a business-defined order.
 */
public class GetClientsUseCase {

    private final ClientRepositoryPort clientRepository;

    /**
     * Creates the use case.
     *
     * @param clientRepository client persistence port
     */
    public GetClientsUseCase(ClientRepositoryPort clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Returns all clients sorted by contract end date; contracts expiring
     * sooner come first and clients without an end date go last.
     *
     * @return ordered list of clients (never {@code null})
     */
    public List<Client> getClientsOrderedByContractEndDate() {
        return clientRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        Client::getContractEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
