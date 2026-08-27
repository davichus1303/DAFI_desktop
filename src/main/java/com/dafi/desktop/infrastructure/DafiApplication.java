package com.dafi.desktop.infrastructure;

import com.dafi.desktop.adapters.inbound.fx.SceneFactory;
import com.dafi.desktop.application.auth.AuthenticateUserUseCase;
import com.dafi.desktop.shared.utils.BaseDirectory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

/**
 * Main class of the DAFI Desktop application.
 * Entry point of the JavaFX application.
 */
public class DafiApplication extends Application {

    private static final Path CONFIG_DIR = BaseDirectory.configDir();
    private static final Path DATA_DIR = BaseDirectory.dataDir();

    /**
     * Initializes internationalization and scene infrastructure, runs
     * first-time setup when no user is configured, then shows the login window.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        I18n.getInstance();
        SceneFactory.initialize(CONFIG_DIR, DATA_DIR);

        AuthenticateUserUseCase authenticateUseCase = createAuthenticateUseCase();

        if (!authenticateUseCase.hasConfiguredUser()) {
            showSetupDialog(authenticateUseCase);
            generateEncryptionKey();
        }

        I18n i18n = I18n.getInstance();
        Scene loginScene = SceneFactory.createLoginScene(primaryStage);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle(i18n.get("app.title") + " " + i18n.get("app.brand") + " - " + i18n.get("login.title"));
        primaryStage.setResizable(false);
        primaryStage.getIcons().setAll(loadAppIcons());
        primaryStage.show();
    }

    private List<Image> loadAppIcons() {
        return Stream.of(16, 24, 32, 48, 64, 128, 256)
                .map(size -> new Image("/icons/icon-" + size + ".png"))
                .toList();
    }

    private AuthenticateUserUseCase createAuthenticateUseCase() {
        var credentialsStorage = new com.dafi.desktop.adapters.outbound.json.FileCredentialsStorageAdapter(CONFIG_DIR);
        var passwordHasher = new com.dafi.desktop.adapters.outbound.security.Argon2PasswordHasherAdapter();
        return new AuthenticateUserUseCase(credentialsStorage, passwordHasher);
    }

    private void showSetupDialog(AuthenticateUserUseCase authenticateUseCase) {
        I18n i18n = I18n.getInstance();

        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle(i18n.get("setup.title"));
        usernameDialog.setHeaderText(i18n.get("setup.username.header"));
        usernameDialog.setContentText(i18n.get("setup.username.prompt"));

        usernameDialog.showAndWait().ifPresent(username -> {
            Dialog<String> passwordDialog = new Dialog<>();
            passwordDialog.setTitle(i18n.get("setup.title"));
            passwordDialog.setHeaderText(i18n.get("setup.password.header"));

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText(i18n.get("setup.password.prompt"));

            VBox vbox = new VBox(passwordField);
            vbox.setSpacing(10);

            passwordDialog.getDialogPane().setContent(vbox);
            passwordDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return passwordField.getText();
                }
                return null;
            });

            passwordDialog.showAndWait().ifPresent(password -> {
                if (password != null && !password.isEmpty()) {
                    authenticateUseCase.registerAdmin(username, password);
                }
            });
        });
    }

    private void generateEncryptionKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            SceneFactory.getKeyStoragePort().storeEncryptionKey(encodedKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar la clave de cifrado", e);
        }
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
