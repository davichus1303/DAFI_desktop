package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.client.BulkClientImportUseCase;
import com.dafi.desktop.application.client.BulkImportResult;
import com.dafi.desktop.infrastructure.I18n;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Handles the UI interactions for bulk client import: file selection,
 * background execution, result reporting and error handling. The actual
 * table refresh is delegated back to the controller via a callback so
 * that this helper remains free of table-specific knowledge.
 */
public class BulkImportHelper {

    private static final Logger log = LoggerFactory.getLogger(BulkImportHelper.class);

    private final BulkClientImportUseCase importUseCase;
    private final Button ownerButton;
    private final Label statusLabel;
    private final Label loadingLabel;
    private final Runnable onImportSucceeded;

    /**
     * @param importUseCase use case that reads and persists the Excel rows
     * @param ownerButton   button used to anchor file chooser dialogs
     * @param statusLabel   label updated with progress/status text
     * @param loadingLabel  spinner label shown during background work
     * @param onImportSucceeded callback invoked on the FX thread after a successful import
     */
    public BulkImportHelper(BulkClientImportUseCase importUseCase,
                            Button ownerButton,
                            Label statusLabel,
                            Label loadingLabel,
                            Runnable onImportSucceeded) {
        this.importUseCase = importUseCase;
        this.ownerButton = ownerButton;
        this.statusLabel = statusLabel;
        this.loadingLabel = loadingLabel;
        this.onImportSucceeded = onImportSucceeded;
    }

    /**
     * Opens the file chooser, runs the import if a file is selected, and
     * reports the outcome to the user.
     */
    public void execute() {
        File selectedFile = chooseExcelFile();
        if (selectedFile == null) {
            return;
        }
        runImport(selectedFile);
    }

    private File chooseExcelFile() {
        I18n i18n = I18n.getInstance();
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("clients.bulkLoad.chooser.title"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                i18n.get("clients.bulkLoad.chooser.filter"), "*.xlsx", "*.xls"));
        return chooser.showSaveDialog(ownerButton.getScene().getWindow());
    }

    private void runImport(File excelFile) {
        Task<BulkImportResult> task = new Task<>() {
            @Override
            protected BulkImportResult call() throws Exception {
                return importUseCase.importFromFile(excelFile.toPath());
            }
        };
        task.setOnSucceeded(event -> handleSuccess(task.getValue()));
        task.setOnFailed(event -> handleFailure(task.getException()));

        statusLabel.setText(text("clients.bulkLoad.running"));
        loadingLabel.setVisible(true);
        new Thread(task).start();
    }

    private void handleSuccess(BulkImportResult result) {
        onImportSucceeded.run();
        showResultAlert(result);
        statusLabel.setText(result.importedCount() + " " + text("clients.loaded"));
    }

    private void showResultAlert(BulkImportResult result) {
        Alert alert = new Alert(result.rejectedCount() > 0
                ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
        alert.setTitle(text("clients.bulkLoad.result.title"));
        alert.setHeaderText(text("clients.bulkLoad.result.header"));
        alert.setContentText(buildSummary(result));
        alert.initOwner(ownerButton.getScene().getWindow());
        alert.showAndWait();
    }

    /**
     * Builds a human-readable summary of the import operation.
     *
     * @param result outcome returned by the import use case
     * @return multi-line summary string
     */
    private String buildSummary(BulkImportResult result) {
        StringBuilder summary = new StringBuilder()
                .append(text("clients.bulkLoad.found")).append(" ").append(result.totalRows()).append('\n')
                .append(text("clients.bulkLoad.imported")).append(" ").append(result.importedCount()).append('\n')
                .append(text("clients.bulkLoad.rejected")).append(" ").append(result.rejectedCount());

        if (!result.unknownColumns().isEmpty()) {
            summary.append('\n').append(text("clients.bulkLoad.unknownColumns"))
                   .append(" ").append(String.join(", ", result.unknownColumns()));
        }
        if (result.rejectedCount() > 0 && result.reportPath() != null) {
            summary.append("\n\n").append(text("clients.bulkLoad.logHint"))
                   .append("\n").append(result.reportPath());
        }
        return summary.toString();
    }

    private void handleFailure(Throwable exception) {
        log.error("Error durante la carga masiva de clientes", exception);
        Platform.runLater(() -> {
            loadingLabel.setVisible(false);
            statusLabel.setText(text("clients.bulkLoad.error.title"));

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(text("clients.bulkLoad.error.title"));
            alert.setHeaderText(null);
            alert.setContentText(text("clients.bulkLoad.error.reading"));
            alert.initOwner(ownerButton.getScene().getWindow());
            alert.showAndWait();
        });
    }

    private String text(String key) {
        return I18n.getInstance().get(key);
    }
}
