package com.dafi.desktop.domain.client;

import com.dafi.desktop.domain.DomainException;
import com.dafi.desktop.domain.shared.Email;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable entity that represents a client of the funeral services firm.
 * Instances can only be created through the nested {@link Builder}.
 */
public class Client {

    private final String id;
    private final String contractFolio;
    private final String fullName;
    private final String ine;
    private final String contractType;
    private final String address;
    private final String neighborhood;
    private final String phone;
    private final String email;
    private final String paymentMethod;
    private final String firstBeneficiary;
    private final String secondBeneficiary;
    private final String saleDescription;
    private final String annuity;
    private final String block;
    private final String lot;
    private final BigDecimal managementFee;
    private final BigDecimal advance;
    private final BigDecimal totalBalance;
    private final LocalDate firstPaymentDate;
    private final LocalDate contractDate;
    private final int paymentDay;
    private final int totalPayments;
    private final BigDecimal monthlyPayment;
    private final LocalDate contractEndDate;

    /**
     * Creates a Client from a validated Builder.
     *
     * @param builder builder whose invariants have already been checked
     */
    private Client(Builder builder) {
        this.id = builder.id;
        this.contractFolio = builder.contractFolio;
        this.fullName = builder.fullName;
        this.ine = builder.ine;
        this.contractType = builder.contractType;
        this.address = builder.address;
        this.neighborhood = builder.neighborhood;
        this.phone = builder.phone;
        this.email = builder.email;
        this.paymentMethod = builder.paymentMethod;
        this.firstBeneficiary = builder.firstBeneficiary;
        this.secondBeneficiary = builder.secondBeneficiary;
        this.saleDescription = builder.saleDescription;
        this.annuity = builder.annuity;
        this.block = builder.block;
        this.lot = builder.lot;
        this.managementFee = builder.managementFee;
        this.advance = builder.advance;
        this.totalBalance = builder.totalBalance;
        this.firstPaymentDate = builder.firstPaymentDate;
        this.contractDate = builder.contractDate;
        this.paymentDay = builder.paymentDay;
        this.totalPayments = builder.totalPayments;
        this.monthlyPayment = builder.monthlyPayment;
        this.contractEndDate = builder.contractEndDate != null
                ? builder.contractEndDate
                : calculateContractEndDate(builder.contractDate, builder.totalPayments);
    }

    /**
     * Creates a new empty builder for {@link Client} instances.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Calculates the date on which a contract ends: the contract date plus
     * the total number of monthly payments.
     *
     * @param contractDate  date the contract was signed
     * @param totalPayments number of monthly payments in the contract
     * @return the computed contract end date, or {@code null} when no
     *         contract date is provided
     */
    public static LocalDate calculateContractEndDate(LocalDate contractDate, int totalPayments) {
        if (contractDate == null) {
            return null;
        }
        return contractDate.plusMonths(totalPayments);
    }

    /** Unique identifier of this client (UUID). */
    public String getId() { return id; }

    /** Contract folio number assigned by the firm. */
    public String getContractFolio() { return contractFolio; }

    /** Full name of the client. */
    public String getFullName() { return fullName; }

    /** INE (Mexican voter ID) code, may be blank. */
    public String getIne() { return ine; }

    /** Name of the contract type from the catalog. */
    public String getContractType() { return contractType; }

    /** Street address of the client. */
    public String getAddress() { return address; }

    /** Neighborhood (colonia) of the client. */
    public String getNeighborhood() { return neighborhood; }

    /** Phone number of the client. */
    public String getPhone() { return phone; }

    /** Email address of the client, may be blank. */
    public String getEmail() { return email; }

    /** Name of the payment method from the catalog. */
    public String getPaymentMethod() { return paymentMethod; }

    /** Name of the first beneficiary of the contract. */
    public String getFirstBeneficiary() { return firstBeneficiary; }

    /** Name of the second beneficiary of the contract, may be blank. */
    public String getSecondBeneficiary() { return secondBeneficiary; }

    /** Description of the sale or service provided. */
    public String getSaleDescription() { return saleDescription; }

    /** Annuity year or period associated with the contract. */
    public String getAnnuity() { return annuity; }

    /** Block (manzana) within the cemetery or memorial park. */
    public String getBlock() { return block; }

    /** Lot number within the block. */
    public String getLot() { return lot; }

    /** Management fee charged for the contract. */
    public BigDecimal getManagementFee() { return managementFee; }

    /** Advance payment made by the client. */
    public BigDecimal getAdvance() { return advance; }

    /** Total balance remaining on the contract. */
    public BigDecimal getTotalBalance() { return totalBalance; }

    /** Date of the first scheduled payment. */
    public LocalDate getFirstPaymentDate() { return firstPaymentDate; }

    /** Date the contract was signed. */
    public LocalDate getContractDate() { return contractDate; }

    /** Day of the month when payments are due (1-31). */
    public int getPaymentDay() { return paymentDay; }

    /** Total number of monthly payments in the contract. */
    public int getTotalPayments() { return totalPayments; }

