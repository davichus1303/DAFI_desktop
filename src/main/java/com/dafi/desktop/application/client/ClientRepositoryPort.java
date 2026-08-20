package com.dafi.desktop.application.client;

import com.dafi.desktop.domain.client.Client;
import java.util.List;

/**
 * Puerto de salida para la persistencia de clientes.
 */
public interface ClientRepositoryPort {

    /**
     * Obtiene todos los clientes almacenados.
     *
     * @return lista de clientes
     */
    List<Client> findAll();

    /**
     * Guarda un cliente.
     *
     * @param client cliente a guardar
     */
    void save(Client client);

    /**
     * Guarda todos los clientes proporcionados.
     *
     * @param clients lista de clientes a guardar
     */
    void saveAll(List<Client> clients);
}
