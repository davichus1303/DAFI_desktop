package com.dafi.desktop.domain;

import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.domain.client.Client.Builder;
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
        assertEquals(expectedEndDate, client.getContractEndDate());
    }

    @Test
    void testCalculateContractEndDateWithDifferentMonths() {
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        Client client = createClient(startDate, 24);

        LocalDate expectedEndDate = LocalDate.of(2026, 6, 1);
        assertEquals(expectedEndDate, client.getContractEndDate());
    }

    @Test
    void testContractEndDateComputedWhenNotProvided() {
        Client client = Client.builder()
                .id("id-1")
                .contractFolio("F-1")
                .fullName("Cliente de Prueba")
                .contractDate(LocalDate.of(2024, 3, 15))
                .firstPaymentDate(LocalDate.of(2024, 4, 5))
                .paymentDay(5)
                .totalPayments(18)
                .build();

        assertEquals(LocalDate.of(2025, 9, 15), client.getContractEndDate());
    }

    @Test
    void testContractEndDateKeptWhenProvided() {
        LocalDate explicitEndDate = LocalDate.of(2030, 1, 1);
        Client client = Client.builder()
                .id("id-2")
                .contractFolio("F-2")
                .fullName("Cliente de Prueba")
                .contractDate(LocalDate.of(2024, 3, 15))
                .firstPaymentDate(LocalDate.of(2024, 4, 5))
                .paymentDay(5)
                .totalPayments(12)
                .contractEndDate(explicitEndDate)
                .build();

        assertEquals(explicitEndDate, client.getContractEndDate());
    }

    @Test
    void testClientsOrderedByContractEndDate() {
        Client client1 = createClient(LocalDate.of(2024, 12, 1), 6);
        Client client2 = createClient(LocalDate.of(2024, 1, 1), 12);
        Client client3 = createClient(LocalDate.of(2024, 6, 1), 3);

        List<Client> clients = Arrays.asList(client1, client2, client3);

        List<Client> sorted = clients.stream()
                .sorted(Comparator.comparing(Client::getContractEndDate))
                .toList();

        assertEquals(client3, sorted.get(0));
        assertEquals(client2, sorted.get(1));
        assertEquals(client1, sorted.get(2));
    }

    @Test
    void testClientEqualityById() {
        Client client1 = createClient(LocalDate.of(2024, 1, 1), 12);
        Client client2 = buildTestClient(
                "different-id",
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 1, 1),
                6,
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

    @Test
    void testBuildRejectsBlankRequiredFields() {
        assertThrows(DomainException.class, () ->
                validBuilder().id("").build());
        assertThrows(DomainException.class, () ->
                validBuilder().contractFolio("  ").build());
        assertThrows(DomainException.class, () ->
                validBuilder().fullName(null).build());
    }

    @Test
    void testBuildRejectsMissingDates() {
        assertThrows(DomainException.class, () ->
                validBuilder().contractDate(null).build());
        assertThrows(DomainException.class, () ->
                validBuilder().firstPaymentDate(null).build());
    }

    @Test
    void testBuildRejectsInvalidPaymentConfiguration() {
        assertThrows(DomainException.class, () ->
                validBuilder().totalPayments(0).build());
        assertThrows(DomainException.class, () ->
                validBuilder().paymentDay(0).build());
        assertThrows(DomainException.class, () ->
                validBuilder().paymentDay(32).build());
    }

    @Test
    void testBuildRejectsNegativeAmounts() {
        assertThrows(DomainException.class, () ->
                validBuilder().totalBalance(new BigDecimal("-1")).build());
        assertThrows(DomainException.class, () ->
                validBuilder().monthlyPayment(new BigDecimal("-0.01")).build());
    }

    @Test
    void testBuildRejectsMalformedEmail() {
        assertThrows(DomainException.class, () ->
                validBuilder().email("not-an-email").build());
    }

    @Test
    void testBuildAcceptsBlankEmailAndZeroAmounts() {
        Client client = validBuilder()
                .email("")
                .advance(BigDecimal.ZERO)
                .build();

        assertEquals("", client.getEmail());
    }

    private Builder validBuilder() {
        return Client.builder()
                .id("test-id")
                .contractFolio("FOL-001")
                .fullName("Test Client")
                .firstPaymentDate(LocalDate.of(2024, 1, 1))
                .contractDate(LocalDate.of(2024, 1, 1))
                .paymentDay(15)
                .totalPayments(12)
                .totalBalance(new BigDecimal("6000"))
                .monthlyPayment(new BigDecimal("500"));
    }

    private Client createClient(LocalDate startDate, int totalPayments) {
        return buildTestClient(
                "test-id",
                startDate, startDate,
                totalPayments,
                startDate.plusMonths(totalPayments)
        );
    }

    private Client buildTestClient(String id, LocalDate firstPaymentDate, LocalDate contractDate,
                                   int totalPayments, LocalDate contractEndDate) {
        return Client.builder()
                .id(id)
                .contractFolio("FOL-001")
                .fullName("Test Client")
                .ine("INE-TEST")
                .contractType("Estándar")
                .address("Domicilio Test")
                .neighborhood("Colonia Test")
                .phone("5551234567")
                .email("test@email.com")
                .paymentMethod("Efectivo")
                .firstBeneficiary("Beneficiario 1")
                .secondBeneficiary("Beneficiario 2")
                .saleDescription("Venta test")
                .annuity("2024")
                .block("Mza-1")
                .lot("Lote-1")
                .managementFee(new BigDecimal("1000"))
                .advance(new BigDecimal("500"))
                .totalBalance(new BigDecimal("6000"))
                .firstPaymentDate(firstPaymentDate)
                .contractDate(contractDate)
                .paymentDay(15)
                .totalPayments(totalPayments)
                .monthlyPayment(new BigDecimal("500"))
                .contractEndDate(contractEndDate)
                .build();
    }
}
