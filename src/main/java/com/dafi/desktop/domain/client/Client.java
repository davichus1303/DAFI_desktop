package com.dafi.desktop.domain.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa un cliente del despacho funerario.
 */
public class Client {

    private String id;
    private String contractFolio;
    private String fullName;
    private String ine;
    private ContractType contractType;
    private String address;
    private String neighborhood;
    private String phone;
    private String email;
    private PaymentMethod paymentMethod;
    private String firstBeneficiary;
    private String secondBeneficiary;
    private String saleDescription;
    private String annuality;
    private String block;
    private String lot;
    private BigDecimal managementFee;
    private BigDecimal advance;
    private BigDecimal totalBalance;
    private LocalDate firstPaymentDate;
    private LocalDate contractDate;
    private int paymentDay;
    private int totalPayments;
    private BigDecimal monthlyPayment;
    private LocalDate contractEndDate;

    public Client() {
    }

    public Client(String id, String contractFolio, String fullName, String ine,
                  ContractType contractType, String address, String neighborhood,
                  String phone, String email, PaymentMethod paymentMethod,
                  String firstBeneficiary, String secondBeneficiary,
                  String saleDescription, String annuality,
                  String block, String lot, BigDecimal managementFee, BigDecimal advance,
                  BigDecimal totalBalance, LocalDate firstPaymentDate, LocalDate contractDate,
                  int paymentDay, int totalPayments, BigDecimal monthlyPayment,
                  LocalDate contractEndDate) {
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        this.contractFolio = contractFolio;
        this.fullName = fullName;
        this.ine = ine;
        this.contractType = contractType;
        this.address = address;
        this.neighborhood = neighborhood;
        this.phone = phone;
        this.email = email;
        this.paymentMethod = paymentMethod;
        this.firstBeneficiary = firstBeneficiary;
        this.secondBeneficiary = secondBeneficiary;
        this.saleDescription = saleDescription;
        this.annuality = annuality;
        this.block = block;
        this.lot = lot;
        this.managementFee = managementFee;
        this.advance = advance;
        this.totalBalance = totalBalance;
        this.firstPaymentDate = firstPaymentDate;
        this.contractDate = contractDate;
        this.paymentDay = paymentDay;
        this.totalPayments = totalPayments;
        this.monthlyPayment = monthlyPayment;
        this.contractEndDate = contractEndDate;
    }

    public LocalDate calculateContractEndDate() {
        return contractDate.plusMonths(totalPayments);
    }

    public String getId() { return id; }
    public String getContractFolio() { return contractFolio; }
    public String getFullName() { return fullName; }
    public String getIne() { return ine; }
    public ContractType getContractType() { return contractType; }
    public String getAddress() { return address; }
    public String getNeighborhood() { return neighborhood; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getFirstBeneficiary() { return firstBeneficiary; }
    public String getSecondBeneficiary() { return secondBeneficiary; }
    public String getSaleDescription() { return saleDescription; }
    public String getAnnuity() { return annuality; }
    public String getBlock() { return block; }
    public String getLot() { return lot; }
    public BigDecimal getManagementFee() { return managementFee; }
    public BigDecimal getAdvance() { return advance; }
    public BigDecimal getTotalBalance() { return totalBalance; }
    public LocalDate getFirstPaymentDate() { return firstPaymentDate; }
    public LocalDate getContractDate() { return contractDate; }
    public int getPaymentDay() { return paymentDay; }
    public int getTotalPayments() { return totalPayments; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public LocalDate getContractEndDate() { return contractEndDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Client{id='" + id + "', folio='" + contractFolio + "', name='" + fullName + "'}";
    }
}
