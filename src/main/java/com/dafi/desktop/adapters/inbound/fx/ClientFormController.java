package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.contracttype.GetContractTypesUseCase;
import com.dafi.desktop.application.paymentmethod.GetPaymentMethodsUseCase;
import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.domain.contracttype.ContractTypeCatalog;
import com.dafi.desktop.domain.paymentmethod.PaymentMethodCatalog;
import com.dafi.desktop.infrastructure.I18n;
import com.dafi.desktop.domain.client.Client;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Controller for the client create/edit form view.
 * Owns only the form; the container view (table, search, navigation) lives in
 * {@link ClientsController}, which injects the collaboration callbacks below.
 */
public class ClientFormController {

    private static final Logger log = LoggerFactory.getLogger(ClientFormController.class);

    @FXML private TextField folioField;
    @FXML private TextField fullNameField;
    @FXML private TextField ineField;
    @FXML private ComboBox<String> contractTypeCombo;
    @FXML private TextField addressField;
    @FXML private TextField neighborhoodField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField firstBeneficiaryField;
    @FXML private TextField secondBeneficiaryField;
    @FXML private TextField saleDescriptionField;
    @FXML private TextField annuityField;
    @FXML private TextField blockField;
    @FXML private TextField lotField;
    @FXML private TextField managementFeeField;
    @FXML private TextField advanceField;
    @FXML private TextField totalBalanceField;
    @FXML private DatePicker firstPaymentDatePicker;
    @FXML private DatePicker contractDatePicker;
    @FXML private TextField paymentDayField;
    @FXML private TextField totalPaymentsField;
    @FXML private TextField monthlyPaymentField;
    @FXML private Button saveButton;
    @FXML private Button editButton;
    @FXML private Label formTitleLabel;
    @FXML private Label formErrorLabel;
    @FXML private Label formSuccessLabel;

    @FXML private Label labelFolio;
    @FXML private Label labelFullName;
    @FXML private Label labelIne;
    @FXML private Label labelContractType;
    @FXML private Label labelAddress;
    @FXML private Label labelNeighborhood;
    @FXML private Label labelPhone;
    @FXML private Label labelEmail;
    @FXML private Label labelPaymentMethod;
    @FXML private Label labelFirstBeneficiary;
    @FXML private Label labelSecondBeneficiary;
    @FXML private Label labelSaleDescription;
    @FXML private Label labelAnnuity;
    @FXML private Label labelBlock;
    @FXML private Label labelLot;
    @FXML private Label labelManagementFee;
    @FXML private Label labelAdvance;
    @FXML private Label labelTotalBalance;
    @FXML private Label labelFirstPaymentDate;
    @FXML private Label labelContractDate;
    @FXML private Label labelPaymentDay;
    @FXML private Label labelTotalPayments;
    @FXML private Label labelMonthlyPayment;

    @FXML private HBox managementFeeBox;
    @FXML private HBox advanceBox;
    @FXML private HBox totalBalanceBox;
    @FXML private HBox monthlyPaymentBox;

    private Consumer<Client> clientSaver = client -> { };
    private Runnable formCloseHandler = () -> { };
    private BiPredicate<String, String> duplicateFolioChecker = (folio, excludeClientId) -> false;
    private String editingClientId = null;

    /**
     * Applies i18n texts, defaults, validation bindings and input filters after FXML loading.
     */
    @FXML
    public void initialize() {
        applyFormFieldLabelTexts();
        applyActionButtonTexts();
        setDefaultFormValues();
        bindFormValidation();
        applyNumericInputFilters();
        addCurrencyPrefixesToAmountFields();
    }

