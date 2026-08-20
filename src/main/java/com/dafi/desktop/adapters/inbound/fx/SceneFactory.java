package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.application.auth.AuthenticateUserUseCase;
import com.dafi.desktop.application.client.ClientRepositoryPort;
import com.dafi.desktop.application.client.GetClientsUseCase;
import com.dafi.desktop.application.security.EncryptionPort;
import com.dafi.desktop.application.security.KeyStoragePort;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public class SceneFactory {

    private static AuthenticateUserUseCase authenticateUseCase;
    private static GetClientsUseCase clientsUseCase;
    private static EncryptionPort encryptionPort;
    private static KeyStoragePort keyStoragePort;
    private static ClientRepositoryPort clientRepositoryPort;

    public static void initialize(Path configDirectory, Path dataDirectory) {
        encryptionPort = new com.dafi.desktop.adapters.outbound.security.AesGcmEncryptionAdapter();
        keyStoragePort = new com.dafi.desktop.adapters.outbound.json.FileKeyStorageAdapter(configDirectory);

        var credentialsStorage = new com.dafi.desktop.adapters.outbound.json.FileCredentialsStorageAdapter(configDirectory);
        var passwordHasher = new com.dafi.desktop.adapters.outbound.security.Argon2PasswordHasherAdapter();
        authenticateUseCase = new AuthenticateUserUseCase(credentialsStorage, passwordHasher);

        clientRepositoryPort = new com.dafi.desktop.adapters.outbound.json.JsonClientRepositoryAdapter(
                dataDirectory, encryptionPort, keyStoragePort);
        clientsUseCase = new GetClientsUseCase(clientRepositoryPort);
    }

    public static Scene createLoginScene(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneFactory.class.getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAuthenticateUseCase(authenticateUseCase);
            controller.setPrimaryStage(primaryStage);

            return new Scene(root);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la vista de login", e);
        }
    }

    public Scene createMainScene(Stage primaryStage, AuthenticateUserUseCase authenticateUseCase) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setAuthenticateUseCase(authenticateUseCase);
            controller.setPrimaryStage(primaryStage);

            return new Scene(root);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la vista principal", e);
        }
    }

    public static GetClientsUseCase getClientsUseCase() {
        return clientsUseCase;
    }

    public static EncryptionPort getEncryptionPort() {
        return encryptionPort;
    }

    public static KeyStoragePort getKeyStoragePort() {
        return keyStoragePort;
    }

    public static ClientRepositoryPort getClientRepositoryPort() {
        return clientRepositoryPort;
    }
}
