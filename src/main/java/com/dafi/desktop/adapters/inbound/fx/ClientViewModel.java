package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.domain.client.Client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClientViewModel {

    private final String id;
    private final String contractFolio;
    private final String fullName;
    private final String contractType;
    private final String phone;
    private final String neighborhood;
    private final String paymentMethod;
    private final LocalDate contractDate;
    private final LocalDate contractEndDate;
    private final int totalPayments;
    private final BigDecimal monthlyPayment;
    private final BigDecimal totalBalance;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static ClientViewModel fromClient(Client client) {
        return new ClientViewModel(
                client.getId(),
                client.getContractFolio(),
                client.getFullName(),
                client.getContractType() != null ? client.getContractType().getDisplayName() : "",
                client.getPhone(),
                client.getNeighborhood(),
                client.getPaymentMethod() != null ? client.getPaymentMethod().getDisplayName() : "",
                client.getContractDate(),
                client.getContractEndDate(),
                client.getTotalPayments(),
                client.getMonthlyPayment(),
                client.getTotalBalance()
        );
    }

    private ClientViewModel(String id, String contractFolio, String fullName,
                           String contractType, String phone, String neighborhood,
                           String paymentMethod, LocalDate contractDate,
                           LocalDate contractEndDate, int totalPayments,
                           BigDecimal monthlyPayment, BigDecimal totalBalance) {
        this.id = id;
        this.contractFolio = contractFolio;
        this.fullName = fullName;
        this.contractType = contractType;
        this.phone = phone;
        this.neighborhood = neighborhood;
        this.paymentMethod = paymentMethod;
        this.contractDate = contractDate;
        this.contractEndDate = contractEndDate;
        this.totalPayments = totalPayments;
        this.monthlyPayment = monthlyPayment;
        this.totalBalance = totalBalance;
    }

    public String getId() { return id; }
    public String getContractFolio() { return contractFolio; }
    public String getFullName() { return fullName; }
    public String getContractType() { return contractType; }
    public String getPhone() { return phone; }
    public String getNeighborhood() { return neighborhood; }
    public String getPaymentMethod() { return paymentMethod; }
    public int getTotalPayments() { return totalPayments; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public BigDecimal getTotalBalance() { return totalBalance; }

    public String getContractDateFormatted() {
        return contractDate != null ? contractDate.format(DATE_FORMATTER) : "";
    }

    public String getContractEndDateFormatted() {
        return contractEndDate != null ? contractEndDate.format(DATE_FORMATTER) : "";
    }
}