    private void applyFormFieldLabelTexts() {
        I18n i18n = I18n.getInstance();
        labelFolio.setText(i18n.get("form.folio"));
        labelFullName.setText(i18n.get("form.fullName"));
        labelIne.setText(i18n.get("form.ine"));
        labelContractType.setText(i18n.get("form.contractType"));
        labelAddress.setText(i18n.get("form.address"));
        labelNeighborhood.setText(i18n.get("form.neighborhood"));
        labelPhone.setText(i18n.get("form.phone"));
        labelEmail.setText(i18n.get("form.email"));
        labelPaymentMethod.setText(i18n.get("form.paymentMethod"));
        labelFirstBeneficiary.setText(i18n.get("form.firstBeneficiary"));
        labelSecondBeneficiary.setText(i18n.get("form.secondBeneficiary"));
        labelSaleDescription.setText(i18n.get("form.saleDescription"));
        labelAnnuity.setText(i18n.get("form.annuity"));
        labelBlock.setText(i18n.get("form.block"));
        labelLot.setText(i18n.get("form.lot"));
        labelManagementFee.setText(i18n.get("form.managementFee"));
        labelAdvance.setText(i18n.get("form.advance"));
        labelTotalBalance.setText(i18n.get("form.totalBalance"));
        labelFirstPaymentDate.setText(i18n.get("form.firstPaymentDate"));
        labelContractDate.setText(i18n.get("form.contractDate"));
        labelPaymentDay.setText(i18n.get("form.paymentDay"));
        labelTotalPayments.setText(i18n.get("form.totalPayments"));
        labelMonthlyPayment.setText(i18n.get("form.monthlyPayment"));
    }

    private void applyActionButtonTexts() {
        I18n i18n = I18n.getInstance();
        saveButton.setText(i18n.get("form.save"));
        editButton.setText(i18n.get("form.edit"));
        formTitleLabel.setText(i18n.get("form.title.new"));
    }

    private void setDefaultFormValues() {
        contractDatePicker.setValue(LocalDate.now());
    }

    /**
     * Loads contract type and payment method names into the form combos.
     *
     * @param contractTypesUseCase source of contract type names
     * @param paymentMethodsUseCase source of payment method names
     */
    public void setCatalogs(GetContractTypesUseCase contractTypesUseCase,
                            GetPaymentMethodsUseCase paymentMethodsUseCase) {
        loadCatalogNames(contractTypesUseCase::getAll, ContractTypeCatalog::getName, contractTypeCombo);
        loadCatalogNames(paymentMethodsUseCase::getAll, PaymentMethodCatalog::getName, paymentMethodCombo);
    }

    /**
     * Injects the callback invoked with the built {@link Client} when the form is saved.
     *
     * @param clientSaver consumer that persists the client
     */
    public void setClientSaver(Consumer<Client> clientSaver) {
        this.clientSaver = clientSaver;
    }

    /**
     * Injects the callback executed after a successful save (typically returning to the table).
     *
     * @param formCloseHandler action to run when the form closes
     */
    public void setFormCloseHandler(Runnable formCloseHandler) {
        this.formCloseHandler = formCloseHandler;
    }

    /**
     * Injects the predicate used to detect duplicate contract folios during
     * validation. It receives the typed folio and the id of the client being
     * edited ({@code null} when creating a new one), so updates do not flag
     * the client's own folio as duplicate.
     *
     * @param duplicateFolioChecker predicate returning true if another client already uses the folio
     */
    public void setDuplicateFolioChecker(BiPredicate<String, String> duplicateFolioChecker) {
        this.duplicateFolioChecker = duplicateFolioChecker;
    }

    private <T> void loadCatalogNames(Supplier<List<T>> loader,
                                      Function<T, String> namer,
                                      ComboBox<String> combo) {
        Task<List<T>> loadTask = new Task<>() {
            @Override
            protected List<T> call() {
                return loader.get();
            }
        };

        loadTask.setOnSucceeded(event -> Platform.runLater(() -> {
            List<String> names = loadTask.getValue().stream()
                    .map(namer)
                    .filter(name -> name != null && !name.isBlank())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            String previous = combo.getValue();
            combo.getItems().setAll(names);
            if (previous != null && names.contains(previous)) {
                combo.setValue(previous);
            } else if (!names.isEmpty()) {
                combo.getSelectionModel().selectFirst();
            }
        }));

        loadTask.setOnFailed(event -> log.error("Error al cargar catálogo", loadTask.getException()));

        new Thread(loadTask).start();
    }

    /**
     * Resets the form to create mode: clears fields, enables editing and hides the edit button.
     */
    public void startNewClient() {
        editingClientId = null;
        formTitleLabel.setText(I18n.getInstance().get("form.title.new"));
        clearForm();
        setFormReadOnly(false);
        hideEditButton();
    }