    /** Amount of each monthly payment. */
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }

    /** Date when the contract ends (computed or explicitly provided). */
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

    /**
     * Builder used to construct immutable {@link Client} value objects.
     * Each configuration method returns the same builder instance, enabling
     * fluent construction before {@link #build()} produces the final
     * immutable Client.
     * <p>
     * {@code build()} enforces the business invariants of the entity and
     * throws {@link DomainException} when any of them is violated, so no
     * invalid Client can exist at runtime regardless of the entry point
     * (UI form, CSV importer, repository loader, tests).
     */
    public static class Builder {

        private String id;
        private String contractFolio;
        private String fullName;
        private String ine;
        private String contractType;
        private String address;
        private String neighborhood;
        private String phone;
        private String email;
        private String paymentMethod;
        private String firstBeneficiary;
        private String secondBeneficiary;
        private String saleDescription;
        private String annuity;
        private String block;
        private String lot;
        private BigDecimal managementFee = BigDecimal.ZERO;
        private BigDecimal advance = BigDecimal.ZERO;
        private BigDecimal totalBalance = BigDecimal.ZERO;
        private LocalDate firstPaymentDate;
        private LocalDate contractDate;
        private int paymentDay;
        private int totalPayments;
        private BigDecimal monthlyPayment = BigDecimal.ZERO;
        private LocalDate contractEndDate;

        /** Sets the unique identifier (UUID). Required. */
        public Builder id(String id) { this.id = id; return this; }

        /** Sets the contract folio number. Required. */
        public Builder contractFolio(String value) { this.contractFolio = value; return this; }

        /** Sets the client's full name. Required. */
        public Builder fullName(String value) { this.fullName = value; return this; }

        /** Sets the INE (voter ID) code. Optional. */
        public Builder ine(String value) { this.ine = value; return this; }

        /** Sets the contract type name. Optional. */
        public Builder contractType(String value) { this.contractType = value; return this; }

        /** Sets the street address. Optional. */
        public Builder address(String value) { this.address = value; return this; }

        /** Sets the neighborhood (colonia). Optional. */
        public Builder neighborhood(String value) { this.neighborhood = value; return this; }

        /** Sets the phone number. Optional. */
        public Builder phone(String value) { this.phone = value; return this; }

        /** Sets the email address. Optional; validated as RFC 5322 format if provided. */
        public Builder email(String value) { this.email = value; return this; }

        /** Sets the payment method name. Optional. */
        public Builder paymentMethod(String value) { this.paymentMethod = value; return this; }

        /** Sets the first beneficiary's name. Optional. */
        public Builder firstBeneficiary(String value) { this.firstBeneficiary = value; return this; }

        /** Sets the second beneficiary's name. Optional. */
        public Builder secondBeneficiary(String value) { this.secondBeneficiary = value; return this; }

        /** Sets the sale/service description. Optional. */
        public Builder saleDescription(String value) { this.saleDescription = value; return this; }

        /** Sets the annuity year or period. Optional. */
        public Builder annuity(String value) { this.annuity = value; return this; }

        /** Sets the block (manzana) within the cemetery. Optional. */
        public Builder block(String value) { this.block = value; return this; }

        /** Sets the lot number within the block. Optional. */
        public Builder lot(String value) { this.lot = value; return this; }

        /** Sets the management fee. Must not be negative; defaults to zero. */
        public Builder managementFee(BigDecimal value) { this.managementFee = value; return this; }

        /** Sets the advance payment. Must not be negative; defaults to zero. */
        public Builder advance(BigDecimal value) { this.advance = value; return this; }

        /** Sets the total balance remaining. Must not be negative; defaults to zero. */
        public Builder totalBalance(BigDecimal value) { this.totalBalance = value; return this; }

        /** Sets the first payment date. Required. */
        public Builder firstPaymentDate(LocalDate value) { this.firstPaymentDate = value; return this; }

        /** Sets the contract signing date. Required. */
        public Builder contractDate(LocalDate value) { this.contractDate = value; return this; }

        /** Sets the day of the month for payments (1-31). Required. */
        public Builder paymentDay(int value) { this.paymentDay = value; return this; }

        /** Sets the total number of monthly payments. Must be >= 1. Required. */
        public Builder totalPayments(int value) { this.totalPayments = value; return this; }

        /** Sets the monthly payment amount. Must not be negative; defaults to zero. */
        public Builder monthlyPayment(BigDecimal value) { this.monthlyPayment = value; return this; }

        /** Sets an explicit contract end date. If null, computed from contractDate + totalPayments. */
        public Builder contractEndDate(LocalDate value) { this.contractEndDate = value; return this; }

        /**
         * Validates the business invariants and creates the immutable Client.
         *
         * @return a fully valid, immutable Client
         * @throws DomainException if any invariant is violated (missing
         *                         required fields, negative amounts, out-of-range
         *                        payment day, malformed e-mail, etc.)
         */
        public Client build() {
            validate();
            return new Client(this);
        }

        private void validate() {
            requireNonBlank(id, "El id es obligatorio");
            requireNonBlank(contractFolio, "El folio de contrato es obligatorio");
            requireNonBlank(fullName, "El nombre completo es obligatorio");

            if (contractDate == null) {
                throw new DomainException("La fecha de contratación es obligatoria");
            }
            if (firstPaymentDate == null) {
                throw new DomainException("La fecha del primer pago es obligatoria");
            }
            if (totalPayments < 1) {
                throw new DomainException("El número de mensualidades debe ser mayor a cero");
            }
            if (paymentDay < 1 || paymentDay > 31) {
                throw new DomainException("El día de pago debe estar entre 1 y 31");
            }

            requireNonNegative(managementFee, "El gasto de gestión");
            requireNonNegative(advance, "El anticipo");
            requireNonNegative(totalBalance, "El saldo total");
            requireNonNegative(monthlyPayment, "La mensualidad");

            if (email != null && !email.isBlank()) {
                try {
                    new Email(email);
                } catch (IllegalArgumentException e) {
                    throw new DomainException(e.getMessage());
                }
            }
        }

        private static void requireNonBlank(String value, String fieldLabel) {
            if (value == null || value.isBlank()) {
                throw new DomainException(fieldLabel);
            }
        }

        private static void requireNonNegative(BigDecimal value, String fieldLabel) {
            if (value == null || value.signum() < 0) {
                throw new DomainException(fieldLabel + " no puede ser negativo");
            }
        }
    }
}
