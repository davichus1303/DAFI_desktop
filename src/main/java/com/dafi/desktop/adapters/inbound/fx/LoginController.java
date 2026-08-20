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
 * Controlador de la pantalla de inicio de sesión.
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
     * Inicializa el controlador después de que el FXML se ha cargado.
     */
    @FXML
    public void initialize() {
        I18n i18n = I18n.getInstance();
        usernameField.setPromptText(i18n.get("login.username"));
        passwordField.setPromptText(i18n.get("login.password"));
        loginButton.setText(i18n.get("login.button"));
    }

    public void setAuthenticateUseCase(com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleLogin() {
        I18n i18n = I18n.getInstance();
        String username = usernameField.getText();
        String password = passwordField.getText();

        log.info("Intento de login para usuario: {}", username);

        if (username.isEmpty() || password.isEmpty()) {
            showError(i18n.get("login.error.empty"));
            return;
        }

        loginButton.setDisable(true);

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    return authenticateUseCase.authenticate(username, password);
                } catch (Exception e) {
                    log.error("Error durante autenticación", e);
                    throw e;
                }
            }
        };

        loginTask.setOnSucceeded(event -> {
            Boolean success = loginTask.getValue();
            log.info("Resultado autenticación: {}", success);
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                if (success) {
                    try {
                        SceneFactory factory = new SceneFactory();
                        primaryStage.setScene(factory.createMainScene(primaryStage, authenticateUseCase));
                        primaryStage.setTitle(i18n.get("app.title") + " - " + i18n.get("app.subtitle"));
                    } catch (Exception e) {
                        log.error("Error al crear escena principal", e);
                        showError("Error al cargar la aplicación: " + e.getMessage());
                    }
                } else {
                    showError(i18n.get("login.error"));
                }
            });
        });

        loginTask.setOnFailed(event -> {
            log.error("Login task falló", loginTask.getException());
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                showError(i18n.get("login.error.general") + ": " + 
                    (loginTask.getException() != null ? loginTask.getException().getMessage() : "unknown"));
            });
        });

        new Thread(loginTask).start();
    }

    private void showError(String message) {
        log.warn("Error mostrado al usuario: {}", message);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