    /**
     * Shows the given client in read-only detail mode and enables the edit button.
     *
     * @param client client to display
     */
    public void showClientDetail(Client client) {
        editingClientId = client.getId();
        formTitleLabel.setText(I18n.getInstance().get("form.title.detail"));
        loadClientIntoForm(client);
        setFormReadOnly(true);
        saveButton.setDisable(true);
        hideFormMessages();
        showEditButton();
    }

    private void hideEditButton() {
        editButton.setVisible(false);
        editButton.setManaged(false);
    }

    private void showEditButton() {
        editButton.setVisible(true);
        editButton.setManaged(true);
    }

    private void loadClientIntoForm(Client client) {
        folioField.setText(client.getContractFolio());
        fullNameField.setText(client.getFullName());
        ineField.setText(client.getIne());
        contractTypeCombo.setValue(client.getContractType());
        addressField.setText(client.getAddress());
        neighborhoodField.setText(client.getNeighborhood());
        phoneField.setText(client.getPhone());
        emailField.setText(client.getEmail());
        paymentMethodCombo.setValue(client.getPaymentMethod());
        firstBeneficiaryField.setText(client.getFirstBeneficiary());
        secondBeneficiaryField.setText(client.getSecondBeneficiary());
        saleDescriptionField.setText(client.getSaleDescription());
        annuityField.setText(client.getAnnuity());
        blockField.setText(client.getBlock());
        lotField.setText(client.getLot());
        managementFeeField.setText(client.getManagementFee().toPlainString());
        advanceField.setText(client.getAdvance().toPlainString());
        totalBalanceField.setText(client.getTotalBalance().toPlainString());
        firstPaymentDatePicker.setValue(client.getFirstPaymentDate());
        contractDatePicker.setValue(client.getContractDate());
        paymentDayField.setText(String.valueOf(client.getPaymentDay()));
        totalPaymentsField.setText(String.valueOf(client.getTotalPayments()));
        monthlyPaymentField.setText(client.getMonthlyPayment().toPlainString());
    }

    private void setFormReadOnly(boolean readOnly) {
        folioField.setDisable(readOnly);
        fullNameField.setDisable(readOnly);
        ineField.setDisable(readOnly);
        contractTypeCombo.setDisable(readOnly);
        addressField.setDisable(readOnly);
        neighborhoodField.setDisable(readOnly);
        phoneField.setDisable(readOnly);
        emailField.setDisable(readOnly);
        paymentMethodCombo.setDisable(readOnly);
        firstBeneficiaryField.setDisable(readOnly);
        secondBeneficiaryField.setDisable(readOnly);
        saleDescriptionField.setDisable(readOnly);
        annuityField.setDisable(readOnly);
        blockField.setDisable(readOnly);
        lotField.setDisable(readOnly);
        managementFeeField.setDisable(readOnly);
        advanceField.setDisable(readOnly);
        totalBalanceField.setDisable(readOnly);
        firstPaymentDatePicker.setDisable(readOnly);
        contractDatePicker.setDisable(readOnly);
        paymentDayField.setDisable(readOnly);
        totalPaymentsField.setDisable(readOnly);
        monthlyPaymentField.setDisable(readOnly);
    }

    @FXML
    private void handleEditClient() {
        formTitleLabel.setText(I18n.getInstance().get("form.title.detail"));
        setFormReadOnly(false);
        hideEditButton();
        validateForm();
    }

    @FXML
    private void handleSaveClient() {
        try {
            Client client = buildClientFromForm();
            clientSaver.accept(client);
            formCloseHandler.run();
        } catch (RuntimeException e) {
            showSaveErrorFeedback(e);
        }
    }

