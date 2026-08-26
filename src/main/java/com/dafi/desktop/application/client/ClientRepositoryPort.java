package com.dafi.desktop.application.client;

import com.dafi.desktop.domain.client.Client;
import java.util.List;

/**
 * Output port for client persistence, implemented by outbound adapters.
 */
public interface ClientRepositoryPort {

    /**
     * Retrieves all stored clients.
     *
     * @return list of clients (never {@code null})
     */
    List<Client> findAll();

    /**
     * Inserts or updates a single client, matched by its identifier.
     *
     * @param client client to persist
     */
    void save(Client client);

    /**
     * Persists all the given clients.
     *
     * @param clients list of clients to persist
     */
    void saveAll(List<Client> clients);
}
