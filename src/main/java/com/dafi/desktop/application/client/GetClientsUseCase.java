package com.dafi.desktop.application.client;

import com.dafi.desktop.domain.client.Client;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Caso de uso para obtener y ordenar clientes.
 */
public class GetClientsUseCase {

    private final ClientRepositoryPort clientRepository;

    public GetClientsUseCase(ClientRepositoryPort clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Obtiene todos los clientes ordenados por fecha de vencimiento.
     * Los más próximos a vencer aparecen primero.
     * Los sin fecha de vencimiento aparecen al final.
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
