# dafi-desktop FXML structure


src/main/resources/fxml/ClientFormView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.ClientFormController
  fx:ids: formTitleLabel, editButton, formScrollPane, labelFolio, folioField, formErrorLabel, labelFullName, fullNameField, labelIne, ineField, labelContractType, contractTypeCombo, labelAddress, addressField, labelNeighborhood, neighborhoodField, labelPhone, phoneField, labelEmail, emailField, labelPaymentMethod, paymentMethodCombo, labelFirstBeneficiary, firstBeneficiaryField, labelSecondBeneficiary, secondBeneficiaryField, labelSaleDescription, saleDescriptionField, labelAnnuity, annuityField, labelBlock, blockField, labelLot, lotField, labelManagementFee, managementFeeBox, managementFeeField, labelAdvance, advanceBox, advanceField, labelTotalBalance, totalBalanceBox, totalBalanceField, labelFirstPaymentDate, firstPaymentDatePicker, labelContractDate, contractDatePicker, labelPaymentDay, paymentDayField, labelTotalPayments, totalPaymentsField, labelMonthlyPayment, monthlyPaymentBox, monthlyPaymentField, saveButton, formSuccessLabel
  actions: handleEditClient, handleSaveClient

src/main/resources/fxml/ClientsView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.ClientsController
  fx:ids: clientsTitle, clientsSubtitle, keyToolsButton, newClientButton, bulkLoadButton, statusLabel, tableContainer, searchField, searchSpinner, searchResultsLabel, clientsTable, folioColumn, nameColumn, vencimientoColumn, contractTypeColumn, phoneColumn, neighborhoodColumn, paymentMethodColumn, contractDateColumn, totalPaymentsColumn, monthlyPaymentColumn, totalBalanceColumn, clientForm, loadingLabel
  actions: handleBulkLoad, handleNewClient, showKeyToolsMenu, x1F527

src/main/resources/fxml/ContractTypeCatalogView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.ContractTypeCatalogController
  fx:ids: titleLabel, subtitleLabel, newTypeButton, backButton, statusLabel, tableContainer, searchField, searchResultsLabel, entriesTable, nameColumn, descriptionColumn, formContainer, formTitleLabel, labelName, nameField, nameErrorLabel, labelDescription, descriptionField, formErrorLabel, formSuccessLabel, saveButton, loadingLabel
  actions: handleBack, handleNewType, handleSave

src/main/resources/fxml/LoginView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.LoginController
  fx:ids: usernameField, passwordField, errorLabel, loginButton
  actions: handleLogin, x1F511

src/main/resources/fxml/MainView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.MainController
  fx:ids: appTitle, appBrand, appSubtitle, clientsButton, clientsLabel, contractTypesButton, contractTypesLabel, paymentMethodsButton, paymentMethodsLabel, versionLabel, contentArea
  actions: 5a6a8a, handleClientsClick, handleContractTypesClick, handlePaymentMethodsClick, x1F465, x1F4B3, x1F4CB

src/main/resources/fxml/PaymentMethodCatalogView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.PaymentMethodCatalogController
  fx:ids: titleLabel, subtitleLabel, newTypeButton, backButton, statusLabel, tableContainer, searchField, searchResultsLabel, entriesTable, nameColumn, descriptionColumn, formContainer, formTitleLabel, labelName, nameField, nameErrorLabel, labelDescription, descriptionField, formErrorLabel, formSuccessLabel, saveButton, loadingLabel
  actions: handleBack, handleNewType, handleSave
