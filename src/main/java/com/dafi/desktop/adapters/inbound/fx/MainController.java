package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.infrastructure.I18n;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller for the main window with the sidebar navigation.
 * Loads each section view into the content area and injects its
 * dependencies from {@link SceneFactory}.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private VBox contentArea;

    @FXML
    private HBox clientsButton;

    @FXML
    private Label clientsLabel;

    @FXML
    private HBox contractTypesButton;

    @FXML
    private Label contractTypesLabel;

    @FXML
    private HBox paymentMethodsButton;

    @FXML
    private Label paymentMethodsLabel;

    @FXML
    private Label appTitle;

    @FXML
    private Label appBrand;

    @FXML
    private Label appSubtitle;

    @FXML
    private Label versionLabel;

    private com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase;
    private Stage primaryStage;
    private HBox activeNavItem;

    /**
     * Applies i18n texts, highlights the default section and shows the clients view.
     */
    @FXML
    public void initialize() {
        I18n i18n = I18n.getInstance();
        appTitle.setText(i18n.get("app.title"));
        appBrand.setText(i18n.get("app.brand"));
        appSubtitle.setText(i18n.get("app.subtitle"));
        versionLabel.setText(i18n.get("app.version"));

        clientsLabel.setText(i18n.get("sidebar.clients"));
        contractTypesLabel.setText(i18n.get("sidebar.contractTypes"));
        paymentMethodsLabel.setText(i18n.get("sidebar.paymentMethods"));

        clientsButton.setCursor(Cursor.HAND);
        contractTypesButton.setCursor(Cursor.HAND);
        paymentMethodsButton.setCursor(Cursor.HAND);

        loadClientsView();
        setActiveNav(clientsButton);
    }

    /**
     * Sets the authentication use case passed down to the section views.
     *
     * @param authenticateUseCase authentication use case
     */
    public void setAuthenticateUseCase(com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    /**
     * Sets the primary stage hosting the main scene.
     *
     * @param primaryStage application primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles the click on the Clients sidebar button.
     */
    @FXML
    private void handleClientsClick(MouseEvent event) {
        loadClientsView();
        setActiveNav(clientsButton);
    }

    /**
     * Handles the click on the Contract Types sidebar button.
     */
    @FXML
    private void handleContractTypesClick(MouseEvent event) {
        loadContractTypesView();
        setActiveNav(contractTypesButton);
    }

    /**
     * Handles the click on the Payment Types sidebar button.
     */
    @FXML
    private void handlePaymentMethodsClick(MouseEvent event) {
        loadPaymentMethodsView();
        setActiveNav(paymentMethodsButton);
    }

    private void loadClientsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientsView.fxml"));
            Parent clientsView = loader.load();

            ClientsController controller = loader.getController();
            controller.setDependencies(
                    SceneFactory.getClientsUseCase(),
                    SceneFactory.getClientRepositoryPort(),
                    SceneFactory.getBulkClientImportUseCase(),
                    SceneFactory.getContractTypesUseCase(),
                    SceneFactory.getPaymentMethodsUseCase(),
                    SceneFactory.getExportEncryptionKeyUseCase(),
                    SceneFactory.getImportEncryptionKeyUseCase()
            );

            displayView(clientsView);
        } catch (IOException e) {
            showViewLoadError("Error al cargar la vista de clientes", e);
        }
    }

    private void loadContractTypesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ContractTypeCatalogView.fxml"));
            Parent view = loader.load();

            ContractTypeCatalogController controller = loader.getController();
            controller.setDependencies(
                    SceneFactory.getContractTypesUseCase(),
                    SceneFactory.getContractTypeCatalogRepositoryPort(),
                    SceneFactory.getExportEncryptionKeyUseCase(),
                    SceneFactory.getImportEncryptionKeyUseCase()
            );

            displayView(view);
        } catch (IOException e) {
            showViewLoadError("Error al cargar la vista de tipos de contrato", e);
        }
    }

    private void loadPaymentMethodsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PaymentMethodCatalogView.fxml"));
            Parent view = loader.load();

            PaymentMethodCatalogController controller = loader.getController();
            controller.setDependencies(
                    SceneFactory.getPaymentMethodsUseCase(),
                    SceneFactory.getPaymentMethodCatalogRepositoryPort()
            );

            displayView(view);
        } catch (IOException e) {
            showViewLoadError("Error al cargar la vista de tipos de pago", e);
        }
    }

    private void displayView(Parent view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void showViewLoadError(String errorContext, IOException e) {
        log.error(errorContext, e);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Error al cargar la vista: " + e.getMessage()));
    }

    private void setActiveNav(HBox navItem) {
        if (activeNavItem != null && activeNavItem != navItem) {
            activeNavItem.getStyleClass().remove("nav-item-active");
        }
        if (!navItem.getStyleClass().contains("nav-item-active")) {
            navItem.getStyleClass().add("nav-item-active");
        }
        activeNavItem = navItem;
    }
}