    private Client buildClientFromForm() {
        LocalDate contractDate = contractDatePicker.getValue();
        int totalPayments = Integer.parseInt(totalPaymentsField.getText().trim());
        LocalDate contractEndDate = Client.calculateContractEndDate(contractDate, totalPayments);
        String id = editingClientId != null ? editingClientId : UUID.randomUUID().toString();

        return Client.builder()
                .id(id)
                .contractFolio(folioField.getText().trim())
                .fullName(fullNameField.getText().trim())
                .ine(ineField.getText().trim())
                .contractType(contractTypeCombo.getValue())
                .address(addressField.getText().trim())
                .neighborhood(neighborhoodField.getText().trim())
                .phone(phoneField.getText().trim())
                .email(emailField.getText().trim())
                .paymentMethod(paymentMethodCombo.getValue())
                .firstBeneficiary(firstBeneficiaryField.getText().trim())
                .secondBeneficiary(secondBeneficiaryField.getText().trim())
                .saleDescription(saleDescriptionField.getText().trim())
                .annuity(annuityField.getText().trim())
                .block(blockField.getText().trim())
                .lot(lotField.getText().trim())
                .managementFee(parseBigDecimal(managementFeeField.getText().trim()))
                .advance(parseBigDecimal(advanceField.getText().trim()))
                .totalBalance(parseBigDecimal(totalBalanceField.getText().trim()))
                .firstPaymentDate(firstPaymentDatePicker.getValue())
                .contractDate(contractDate)
                .paymentDay(Integer.parseInt(paymentDayField.getText().trim()))
                .totalPayments(totalPayments)
                .monthlyPayment(parseBigDecimal(monthlyPaymentField.getText().trim()))
                .contractEndDate(contractEndDate)
                .build();
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void showSaveErrorFeedback(RuntimeException e) {
        log.error("Error al guardar cliente", e);
        formErrorLabel.setText(I18n.getInstance().get("form.error.save"));
        formErrorLabel.setVisible(true);
        formSuccessLabel.setVisible(false);
    }

    private void hideFormMessages() {
        formErrorLabel.setVisible(false);
        formSuccessLabel.setVisible(false);
    }

    private void clearForm() {
        folioField.clear();
        fullNameField.clear();
        ineField.clear();
        contractTypeCombo.getSelectionModel().selectFirst();
        addressField.clear();
        neighborhoodField.clear();
        phoneField.clear();
        emailField.clear();
        paymentMethodCombo.getSelectionModel().selectFirst();
        firstBeneficiaryField.clear();
        secondBeneficiaryField.clear();
        saleDescriptionField.clear();
        annuityField.clear();
        blockField.clear();
        lotField.clear();
        managementFeeField.clear();
        advanceField.clear();
        totalBalanceField.clear();
        firstPaymentDatePicker.setValue(null);
        contractDatePicker.setValue(LocalDate.now());
        paymentDayField.clear();
        totalPaymentsField.clear();
        monthlyPaymentField.clear();
        hideFormMessages();
        saveButton.setDisable(true);
    }

    private void bindFormValidation() {
        Runnable validator = this::validateForm;
        bindTextFieldValidators(validator);
        bindSelectorValidators(validator);
        bindAutoCalculationTriggers();
    }

    private void bindTextFieldValidators(Runnable validator) {
        folioField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        fullNameField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        ineField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        addressField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        neighborhoodField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        firstBeneficiaryField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        secondBeneficiaryField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        saleDescriptionField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        annuityField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        blockField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        lotField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        managementFeeField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        advanceField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        totalBalanceField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        paymentDayField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        totalPaymentsField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
        monthlyPaymentField.textProperty().addListener((observable, oldValue, newValue) -> validator.run());
    }

    private void bindSelectorValidators(Runnable validator) {
        firstPaymentDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> validator.run());
        contractDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> validator.run());
        contractTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> validator.run());
        paymentMethodCombo.valueProperty().addListener((observable, oldValue, newValue) -> validator.run());
    }

    private void bindAutoCalculationTriggers() {
        totalBalanceField.textProperty().addListener((observable, oldValue, newValue) -> autoCalculateMonthlyPayment());
        totalPaymentsField.textProperty().addListener((observable, oldValue, newValue) -> autoCalculateMonthlyPayment());
    }

    private void autoCalculateMonthlyPayment() {
        try {
            String balanceText = totalBalanceField.getText().trim();
            String paymentsText = totalPaymentsField.getText().trim();
            if (!balanceText.isEmpty() && !paymentsText.isEmpty()) {
                BigDecimal balance = new BigDecimal(balanceText);
                int payments = Integer.parseInt(paymentsText);
                if (payments > 0) {
                    BigDecimal monthly = balance.divide(BigDecimal.valueOf(payments), 2, RoundingMode.HALF_UP);
                    monthlyPaymentField.setText(monthly.toPlainString());
                    validateForm();
                    return;
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void validateForm() {
        showDuplicateFolioWarningIfAny();
        saveButton.setDisable(!isFormComplete());
    }

    private void showDuplicateFolioWarningIfAny() {
        boolean duplicateFolio = hasDuplicateFolio();
        if (duplicateFolio) {
            formErrorLabel.setText(I18n.getInstance().get("form.error.folio.duplicate"));
        }
        formErrorLabel.setVisible(duplicateFolio);
    }

    private boolean hasDuplicateFolio() {
        String folio = folioField.getText().trim();
        if (folio.isEmpty()) return false;
        return duplicateFolioChecker.test(folio, editingClientId);
    }

    private boolean isFormComplete() {
        return isFolioFilledAndUnique()
                && areRequiredTextFieldsFilled()
                && areCatalogCombosSelected()
                && areDatesSelected()
                && areNumberFieldsValid();
    }

    private boolean isFolioFilledAndUnique() {
        return !folioField.getText().trim().isEmpty() && !hasDuplicateFolio();
    }

    private boolean areRequiredTextFieldsFilled() {
        return !fullNameField.getText().trim().isEmpty()
                && !ineField.getText().trim().isEmpty()
                && !addressField.getText().trim().isEmpty()
                && !neighborhoodField.getText().trim().isEmpty()
                && !phoneField.getText().trim().isEmpty()
                && !firstBeneficiaryField.getText().trim().isEmpty()
                && !secondBeneficiaryField.getText().trim().isEmpty()
                && !saleDescriptionField.getText().trim().isEmpty()
                && !annuityField.getText().trim().isEmpty()
                && !blockField.getText().trim().isEmpty()
                && !lotField.getText().trim().isEmpty()
                && !managementFeeField.getText().trim().isEmpty()
                && !totalBalanceField.getText().trim().isEmpty()
                && !monthlyPaymentField.getText().trim().isEmpty();
    }

    private boolean areCatalogCombosSelected() {
        return contractTypeCombo.getValue() != null
                && paymentMethodCombo.getValue() != null;
    }

    private boolean areDatesSelected() {
        return firstPaymentDatePicker.getValue() != null
                && contractDatePicker.getValue() != null;
    }

    private boolean areNumberFieldsValid() {
        return isPositiveInteger(paymentDayField.getText().trim())
                && isPositiveInteger(totalPaymentsField.getText().trim());
    }

    private boolean isPositiveInteger(String text) {
        if (text.isEmpty()) return false;
        try {
            return Integer.parseInt(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void applyNumericInputFilters() {
        applyNumericFilter(annuityField, false);
        applyNumericFilter(blockField, false);
        applyNumericFilter(lotField, false);
        applyNumericFilter(managementFeeField, true);
        applyNumericFilter(advanceField, true);
        applyNumericFilter(totalBalanceField, true);
        applyNumericFilter(paymentDayField, false);
        applyNumericFilter(totalPaymentsField, false);
        applyNumericFilter(monthlyPaymentField, true);
    }

    private void applyNumericFilter(TextField field, boolean allowDecimal) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (allowDecimal) {
                return newText.matches("^\\d*\\.?\\d{0,2}$") ? change : null;
            } else {
                return newText.matches("^\\d*$") ? change : null;
            }
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    private void addCurrencyPrefixesToAmountFields() {
        addCurrencyPrefix(managementFeeBox);
        addCurrencyPrefix(advanceBox);
        addCurrencyPrefix(totalBalanceBox);
        addCurrencyPrefix(monthlyPaymentBox);
    }

    private void addCurrencyPrefix(HBox box) {
        Label prefix = new Label("$");
        prefix.getStyleClass().add("currency-prefix");
        box.getChildren().add(0, prefix);
    }
}
