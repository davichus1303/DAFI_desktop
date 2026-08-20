package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.client.GetClientsUseCase;
import com.dafi.desktop.application.client.ClientRepositoryPort;
import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.domain.client.ContractType;
import com.dafi.desktop.domain.client.PaymentMethod;
import com.dafi.desktop.infrastructure.I18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class ClientsController {

    private static final Logger log = LoggerFactory.getLogger(ClientsController.class);

    @FXML private TableView<ClientViewModel> clientsTable;
    @FXML private TableColumn<ClientViewModel, String> folioColumn;
    @FXML private TableColumn<ClientViewModel, String> nameColumn;
    @FXML private TableColumn<ClientViewModel, String> vencimientoColumn;
    @FXML private TableColumn<ClientViewModel, String> contractTypeColumn;
    @FXML private TableColumn<ClientViewModel, String> phoneColumn;
    @FXML private TableColumn<ClientViewModel, String> neighborhoodColumn;
    @FXML private TableColumn<ClientViewModel, String> paymentMethodColumn;
    @FXML private TableColumn<ClientViewModel, String> contractDateColumn;
    @FXML private TableColumn<ClientViewModel, Integer> totalPaymentsColumn;
    @FXML private TableColumn<ClientViewModel, java.math.BigDecimal> monthlyPaymentColumn;
    @FXML private TableColumn<ClientViewModel, java.math.BigDecimal> totalBalanceColumn;

    @FXML private Label statusLabel;
    @FXML private Label loadingLabel;
    @FXML private Label clientsTitle;
    @FXML private Label clientsSubtitle;
    @FXML private Button newClientButton;

    @FXML private TextField searchField;
    @FXML private ProgressIndicator searchSpinner;
    @FXML private Label searchResultsLabel;

    @FXML private VBox tableContainer;
    @FXML private VBox formContainer;
    @FXML private ScrollPane formScrollPane;

    @FXML private TextField folioField;
    @FXML private TextField fullNameField;
    @FXML private TextField ineField;
    @FXML private ComboBox<ContractType> contractTypeCombo;
    @FXML private TextField addressField;
    @FXML private TextField neighborhoodField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<PaymentMethod> paymentMethodCombo;
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

    private GetClientsUseCase getClientsUseCase;
    private ClientRepositoryPort clientRepositoryPort;

    private final ObservableList<ClientViewModel> clientsData = FXCollections.observableArrayList();
    private List<Client> allClients = new ArrayList<>();
    private boolean showingForm = false;
    private Timer searchDebounceTimer;

    @FXML
    public void initialize() {
        I18n i18n = I18n.getInstance();
        clientsTitle.setText(i18n.get("clients.title"));
        clientsSubtitle.setText(i18n.get("clients.subtitle"));
        newClientButton.setText("  " + i18n.get("clients.new"));
        statusLabel.setText(i18n.get("clients.loading"));
        saveButton.setText(i18n.get("form.save"));
        searchField.setPromptText(i18n.get("clients.search"));

        folioColumn.setText(i18n.get("table.folio"));
        nameColumn.setText(i18n.get("table.name"));
        vencimientoColumn.setText(i18n.get("table.vencimiento"));
        contractTypeColumn.setText(i18n.get("table.contractType"));
        phoneColumn.setText(i18n.get("table.phone"));
        neighborhoodColumn.setText(i18n.get("table.neighborhood"));
        paymentMethodColumn.setText(i18n.get("table.paymentMethod"));
        contractDateColumn.setText(i18n.get("table.contractDate"));
        totalPaymentsColumn.setText(i18n.get("table.totalPayments"));
        monthlyPaymentColumn.setText(i18n.get("table.monthlyPayment"));
        totalBalanceColumn.setText(i18n.get("table.totalBalance"));

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

        contractTypeCombo.getItems().addAll(ContractType.values());
        contractTypeCombo.getSelectionModel().selectFirst();

        paymentMethodCombo.getItems().addAll(PaymentMethod.values());
        paymentMethodCombo.getSelectionModel().selectFirst();

        contractDatePicker.setValue(LocalDate.now());

        bindFormValidation();
        applyNumericFilter(annuityField, false);
        applyNumericFilter(blockField, false);
        applyNumericFilter(lotField, false);
        applyNumericFilter(managementFeeField, true);
        applyNumericFilter(advanceField, true);
        applyNumericFilter(totalBalanceField, true);
        applyNumericFilter(paymentDayField, false);
        applyNumericFilter(totalPaymentsField, false);
        applyNumericFilter(monthlyPaymentField, true);

        addCurrencyPrefix(managementFeeBox);
        addCurrencyPrefix(advanceBox);
        addCurrencyPrefix(totalBalanceBox);
        addCurrencyPrefix(monthlyPaymentBox);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (searchDebounceTimer != null) searchDebounceTimer.cancel();
            searchDebounceTimer = new Timer(true);
            searchDebounceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> performSearch());
                }
            }, 300);
        });
    }

    private void addCurrencyPrefix(HBox box) {
        Label prefix = new Label("$");
        prefix.getStyleClass().add("currency-prefix");
        box.getChildren().add(0, prefix);
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

    public void setDependencies(GetClientsUseCase getClientsUseCase,
                                ClientRepositoryPort clientRepositoryPort) {
        this.getClientsUseCase = getClientsUseCase;
        this.clientRepositoryPort = clientRepositoryPort;
        initializeColumns();
        loadClients();
    }

    @FXML
    private void handleNewClient() {
        I18n i18n = I18n.getInstance();
        showingForm = !showingForm;

        if (showingForm) {
            tableContainer.setVisible(false);
            tableContainer.setManaged(false);
            formContainer.setVisible(true);
            formContainer.setManaged(true);
            formContainer.requestFocus();
            newClientButton.setText("  " + i18n.get("clients.viewList"));
            clearForm();
        } else {
            tableContainer.setVisible(true);
            tableContainer.setManaged(true);
            formContainer.setVisible(false);
            formContainer.setManaged(false);
            newClientButton.setText("  " + i18n.get("clients.new"));
            loadClients();
        }
    }

    @FXML
    private void handleSaveClient() {
        I18n i18n = I18n.getInstance();

        try {
            Client client = buildClientFromForm();
            clientRepositoryPort.save(client);
            allClients.add(client);
            log.info("Cliente guardado: {}", client.getFullName());

            formSuccessLabel.setText(i18n.get("form.success.save"));
            formSuccessLabel.setVisible(true);
            formErrorLabel.setVisible(false);
            clearForm();

        } catch (Exception e) {
            log.error("Error al guardar cliente", e);
            formErrorLabel.setText(i18n.get("form.error.save"));
            formErrorLabel.setVisible(true);
            formSuccessLabel.setVisible(false);
        }
    }

    private Client buildClientFromForm() {
        LocalDate contractDate = contractDatePicker.getValue();
        int totalPayments = Integer.parseInt(totalPaymentsField.getText().trim());
        LocalDate contractEndDate = contractDate.plusMonths(totalPayments);

        return new Client(
                UUID.randomUUID().toString(),
                folioField.getText().trim(),
                fullNameField.getText().trim(),
                ineField.getText().trim(),
                contractTypeCombo.getValue(),
                addressField.getText().trim(),
                neighborhoodField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                paymentMethodCombo.getValue(),
                firstBeneficiaryField.getText().trim(),
                secondBeneficiaryField.getText().trim(),
                saleDescriptionField.getText().trim(),
                annuityField.getText().trim(),
                blockField.getText().trim(),
                lotField.getText().trim(),
                parseBigDecimal(managementFeeField.getText().trim()),
                parseBigDecimal(advanceField.getText().trim()),
                parseBigDecimal(totalBalanceField.getText().trim()),
                firstPaymentDatePicker.getValue(),
                contractDate,
                Integer.parseInt(paymentDayField.getText().trim()),
                totalPayments,
                parseBigDecimal(monthlyPaymentField.getText().trim()),
                contractEndDate
        );
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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
        formErrorLabel.setVisible(false);
        formSuccessLabel.setVisible(false);
        saveButton.setDisable(true);
    }

    private void bindFormValidation() {
        Runnable validator = this::validateForm;
        folioField.textProperty().addListener((obs, o, n) -> validator.run());
        fullNameField.textProperty().addListener((obs, o, n) -> validator.run());
        ineField.textProperty().addListener((obs, o, n) -> validator.run());
        addressField.textProperty().addListener((obs, o, n) -> validator.run());
        neighborhoodField.textProperty().addListener((obs, o, n) -> validator.run());
        phoneField.textProperty().addListener((obs, o, n) -> validator.run());
        firstBeneficiaryField.textProperty().addListener((obs, o, n) -> validator.run());
        secondBeneficiaryField.textProperty().addListener((obs, o, n) -> validator.run());
        saleDescriptionField.textProperty().addListener((obs, o, n) -> validator.run());
        annuityField.textProperty().addListener((obs, o, n) -> validator.run());
        blockField.textProperty().addListener((obs, o, n) -> validator.run());
        lotField.textProperty().addListener((obs, o, n) -> validator.run());
        managementFeeField.textProperty().addListener((obs, o, n) -> validator.run());
        advanceField.textProperty().addListener((obs, o, n) -> validator.run());
        totalBalanceField.textProperty().addListener((obs, o, n) -> validator.run());
        paymentDayField.textProperty().addListener((obs, o, n) -> validator.run());
        totalPaymentsField.textProperty().addListener((obs, o, n) -> validator.run());
        monthlyPaymentField.textProperty().addListener((obs, o, n) -> validator.run());
        firstPaymentDatePicker.valueProperty().addListener((obs, o, n) -> validator.run());
        contractDatePicker.valueProperty().addListener((obs, o, n) -> validator.run());
        contractTypeCombo.valueProperty().addListener((obs, o, n) -> validator.run());
        paymentMethodCombo.valueProperty().addListener((obs, o, n) -> validator.run());

        totalBalanceField.textProperty().addListener((obs, o, n) -> autoCalculateMonthlyPayment());
        totalPaymentsField.textProperty().addListener((obs, o, n) -> autoCalculateMonthlyPayment());
    }

    private void autoCalculateMonthlyPayment() {
        try {
            String balanceText = totalBalanceField.getText().trim();
            String paymentsText = totalPaymentsField.getText().trim();
            if (!balanceText.isEmpty() && !paymentsText.isEmpty()) {
                BigDecimal balance = new BigDecimal(balanceText);
                int payments = Integer.parseInt(paymentsText);
                if (payments > 0) {
                    BigDecimal monthly = balance.divide(BigDecimal.valueOf(payments), 2, java.math.RoundingMode.HALF_UP);
                    monthlyPaymentField.setText(monthly.toPlainString());
                    validateForm();
                    return;
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void validateForm() {
        I18n i18n = I18n.getInstance();
        String folio = folioField.getText().trim();
        boolean folioExists = !folio.isEmpty() && allClients.stream()
                .anyMatch(c -> folio.equalsIgnoreCase(c.getContractFolio()));

        if (folioExists) {
            formErrorLabel.setText(i18n.get("form.error.folio.duplicate"));
            formErrorLabel.setVisible(true);
        } else {
            formErrorLabel.setVisible(false);
        }

        boolean valid = !folio.isEmpty()
                && !folioExists
                && !fullNameField.getText().trim().isEmpty()
                && !ineField.getText().trim().isEmpty()
                && contractTypeCombo.getValue() != null
                && !addressField.getText().trim().isEmpty()
                && !neighborhoodField.getText().trim().isEmpty()
                && !phoneField.getText().trim().isEmpty()
                && paymentMethodCombo.getValue() != null
                && !firstBeneficiaryField.getText().trim().isEmpty()
                && !secondBeneficiaryField.getText().trim().isEmpty()
                && !saleDescriptionField.getText().trim().isEmpty()
                && !annuityField.getText().trim().isEmpty()
                && !blockField.getText().trim().isEmpty()
                && !lotField.getText().trim().isEmpty()
                && !managementFeeField.getText().trim().isEmpty()
                && !totalBalanceField.getText().trim().isEmpty()
                && firstPaymentDatePicker.getValue() != null
                && contractDatePicker.getValue() != null
                && isPositiveInteger(paymentDayField.getText().trim())
                && isPositiveInteger(totalPaymentsField.getText().trim())
                && !monthlyPaymentField.getText().trim().isEmpty();
        saveButton.setDisable(!valid);
    }

    private boolean isPositiveInteger(String text) {
        if (text.isEmpty()) return false;
        try {
            return Integer.parseInt(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isContractActive(Client client) {
        return client.getContractEndDate() == null
                || !client.getContractEndDate().isBefore(LocalDate.now());
    }

    private void initializeColumns() {
        folioColumn.setCellValueFactory(new PropertyValueFactory<>("contractFolio"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        vencimientoColumn.setCellValueFactory(new PropertyValueFactory<>("contractEndDateFormatted"));
        contractTypeColumn.setCellValueFactory(new PropertyValueFactory<>("contractType"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        neighborhoodColumn.setCellValueFactory(new PropertyValueFactory<>("neighborhood"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        contractDateColumn.setCellValueFactory(new PropertyValueFactory<>("contractDateFormatted"));
        totalPaymentsColumn.setCellValueFactory(new PropertyValueFactory<>("totalPayments"));
        monthlyPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("monthlyPayment"));
        totalBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("totalBalance"));

        monthlyPaymentColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.math.BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "$ " + item.toString());
            }
        });

        totalBalanceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.math.BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "$ " + item.toString());
            }
        });

        clientsTable.setItems(clientsData);
    }

    private void loadClients() {
        I18n i18n = I18n.getInstance();
        loadingLabel.setVisible(true);
        statusLabel.setText(i18n.get("clients.loading"));

        Task<List<Client>> loadTask = new Task<>() {
            @Override
            protected List<Client> call() {
                return getClientsUseCase.getClientsOrderedByContractEndDate();
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Client> clients = loadTask.getValue();
            Platform.runLater(() -> {
                allClients = new ArrayList<>(clients);
                clientsData.clear();
                clients.stream()
                        .filter(this::isContractActive)
                        .forEach(client -> clientsData.add(ClientViewModel.fromClient(client)));
                statusLabel.setText(clientsData.size() + " " + i18n.get("clients.loaded"));
                loadingLabel.setVisible(false);
                searchResultsLabel.setText("");
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                statusLabel.setText(i18n.get("clients.error"));
                loadingLabel.setVisible(false);
            });
        });

        new Thread(loadTask).start();
    }

    private void performSearch() {
        I18n i18n = I18n.getInstance();
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            clientsData.clear();
            allClients.stream()
                    .filter(this::isContractActive)
                    .forEach(c -> clientsData.add(ClientViewModel.fromClient(c)));
            searchResultsLabel.setText("");
            return;
        }

        List<Client> results = allClients.stream()
                .filter(this::isContractActive)
                .filter(c -> {
                    boolean matchName = c.getFullName() != null
                            && c.getFullName().toLowerCase().contains(query);
                    boolean matchFolio = c.getContractFolio() != null
                            && c.getContractFolio().toLowerCase().contains(query);
                    return matchName || matchFolio;
                })
                .sorted(Comparator.comparing(
                        Client::getContractEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        clientsData.clear();
        results.forEach(client -> clientsData.add(ClientViewModel.fromClient(client)));
        searchResultsLabel.setText(results.size() + " " + i18n.get("clients.results"));
    }
}
