package com.dafi.desktop.domain;

import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.domain.client.ContractType;
import com.dafi.desktop.domain.client.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void testCalculateContractEndDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        Client client = createClient(startDate, 12);

        LocalDate expectedEndDate = LocalDate.of(2025, 1, 15);
        assertEquals(expectedEndDate, client.calculateContractEndDate());
    }

    @Test
    void testCalculateContractEndDateWithDifferentMonths() {
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        Client client = createClient(startDate, 24);

        LocalDate expectedEndDate = LocalDate.of(2026, 6, 1);
        assertEquals(expectedEndDate, client.calculateContractEndDate());
    }

    @Test
    void testClientsOrderedByContractEndDate() {
        Client client1 = createClient(LocalDate.of(2024, 12, 1), 6);
        Client client2 = createClient(LocalDate.of(2024, 1, 1), 12);
        Client client3 = createClient(LocalDate.of(2024, 6, 1), 3);

        List<Client> clients = Arrays.asList(client1, client2, client3);

        List<Client> sorted = clients.stream()
                .sorted(Comparator.comparing(Client::calculateContractEndDate))
                .toList();

        assertEquals(client3, sorted.get(0));
        assertEquals(client2, sorted.get(1));
        assertEquals(client1, sorted.get(2));
    }

    @Test
    void testClientEqualityById() {
        Client client1 = createClient(LocalDate.of(2024, 1, 1), 12);
        Client client2 = new Client(
                "different-id",
                "FOL-001", "Test Client", "INE-TEST",
                ContractType.STANDARD, "Domicilio Test", "Colonia Test",
                "5551234567", "test@email.com", PaymentMethod.CASH,
                "Beneficiario 1", "Beneficiario 2", "Venta test",
                "2024", "Mza-1", "Lote-1",
                new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("6000"),
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 1, 1),
                15, 6, new BigDecimal("500"),
                LocalDate.of(2024, 7, 1)
        );

        assertEquals(client1, client1);
        assertNotEquals(client1, client2);
    }

    @Test
    void testClientHashCode() {
        Client client1 = createClient(LocalDate.of(2024, 1, 1), 12);
        Client client2 = createClient(LocalDate.of(2024, 1, 1), 12);

        assertEquals(client1.hashCode(), client2.hashCode());
    }

    private Client createClient(LocalDate startDate, int totalPayments) {
        return new Client(
                "test-id",
                "FOL-001", "Test Client", "INE-TEST",
                ContractType.STANDARD, "Domicilio Test", "Colonia Test",
                "5551234567", "test@email.com", PaymentMethod.CASH,
                "Beneficiario 1", "Beneficiario 2", "Venta test",
                "2024", "Mza-1", "Lote-1",
                new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("6000"),
                startDate, startDate,
                15, totalPayments, new BigDecimal("500"),
                startDate.plusMonths(totalPayments)
        );
    }
}
