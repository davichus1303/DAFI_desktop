package com.dafi.desktop.adapters.inbound.fx;

import com.dafi.desktop.infrastructure.I18n;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
 * Controlador de la ventana principal con barra lateral.
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
    private Label appTitle;

    @FXML
    private Label appSubtitle;

    @FXML
    private Label versionLabel;

    private com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase;
    private Stage primaryStage;
    private HBox activeNavItem;

    /**
     * Inicializa el controlador después de que el FXML se ha cargado.
     */
    @FXML
    public void initialize() {
        I18n i18n = I18n.getInstance();
        appTitle.setText(i18n.get("app.title"));
        appSubtitle.setText(i18n.get("app.subtitle"));
        versionLabel.setText(i18n.get("app.version"));

        clientsLabel.setText(i18n.get("sidebar.clients"));

        clientsButton.setCursor(Cursor.HAND);

        loadClientsView();
        setActiveNav(clientsButton);
    }

    /**
     * Establece el caso de uso de autenticación.
     *
     * @param authenticateUseCase caso de uso
     */
    public void setAuthenticateUseCase(com.dafi.desktop.application.auth.AuthenticateUserUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    /**
     * Establece el stage principal.
     *
     * @param primaryStage stage principal
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Maneja el clic en el botón de Clientes.
     */
    @FXML
    private void handleClientsClick(MouseEvent event) {
        loadClientsView();
        setActiveNav(clientsButton);
    }

    private void loadClientsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientsView.fxml"));
            Parent clientsView = loader.load();

            ClientsController controller = loader.getController();
            controller.setDependencies(
                    SceneFactory.getClientsUseCase(),
                    SceneFactory.getClientRepositoryPort()
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(clientsView);
        } catch (IOException e) {
            log.error("Error al cargar la vista de clientes", e);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(new Label("Error al cargar la vista de clientes: " + e.getMessage()));
        }
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
