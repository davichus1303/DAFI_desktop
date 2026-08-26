package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.infrastructure.I18n;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the login screen.
 * Authenticates the credentials against the {@link com.dafi.desktop.application.auth.AuthenticateUserUseCase}
 * on a background task and swaps to the main scene on success.
 */
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase;
    private Stage primaryStage;

    /**
     * Applies i18n texts after the FXML has been loaded.
     */
    @FXML
    public void initialize() {
        I18n i18n = I18n.getInstance();
        usernameField.setPromptText(i18n.get("login.username"));
        passwordField.setPromptText(i18n.get("login.password"));
        loginButton.setText(i18n.get("login.button"));
    }

    /**
     * Sets the authentication use case invoked on login.
     *
     * @param authenticateUseCase authentication use case
     */
    public void setAuthenticateUseCase(com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    /**
     * Sets the primary stage used to swap to the main scene after a successful login.
     *
     * @param primaryStage application primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleLogin() {
        I18n i18n = I18n.getInstance();
        String username = usernameField.getText();
        String password = passwordField.getText();

        log.info("Intento de login para usuario: {}", username);

        if (hasEmptyCredentials()) {
            showError(i18n.get("login.error.empty"));
            return;
        }

        setLoginInProgress(true);
        authenticateInBackground(username, password, i18n);
    }

    private boolean hasEmptyCredentials() {
        return usernameField.getText().isEmpty() || passwordField.getText().isEmpty();
    }

    private void setLoginInProgress(boolean inProgress) {
        loginButton.setDisable(inProgress);
    }

    private void authenticateInBackground(String username, String password, I18n i18n) {
        Task<Boolean> loginTask = createAuthenticationTask(username, password);

        loginTask.setOnSucceeded(event -> handleAuthenticationSuccess(loginTask.getValue(), i18n));
        loginTask.setOnFailed(event -> handleAuthenticationFailure(loginTask, i18n));

        new Thread(loginTask).start();
    }

    private Task<Boolean> createAuthenticationTask(String username, String password) {
        return new Task<>() {
            @Override
            protected Boolean call() {
                return authenticateUseCase.authenticate(username, password);
            }
        };
    }

    private void handleAuthenticationSuccess(Boolean success, I18n i18n) {
        log.info("Resultado autenticación: {}", success);
        Platform.runLater(() -> {
            setLoginInProgress(false);
            if (success) {
                openMainWindow(i18n);
            } else {
                showError(i18n.get("login.error"));
            }
        });
    }

    private void openMainWindow(I18n i18n) {
        try {
            primaryStage.setScene(SceneFactory.createMainScene(primaryStage, authenticateUseCase));
            primaryStage.setTitle(i18n.get("app.title") + " " + i18n.get("app.brand") + " - " + i18n.get("app.subtitle"));
        } catch (Exception e) {
            log.error("Error al crear escena principal", e);
            showError("Error al cargar la aplicación: " + e.getMessage());
        }
    }

    private void handleAuthenticationFailure(Task<Boolean> loginTask, I18n i18n) {
        log.error("Login task falló", loginTask.getException());
        Platform.runLater(() -> {
            setLoginInProgress(false);
            showError(i18n.get("login.error.general") + ": " +
                (loginTask.getException() != null ? loginTask.getException().getMessage() : "unknown"));
        });
    }

    private void showError(String message) {
        log.warn("Error mostrado al usuario: {}", message);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
