# dafi-desktop FXML structure


src/main/resources/fxml/ClientsView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.ClientsController
  fx:ids: clientsTitle, clientsSubtitle, newClientButton, statusLabel, tableContainer, searchField, searchSpinner, searchResultsLabel, clientsTable, folioColumn, nameColumn, vencimientoColumn, contractTypeColumn, phoneColumn, neighborhoodColumn, paymentMethodColumn, contractDateColumn, totalPaymentsColumn, monthlyPaymentColumn, totalBalanceColumn, formContainer, formScrollPane, labelFolio, folioField, formErrorLabel, labelFullName, fullNameField, labelIne, ineField, labelContractType, contractTypeCombo, labelAddress, addressField, labelNeighborhood, neighborhoodField, labelPhone, phoneField, labelEmail, emailField, labelPaymentMethod, paymentMethodCombo, labelFirstBeneficiary, firstBeneficiaryField, labelSecondBeneficiary, secondBeneficiaryField, labelSaleDescription, saleDescriptionField, labelAnnuity, annuityField, labelBlock, blockField, labelLot, lotField, labelManagementFee, managementFeeBox, managementFeeField, labelAdvance, advanceBox, advanceField, labelTotalBalance, totalBalanceBox, totalBalanceField, labelFirstPaymentDate, firstPaymentDatePicker, labelContractDate, contractDatePicker, labelPaymentDay, paymentDayField, labelTotalPayments, totalPaymentsField, labelMonthlyPayment, monthlyPaymentBox, monthlyPaymentField, saveButton, formSuccessLabel, loadingLabel
  actions: handleNewClient, handleSaveClient

src/main/resources/fxml/LoginView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.LoginController
  fx:ids: usernameField, passwordField, errorLabel, loginButton
  actions: handleLogin, x1F511

src/main/resources/fxml/MainView.fxml
  controller: com.dafi.desktop.adapters.inbound.fx.MainController
  fx:ids: appTitle, appSubtitle, clientsButton, clientsLabel, versionLabel, contentArea
  actions: 5a6a8a, handleClientsClick, x1F465
