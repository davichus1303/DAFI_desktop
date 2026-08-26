package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.client.BulkClientImportUseCase;
import com.dafi.desktop.application.client.ClientRepositoryPort;
import com.dafi.desktop.application.client.GetClientsUseCase;
import com.dafi.desktop.application.contracttype.GetContractTypesUseCase;
import com.dafi.desktop.application.paymentmethod.GetPaymentMethodsUseCase;
import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.infrastructure.I18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Container controller for the clients view: table, search and navigation.
 * The create/edit form lives in {@link ClientFormController}, wired here via callbacks.
 */
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
    @FXML private TableColumn<ClientViewModel, BigDecimal> monthlyPaymentColumn;
    @FXML private TableColumn<ClientViewModel, BigDecimal> totalBalanceColumn;

    @FXML private Label statusLabel;
    @FXML private Label loadingLabel;
    @FXML private Label clientsTitle;
    @FXML private Label clientsSubtitle;
    @FXML private Button newClientButton;
    @FXML private Button bulkLoadButton;
    @FXML private Button keyToolsButton;

    @FXML private TextField searchField;
    @FXML private ProgressIndicator searchSpinner;
    @FXML private Label searchResultsLabel;

    @FXML private VBox tableContainer;
    @FXML private VBox clientForm;

    @FXML private ClientFormController clientFormController;

    private GetClientsUseCase getClientsUseCase;
    private ClientRepositoryPort clientRepositoryPort;
    private BulkImportHelper bulkImportHelper;
    private KeyToolsHelper keyToolsHelper;

    private final ObservableList<ClientViewModel> clientsData = FXCollections.observableArrayList();
    private List<Client> allClients = new ArrayList<>();
    private boolean showingForm = false;
    private String selectedClientId = null;

    @FXML
    public void initialize() {
        applyI18nTexts();
        configureSearchDebounce();
        configureTableRowSelection();
    }

    private void applyI18nTexts() {
        I18n i18n = I18n.getInstance();
        applyHeaderTexts(i18n);
        applyTableHeaderTexts(i18n);
    }

    private void applyHeaderTexts(I18n i18n) {
        clientsTitle.setText(i18n.get("clients.title"));
        clientsSubtitle.setText(i18n.get("clients.subtitle"));
        newClientButton.setText("  " + i18n.get("clients.new"));
        bulkLoadButton.setText("  " + i18n.get("clients.bulkLoad"));
        statusLabel.setText(i18n.get("clients.loading"));
        searchField.setPromptText(i18n.get("clients.search"));
    }

    private void applyTableHeaderTexts(I18n i18n) {
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
    }

    private void configureSearchDebounce() {
        SearchDebounceUtils.attach(searchField, this::performSearch);
    }

    private void configureTableRowSelection() {
        clientsTable.setOnMouseClicked(event -> {
            ClientViewModel selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showClientDetail(selected);
            }
        });
    }

    /**
     * Injects the use cases and repository port, wires the embedded client form
     * and triggers the initial load.
     *
     * @param getClientsUseCase use case listing clients ordered by contract end date
     * @param clientRepositoryPort port used to persist client updates
     * @param bulkClientImportUseCase use case importing clients from Excel files
     * @param getContractTypesUseCase use case feeding the form's contract type combo
     * @param getPaymentMethodsUseCase use case feeding the form's payment method combo
     * @param exportEncryptionKeyUseCase use case exporting the encryption key to a text file
     * @param importEncryptionKeyUseCase use case importing an encryption key from a text file
     */
    public void setDependencies(GetClientsUseCase getClientsUseCase,
                                ClientRepositoryPort clientRepositoryPort,
                                BulkClientImportUseCase bulkClientImportUseCase,
                                GetContractTypesUseCase getContractTypesUseCase,
                                GetPaymentMethodsUseCase getPaymentMethodsUseCase,
                                com.dafi.desktop.application.security.ExportEncryptionKeyUseCase exportUseCase,
                                com.dafi.desktop.application.security.ImportEncryptionKeyUseCase importUseCase) {
        this.getClientsUseCase = getClientsUseCase;
        this.clientRepositoryPort = clientRepositoryPort;
        this.bulkImportHelper = new BulkImportHelper(bulkClientImportUseCase, bulkLoadButton, statusLabel, loadingLabel, this::loadClients);
        this.keyToolsHelper = new KeyToolsHelper(exportUseCase, importUseCase, keyToolsButton);

        wireClientForm(getContractTypesUseCase, getPaymentMethodsUseCase);

        initializeColumns();
        loadClients();
    }

    /**
     * Displays the tools context menu with the encryption key options.
     */
    @FXML
    private void showKeyToolsMenu() {
        keyToolsHelper.showMenu();
    }

    private void wireClientForm(GetContractTypesUseCase getContractTypesUseCase,
                                GetPaymentMethodsUseCase getPaymentMethodsUseCase) {
        clientFormController.setCatalogs(getContractTypesUseCase, getPaymentMethodsUseCase);
        clientFormController.setClientSaver(this::persistClient);
        clientFormController.setFormCloseHandler(this::showClientsTableAfterSave);
        clientFormController.setDuplicateFolioChecker(this::hasDuplicateFolio);
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

        monthlyPaymentColumn.setCellFactory(col -> newCurrencyCell());
        totalBalanceColumn.setCellFactory(col -> newCurrencyCell());

        clientsTable.setItems(clientsData);
    }

    private TableCell<ClientViewModel, BigDecimal> newCurrencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "$ " + item.toString());
            }
        };
    }

    @FXML
    private void handleNewClient() {
        showingForm = !showingForm;
        if (showingForm) {
            openNewClientForm();
        } else {
            backToClientsTable();
        }
    }

    @FXML
    private void handleBulkLoad() {
        bulkImportHelper.execute();
    }











    private void openNewClientForm() {
        selectedClientId = null;
        swapContainers(true);
        requestFocusOnForm();
        newClientButton.setText("  " + text("clients.viewList"));
        clientFormController.startNewClient();
    }

    private void backToClientsTable() {
        selectedClientId = null;
        swapContainers(false);
        newClientButton.setText("  " + text("clients.new"));
        loadClients();
    }

    private void showClientDetail(ClientViewModel viewModel) {
        selectedClientId = viewModel.getId();

        Client client = findClientById(selectedClientId);
        if (client == null) return;

        showingForm = true;
        swapContainers(true);
        newClientButton.setText("  " + text("clients.viewList"));
        clientFormController.showClientDetail(client);
    }

    private Client findClientById(String id) {
        return allClients.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void swapContainers(boolean showForm) {
        tableContainer.setVisible(!showForm);
        tableContainer.setManaged(!showForm);
        clientForm.setVisible(showForm);
        clientForm.setManaged(showForm);
    }

    private void requestFocusOnForm() {
        clientForm.requestFocus();
    }

    private void persistClient(Client client) {
        boolean isUpdate = selectedClientId != null;
        clientRepositoryPort.save(client);
        updateCachedClient(client);
        logClientPersisted(client, isUpdate);
    }

    private void updateCachedClient(Client client) {
        allClients.removeIf(c -> c.getId().equals(selectedClientId));
        allClients.add(client);
    }

    private void logClientPersisted(Client client, boolean isUpdate) {
        if (isUpdate) {
            log.info("Cliente actualizado: {}", client.getFullName());
        } else {
            log.info("Cliente guardado: {}", client.getFullName());
        }
    }

    private void showClientsTableAfterSave() {
        showingForm = false;
        swapContainers(false);
        newClientButton.setText("  " + text("clients.new"));
        selectedClientId = null;
        loadClients();
    }

    /**
     * Detects whether another client (different from the one being edited)
     * already uses the given contract folio. The excluded id comes from the
     * form itself so updates never flag the client's own folio as duplicate.
     */
    private boolean hasDuplicateFolio(String folio, String clientIdToExclude) {
        if (folio == null || folio.isEmpty()) return false;
        return allClients.stream()
                .anyMatch(c -> folio.equalsIgnoreCase(c.getContractFolio())
                        && !c.getId().equals(clientIdToExclude));
    }

    private String text(String key) {
        return I18n.getInstance().get(key);
    }

    private boolean isContractActive(Client client) {
        return client.getContractEndDate() == null
                || !client.getContractEndDate().isBefore(LocalDate.now());
    }

    private void loadClients() {
        showClientsLoadingState();

        Task<List<Client>> loadTask = createLoadClientsTask();
        loadTask.setOnSucceeded(event -> showLoadedClients(loadTask.getValue()));
        loadTask.setOnFailed(event -> showClientsLoadError());

        new Thread(loadTask).start();
    }

    private void showClientsLoadingState() {
        loadingLabel.setVisible(true);
        statusLabel.setText(text("clients.loading"));
    }

    private Task<List<Client>> createLoadClientsTask() {
        return new Task<>() {
            @Override
            protected List<Client> call() {
                return getClientsUseCase.getClientsOrderedByContractEndDate();
            }
        };
    }

    private void showLoadedClients(List<Client> clients) {
        Platform.runLater(() -> {
            allClients = new ArrayList<>(clients);
            refreshClientsTable(clients);
            statusLabel.setText(clientsData.size() + " " + text("clients.loaded"));
            loadingLabel.setVisible(false);
            searchResultsLabel.setText("");
        });
    }

    private void refreshClientsTable(List<Client> clients) {
        clientsData.clear();
        clients.stream()
                .filter(this::isContractActive)
                .forEach(client -> clientsData.add(ClientViewModel.fromClient(client)));
    }

    private void showClientsLoadError() {
        Platform.runLater(() -> {
            statusLabel.setText(text("clients.error"));
            loadingLabel.setVisible(false);
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            refreshClientsTable(allClients);
            searchResultsLabel.setText("");
            return;
        }

        List<Client> results = allClients.stream()
                .filter(this::isContractActive)
                .filter(c -> matchesQuery(c, query))
                .sorted(Comparator.comparing(
                        Client::getContractEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        clientsData.clear();
        results.forEach(client -> clientsData.add(ClientViewModel.fromClient(client)));
        searchResultsLabel.setText(results.size() + " " + text("clients.results"));
    }

    private boolean matchesQuery(Client client, String query) {
        boolean matchName = client.getFullName() != null
                && client.getFullName().toLowerCase().contains(query);
        boolean matchFolio = client.getContractFolio() != null
                && client.getContractFolio().toLowerCase().contains(query);
        return matchName || matchFolio;
    }
}
