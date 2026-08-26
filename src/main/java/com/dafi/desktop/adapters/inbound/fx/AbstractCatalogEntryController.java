package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.domain.shared.CatalogEntry;
import com.dafi.desktop.infrastructure.I18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.UUID;

/**
 * Base controller for catalog management views (table + create/edit form).
 * Subclasses provide the i18n prefix and the persistence hooks.
 *
 * @param <E> the catalog entry type
 */
public abstract class AbstractCatalogEntryController<E extends CatalogEntry> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @FXML protected TableView<CatalogEntryViewModel> entriesTable;
    @FXML protected TableColumn<CatalogEntryViewModel, String> nameColumn;
    @FXML protected TableColumn<CatalogEntryViewModel, String> descriptionColumn;

    @FXML protected Label titleLabel;
    @FXML protected Label subtitleLabel;
    @FXML protected Label statusLabel;
    @FXML protected Label loadingLabel;
    @FXML protected Button newTypeButton;
    @FXML protected Button backButton;
    @FXML protected TextField searchField;
    @FXML protected Label searchResultsLabel;

    @FXML protected VBox tableContainer;
    @FXML protected VBox formContainer;
    @FXML protected Label formTitleLabel;
    @FXML protected Label labelName;
    @FXML protected Label labelDescription;
    @FXML protected TextField nameField;
    @FXML protected Label nameErrorLabel;
    @FXML protected TextArea descriptionField;
    @FXML protected Label formErrorLabel;
    @FXML protected Label formSuccessLabel;
    @FXML protected Button saveButton;

    private final ObservableList<CatalogEntryViewModel> tableData = FXCollections.observableArrayList();
    private List<E> allEntries = new ArrayList<>();

    private boolean showingForm = false;
    private String editingEntryId = null;

    /**
     * i18n key prefix shared by every text of this view.
     */
    protected abstract String i18nPrefix();

    /**
     * Fetches the current entries from the application layer.
     */
    protected abstract List<E> fetchEntries();

    /**
     * Persists the given entry (insert or update by id).
     */
    protected abstract void persistEntry(E entry);

    /**
     * Builds an entity from the form values.
     */
    protected abstract E buildEntity(String id, String name, String description);

    @FXML
    public void initialize() {
        applyI18nTexts();
        configureTable();
        configureSearchDebounce();
        bindNameFieldValidation();
    }

    private void applyI18nTexts() {
        titleLabel.setText(text("title"));
        subtitleLabel.setText(text("subtitle"));
        newTypeButton.setText("  " + text("new"));
        backButton.setText("  " + text("back"));
        statusLabel.setText(text("loading"));
        searchField.setPromptText(text("search"));

        nameColumn.setText(text("column.name"));
        descriptionColumn.setText(text("column.description"));

        formTitleLabel.setText(text("form.title"));
        labelName.setText(text("form.name"));
        labelDescription.setText(text("form.description"));
        saveButton.setText(text("form.save"));
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        entriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        entriesTable.setItems(tableData);
        entriesTable.setRowFactory(tv -> {
            var row = new TableRow<CatalogEntryViewModel>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    showEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void configureSearchDebounce() {
        SearchDebounceUtils.attach(searchField, this::performSearch);
    }

    private void bindNameFieldValidation() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            nameErrorLabel.setVisible(false);
            validateForm();
        });
    }

    /**
     * Resolves an i18n message by appending the given suffix to {@link #i18nPrefix()}.
     *
     * @param suffix key suffix within this view's i18n namespace
     * @return the localized text for the composed key
     */
    protected String text(String suffix) {
        return I18n.getInstance().get(i18nPrefix() + "." + suffix);
    }

    /**
     * Toggles between the entries table and the create form.
     */
    @FXML
    protected void handleNewType() {
        if (showingForm) {
            showTableView();
        } else {
            showCreateForm();
        }
    }

    /**
     * Returns to the entries table view.
     */
    @FXML
    protected void handleBack() {
        showTableView();
    }

    private void showTableView() {
        showingForm = false;
        editingEntryId = null;
        swapContainers(true);
        newTypeButton.setVisible(true);
        newTypeButton.setManaged(true);
        newTypeButton.setText("  " + text("new"));
        backButton.setVisible(false);
        backButton.setManaged(false);
        loadData();
    }

    private void showCreateForm() {
        showingForm = true;
        editingEntryId = null;
        swapContainers(false);
        newTypeButton.setVisible(true);
        newTypeButton.setManaged(true);
        newTypeButton.setText("  " + text("back"));
        backButton.setVisible(false);
        backButton.setManaged(false);
        formTitleLabel.setText(text("form.title"));
        clearForm();
        formContainer.requestFocus();
    }

    private void showEditForm(CatalogEntryViewModel viewModel) {
        E entry = findEntryById(viewModel.getId());
        if (entry == null) return;

        showingForm = true;
        editingEntryId = entry.getId();
        swapContainers(false);
        newTypeButton.setVisible(false);
        newTypeButton.setManaged(false);
        backButton.setVisible(true);
        backButton.setManaged(true);
        formTitleLabel.setText(text("form.title.edit"));
        clearForm();
        nameField.setText(entry.getName());
        descriptionField.setText(entry.getDescription());
        validateForm();
        formContainer.requestFocus();
    }

    private E findEntryById(String id) {
        return allEntries.stream()
                .filter(entry -> entry.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void swapContainers(boolean showTable) {
        tableContainer.setVisible(showTable);
        tableContainer.setManaged(showTable);
        formContainer.setVisible(!showTable);
        formContainer.setManaged(!showTable);
    }

    /**
     * Persists the entry built from the form and refreshes the view.
     */
    @FXML
    protected void handleSave() {
        try {
            if (hasDuplicateName()) {
                showDuplicateNameMessage();
                return;
            }
            boolean updating = isEditingExistingEntry();
            persistEntryFromForm(updating);
            if (updating) {
                showTableView();
            } else {
                showCreateSuccessFeedback();
            }
        } catch (RuntimeException e) {
            showUnexpectedSaveError(e);
        }
    }

    private boolean isEditingExistingEntry() {
        return editingEntryId != null;
    }

    private boolean hasDuplicateName() {
        String name = nameField.getText().trim();
        return fetchEntries().stream()
                .anyMatch(entry -> name.equalsIgnoreCase(entry.getName())
                        && !entry.getId().equals(editingEntryId));
    }

    private void showDuplicateNameMessage() {
        nameErrorLabel.setText(text("form.name.duplicate"));
        nameErrorLabel.setVisible(true);
    }

    private void persistEntryFromForm(boolean updating) {
        E entry = buildEntity(
                updating ? editingEntryId : UUID.randomUUID().toString(),
                nameField.getText().trim(),
                descriptionField.getText().trim()
        );
        persistEntry(entry);
        updateCachedEntries(entry);
        log.info("Entrada de catálogo guardada: {}", entry.getName());
    }

    private void updateCachedEntries(E entry) {
        allEntries.removeIf(existing -> existing.getId().equals(entry.getId()));
        allEntries.add(entry);
    }

    private void showCreateSuccessFeedback() {
        formSuccessLabel.setText(text("form.success"));
        formSuccessLabel.setVisible(true);
        formErrorLabel.setVisible(false);
        clearForm();
    }

    private void showUnexpectedSaveError(RuntimeException e) {
        log.error("Error al guardar entrada de catálogo", e);
        formErrorLabel.setText(text("form.error"));
        formErrorLabel.setVisible(true);
        formSuccessLabel.setVisible(false);
    }

    private void validateForm() {
        saveButton.setDisable(nameField.getText().trim().isEmpty());
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        formErrorLabel.setVisible(false);
        formSuccessLabel.setVisible(false);
        nameErrorLabel.setVisible(false);
        saveButton.setDisable(true);
    }

    /**
     * Loads the entries asynchronously via {@link #fetchEntries()} and refreshes the table.
     */
    protected void loadData() {
        showLoadingState();

        Task<List<E>> loadTask = createLoadEntriesTask();
        loadTask.setOnSucceeded(event -> showLoadedEntries(loadTask.getValue()));
        loadTask.setOnFailed(event -> showLoadError());

        new Thread(loadTask).start();
    }

    private void showLoadingState() {
        loadingLabel.setVisible(true);
        statusLabel.setText(text("loading"));
    }

    private Task<List<E>> createLoadEntriesTask() {
        return new Task<>() {
            @Override
            protected List<E> call() {
                return fetchEntries();
            }
        };
    }

    private void showLoadedEntries(List<E> entries) {
        Platform.runLater(() -> {
            allEntries = new ArrayList<>(entries);
            refreshTable(entries);
            statusLabel.setText(entries.size() + " " + text("loaded"));
            loadingLabel.setVisible(false);
            searchResultsLabel.setText("");
        });
    }

    private void refreshTable(List<E> entries) {
        tableData.clear();
        entries.forEach(entry -> tableData.add(CatalogEntryViewModel.fromEntry(entry)));
    }

    private void showLoadError() {
        Platform.runLater(() -> {
            statusLabel.setText(text("error"));
            loadingLabel.setVisible(false);
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            refreshTable(allEntries);
            searchResultsLabel.setText("");
            return;
        }

        List<E> results = allEntries.stream()
                .filter(entry -> matchesQuery(entry, query))
                .sorted(Comparator.comparing(
                        entry -> entry.getName() != null ? entry.getName().toLowerCase() : ""))
                .toList();

        refreshTable(results);
        searchResultsLabel.setText(results.size() + " " + text("results"));
    }

    private boolean matchesQuery(E entry, String query) {
        boolean matchName = entry.getName() != null
                && entry.getName().toLowerCase().contains(query);
        boolean matchDesc = entry.getDescription() != null
                && entry.getDescription().toLowerCase().contains(query);
        return matchName || matchDesc;
    }
}
