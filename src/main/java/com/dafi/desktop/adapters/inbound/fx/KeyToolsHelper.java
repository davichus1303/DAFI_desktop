package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.security.ExportEncryptionKeyUseCase;
import com.dafi.desktop.application.security.ImportEncryptionKeyUseCase;
import com.dafi.desktop.infrastructure.I18n;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

/**
 * Encapsulates all encryption-key export/import UI operations so that
 * multiple controllers can reuse the same menu, dialogs and file-chooser
 * logic without duplicating code.
 */
public class KeyToolsHelper {

    private final ExportEncryptionKeyUseCase exportUseCase;
    private final ImportEncryptionKeyUseCase importUseCase;
    private final Button ownerButton;

    /**
     * @param exportUseCase use case writing the key to a file
     * @param importUseCase use case reading the key from a file
     * @param ownerButton button under which the context menu is anchored
     */
    public KeyToolsHelper(ExportEncryptionKeyUseCase exportUseCase,
                          ImportEncryptionKeyUseCase importUseCase,
                          Button ownerButton) {
        this.exportUseCase = exportUseCase;
        this.importUseCase = importUseCase;
        this.ownerButton = ownerButton;
    }

    /**
     * Shows the tools context menu anchored below the owner button.
     */
    public void showMenu() {
        I18n i18n = I18n.getInstance();
        ContextMenu menu = new ContextMenu(
                buildMenuItem(i18n.get("security.keyTools.menu.export"), this::handleExport),
                buildMenuItem(i18n.get("security.keyTools.menu.import"), this::handleImport));
        menu.show(ownerButton, javafx.geometry.Side.BOTTOM, 0, 2);
    }

    /**
     * Builds a context menu entry bound to the given handler.
     *
     * @param label localized menu text
     * @param handler action executed when the entry is chosen
     * @return the configured menu item
     */
    private MenuItem buildMenuItem(String label, Runnable handler) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(e -> handler.run());
        return item;
    }

    /**
     * Exports the encryption key: asks where to save it, runs the export
     * and reports the outcome.
     */
    private void handleExport() {
        Path destination = chooseExportDestination();
        if (destination == null) {
            return;
        }
        try {
            exportUseCase.exportTo(destination);
            showFeedback(Alert.AlertType.INFORMATION,
                    I18n.getInstance().get("security.keyTools.export.success") + "\n" + destination);
        } catch (Exception e) {
            showError(e);
        }
    }

    /**
     * Opens a save dialog so the user picks where the key file is stored.
     *
     * @return selected destination, or {@code null} when cancelled
     */
    private Path chooseExportDestination() {
        I18n i18n = I18n.getInstance();
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("security.keyTools.export.chooser.title"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                i18n.get("security.keyTools.keyFile.filter"),
                "*.txt", "*.key"));
        File selected = chooser.showSaveDialog(ownerButton.getScene().getWindow());
        return selected == null ? null : ensureTxtExtension(selected).toPath();
    }

    /**
     * Appends the .txt extension when the user typed a file name without one.
     *
     * @param file chosen destination file
     * @return the same file guaranteed to end with .txt
     */
    private File ensureTxtExtension(File file) {
        return file.getName().contains(".") ? file : new File(file.getParentFile(), file.getName() + ".txt");
    }

    /**
     * Imports an encryption key: warns about data compatibility, asks for
     * the source file, runs the import and reports the outcome.
     */
    private void handleImport() {
        if (!confirmImportOverwritesData()) {
            return;
        }
        Path source = chooseImportSource();
        if (source == null) {
            return;
        }
        try {
            importUseCase.importFrom(source);
            showFeedback(Alert.AlertType.INFORMATION,
                    I18n.getInstance().get("security.keyTools.import.success"));
        } catch (Exception e) {
            showError(e);
        }
    }

    /**
     * Shows the confirmation dialog warning that switching keys can make
     * existing encrypted data unreadable.
     *
     * @return {@code true} when the user accepts the import
     */
    private boolean confirmImportOverwritesData() {
        I18n i18n = I18n.getInstance();
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(i18n.get("security.keyTools.import.confirm.title"));
        confirmation.setHeaderText(i18n.get("security.keyTools.import.confirm.header"));
        confirmation.setContentText(i18n.get("security.keyTools.import.confirm.content"));
        return confirmation.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    /**
     * Opens an open dialog so the user selects the exported key file.
     *
     * @return selected source file path, or {@code null} when cancelled
     */
    private Path chooseImportSource() {
        I18n i18n = I18n.getInstance();
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n.get("security.keyTools.import.chooser.title"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                i18n.get("security.keyTools.keyFile.filter"),
                "*.txt", "*.key"));
        File selected = chooser.showOpenDialog(ownerButton.getScene().getWindow());
        return selected == null ? null : selected.toPath();
    }

    /**
     * Shows a dialog describing the outcome of a key operation.
     *
     * @param type alert type (information or error)
     * @param message localized result text
     */
    private void showFeedback(Alert.AlertType type, String message) {
        Alert feedback = new Alert(type);
        feedback.setTitle(I18n.getInstance().get("security.keyTools.feedback.title"));
        feedback.setHeaderText(null);
        feedback.setContentText(message);
        feedback.showAndWait();
    }

    /**
     * Shows the standard error dialog for failed key operations.
     *
     * @param e exception raised by the underlying use case
     */
    private void showError(Exception e) {
        showFeedback(Alert.AlertType.ERROR,
                I18n.getInstance().get("security.keyTools.error.content") + " " + e.getMessage());
    }
}
