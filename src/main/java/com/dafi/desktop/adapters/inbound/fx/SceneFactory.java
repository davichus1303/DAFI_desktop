package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.adapters.outbound.json.FileCredentialsStorageAdapter;
import com.dafi.desktop.adapters.outbound.json.FileKeyStorageAdapter;
import com.dafi.desktop.adapters.outbound.json.JsonClientRepositoryAdapter;
import com.dafi.desktop.adapters.outbound.json.JsonContractTypeCatalogRepositoryAdapter;
import com.dafi.desktop.adapters.outbound.json.JsonPaymentMethodCatalogRepositoryAdapter;
import com.dafi.desktop.adapters.outbound.security.AesGcmEncryptionAdapter;
import com.dafi.desktop.adapters.outbound.security.Argon2PasswordHasherAdapter;
import com.dafi.desktop.adapters.outbound.security.OsKeyringKeyStorageAdapter;
import com.dafi.desktop.application.auth.AuthenticateUserUseCase;
import com.dafi.desktop.application.client.BulkClientImportUseCase;
import com.dafi.desktop.application.client.ClientRepositoryPort;
import com.dafi.desktop.application.client.GetClientsUseCase;
import com.dafi.desktop.application.contracttype.ContractTypeCatalogRepositoryPort;
import com.dafi.desktop.application.contracttype.GetContractTypesUseCase;
import com.dafi.desktop.application.paymentmethod.GetPaymentMethodsUseCase;
import com.dafi.desktop.application.paymentmethod.PaymentMethodCatalogRepositoryPort;
import com.dafi.desktop.application.security.EncryptionPort;
import com.dafi.desktop.application.security.ExportEncryptionKeyUseCase;
import com.dafi.desktop.application.security.ImportEncryptionKeyUseCase;
import com.dafi.desktop.application.security.KeyStoragePort;
import com.dafi.desktop.adapters.outbound.CryptoUtils;
import com.github.javakeyring.BackendNotSupportedException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Static composition root for the application: builds the outbound adapters,
 * wires them into application-layer use cases and exposes JavaFX scenes and
 * dependencies to the UI controllers.
 * Dependencies are held as static singletons initialized once by {@link #initialize}.
 */
public class SceneFactory {

    private static final Logger log = LoggerFactory.getLogger(SceneFactory.class);

    private static final String CSS_PATH = SceneFactory.class.getResource("/css/styles.css").toExternalForm();

    private static AuthenticateUserUseCase authenticateUseCase;
    private static GetClientsUseCase clientsUseCase;
    private static GetContractTypesUseCase contractTypesUseCase;
    private static GetPaymentMethodsUseCase paymentMethodsUseCase;
    private static EncryptionPort encryptionPort;
    private static KeyStoragePort keyStoragePort;
    private static ClientRepositoryPort clientRepositoryPort;
    private static BulkClientImportUseCase bulkClientImportUseCase;
    private static ContractTypeCatalogRepositoryPort contractTypeCatalogRepositoryPort;
    private static PaymentMethodCatalogRepositoryPort paymentMethodCatalogRepositoryPort;
    private static ExportEncryptionKeyUseCase exportEncryptionKeyUseCase;
    private static ImportEncryptionKeyUseCase importEncryptionKeyUseCase;

    /**
     * Builds every adapter and use case from the given directories.
     * Must be called once before any scene creation or dependency lookup.
     *
     * @param configDirectory directory holding configuration (encryption keys, credentials)
     * @param dataDirectory directory holding the encrypted JSON repositories
     */
    public static void initialize(Path configDirectory, Path dataDirectory) {
        keyStoragePort = createKeyStorage(configDirectory);
        encryptionPort = new AesGcmEncryptionAdapter();
        CryptoUtils cryptoUtils = new CryptoUtils(encryptionPort, keyStoragePort);

        var credentialsStorage = new FileCredentialsStorageAdapter(configDirectory);
        var passwordHasher = new Argon2PasswordHasherAdapter();
        authenticateUseCase = new AuthenticateUserUseCase(credentialsStorage, passwordHasher);

        clientRepositoryPort = new JsonClientRepositoryAdapter(dataDirectory, cryptoUtils);
        clientsUseCase = new GetClientsUseCase(clientRepositoryPort);

        contractTypeCatalogRepositoryPort = new JsonContractTypeCatalogRepositoryAdapter(dataDirectory, cryptoUtils);
        contractTypesUseCase = new GetContractTypesUseCase(contractTypeCatalogRepositoryPort);

        paymentMethodCatalogRepositoryPort = new JsonPaymentMethodCatalogRepositoryAdapter(dataDirectory, cryptoUtils);
        paymentMethodsUseCase = new GetPaymentMethodsUseCase(paymentMethodCatalogRepositoryPort);

        bulkClientImportUseCase = new BulkClientImportUseCase(clientRepositoryPort,
                contractTypeCatalogRepositoryPort, paymentMethodCatalogRepositoryPort);

        exportEncryptionKeyUseCase = new ExportEncryptionKeyUseCase(keyStoragePort);
        importEncryptionKeyUseCase = new ImportEncryptionKeyUseCase(keyStoragePort);
    }

    /**
     * Selects the key storage implementation: prefers the OS keyring
     * (Windows Credential Manager, Linux Secret Service/KWallet, macOS
     * Keychain) and falls back to the plain file adapter when no system
     * backend is available. On first run with a keyring present, any legacy
     * key file is migrated into the keyring and securely removed.
     *
     * @param configDirectory directory where the fallback key file lives
     * @return the selected key storage port
     */
    private static KeyStoragePort createKeyStorage(Path configDirectory) {
        FileKeyStorageAdapter fileStorage = new FileKeyStorageAdapter(configDirectory);
        try {
            OsKeyringKeyStorageAdapter osKeyring = new OsKeyringKeyStorageAdapter();
            migrateFileKeyToOsKeyring(fileStorage, osKeyring);
            registerKeyringShutdownHook(osKeyring);
            log.info("Almacenamiento de clave: keyring del sistema");
            return osKeyring;
        } catch (BackendNotSupportedException | RuntimeException e) {
            log.warn("Keyring del sistema no disponible ({}); usando archivo de clave local", e.getMessage());
            return fileStorage;
        }
    }

    /**
     * Ensures the keyring connection is released when the JVM exits; without
     * this hook the DBus worker threads would keep the process alive after
     * the last window closes.
     */
    private static void registerKeyringShutdownHook(OsKeyringKeyStorageAdapter osKeyring) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                osKeyring.close();
            } catch (RuntimeException e) {
                log.debug("Error al cerrar el keyring durante el apagado", e);
            }
        }, "dafi-keyring-shutdown"));
    }

    private static void migrateFileKeyToOsKeyring(FileKeyStorageAdapter fileStorage,
                                                  OsKeyringKeyStorageAdapter osKeyring) {
        if (osKeyring.hasStoredKey()) {
            return;
        }

        String legacyKey = fileStorage.getEncryptionKey();
        if (legacyKey == null) {
            return;
        }

        osKeyring.storeEncryptionKey(legacyKey);
        fileStorage.deleteStoredKeyFileSecurely();
        log.info("Clave de cifrado migrada del archivo local al keyring del sistema");
    }

    /**
     * Loads the login view, injects its dependencies and returns its scene.
     *
     * @param primaryStage application primary stage
     * @return the configured login scene
     * @throws IllegalStateException if the FXML cannot be loaded
     */
    public static Scene createLoginScene(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneFactory.class.getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAuthenticateUseCase(authenticateUseCase);
            controller.setPrimaryStage(primaryStage);

            return buildScene(root);
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar la vista de login", e);
        }
    }

    /**
     * Loads the main view, injects its dependencies and returns its scene.
     *
     * @param primaryStage application primary stage
     * @param authenticatedUseCase authentication use case forwarded to the main controller
     * @return the configured main scene
     * @throws IllegalStateException if the FXML cannot be loaded
     */
    public static Scene createMainScene(Stage primaryStage, AuthenticateUserUseCase authenticatedUseCase) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneFactory.class.getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setAuthenticateUseCase(authenticatedUseCase);
            controller.setPrimaryStage(primaryStage);

            return buildScene(root);
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar la vista principal", e);
        }
    }

    private static Scene buildScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS_PATH);
        return scene;
    }

    /** Returns the use case listing clients. */
    public static GetClientsUseCase getClientsUseCase() { return clientsUseCase; }

    /** Returns the selected key storage port (OS keyring or file fallback). */
    public static KeyStoragePort getKeyStoragePort() { return keyStoragePort; }

    /** Returns the port persisting clients. */
    public static ClientRepositoryPort getClientRepositoryPort() { return clientRepositoryPort; }

    /** Returns the use case importing clients from Excel files. */
    public static BulkClientImportUseCase getBulkClientImportUseCase() { return bulkClientImportUseCase; }

    /** Returns the use case listing contract types. */
    public static GetContractTypesUseCase getContractTypesUseCase() { return contractTypesUseCase; }

    /** Returns the port persisting contract type entries. */
    public static ContractTypeCatalogRepositoryPort getContractTypeCatalogRepositoryPort() {
        return contractTypeCatalogRepositoryPort;
    }

    /** Returns the use case listing payment methods. */
    public static GetPaymentMethodsUseCase getPaymentMethodsUseCase() { return paymentMethodsUseCase; }

    /** Returns the use case exporting the encryption key to a text file. */
    public static ExportEncryptionKeyUseCase getExportEncryptionKeyUseCase() {
        return exportEncryptionKeyUseCase;
    }

    /** Returns the use case importing an encryption key from a text file. */
    public static ImportEncryptionKeyUseCase getImportEncryptionKeyUseCase() {
        return importEncryptionKeyUseCase;
    }

    /** Returns the port persisting payment method entries. */
    public static PaymentMethodCatalogRepositoryPort getPaymentMethodCatalogRepositoryPort() {
        return paymentMethodCatalogRepositoryPort;
    }
}
