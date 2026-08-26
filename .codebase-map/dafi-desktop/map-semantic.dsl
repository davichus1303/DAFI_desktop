# dafi-desktop structural map | imports shown only for com.dafi.desktop
# legend: cl=class if=interface rc=record en=enum; m=public/protected method; @fxml=FXML fields

src/main/java/com/dafi/desktop/adapters/inbound/ExcelRow.java > 
  rc ExcelRow

src/main/java/com/dafi/desktop/adapters/inbound/ExcelRowReader.java > 
  cl ExcelRowReader

src/main/java/com/dafi/desktop/adapters/inbound/fx/AbstractCatalogEntryController.java > src/main/java/com/dafi/desktop/domain/shared/CatalogEntry.java, src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl AbstractCatalogEntryController
  @FXML protected TableView<CatalogEntryViewModel> entriesTable; @FXML protected TableColumn<CatalogEntryViewModel, String> nameColumn; @FXML protected TableColumn<CatalogEntryViewModel, String> descriptionColumn; @FXML protected Label titleLabel; @FXML protected Label subtitleLabel; @FXML protected Label statusLabel; @FXML protected Label loadingLabel; @FXML protected Button newTypeButton; @FXML protected Button backButton; @FXML protected TextField searchField; @FXML protected Label searchResultsLabel; @FXML protected VBox tableContainer; @FXML protected VBox formContainer; @FXML protected Label formTitleLabel; @FXML protected Label labelName; @FXML protected Label labelDescription; @FXML protected TextField nameField; @FXML protected Label nameErrorLabel; @FXML protected TextArea descriptionField; @FXML protected Label formErrorLabel; @FXML protected Label formSuccessLabel; @FXML protected Button saveButton; @FXML @FXML @FXML @FXML
  @fxml x26

src/main/java/com/dafi/desktop/adapters/inbound/fx/BulkImportHelper.java > src/main/java/com/dafi/desktop/application/client/BulkClientImportUseCase.java, src/main/java/com/dafi/desktop/application/client/BulkImportResult.java, src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl BulkImportHelper

src/main/java/com/dafi/desktop/adapters/inbound/fx/CatalogEntryViewModel.java > src/main/java/com/dafi/desktop/domain/shared/CatalogEntry.java
  cl CatalogEntryViewModel

src/main/java/com/dafi/desktop/adapters/inbound/fx/ClientFormController.java > src/main/java/com/dafi/desktop/application/contracttype/GetContractTypesUseCase.java, src/main/java/com/dafi/desktop/application/paymentmethod/GetPaymentMethodsUseCase.java, src/main/java/com/dafi/desktop/domain/client/Client.java, src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java, src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java, src/main/java/com/dafi/desktop/infrastructure/I18n.java, src/main/java/com/dafi/desktop/domain/client/Client.java
  cl ClientFormController
  @FXML private TextField folioField; @FXML private TextField fullNameField; @FXML private TextField ineField; @FXML private ComboBox<String> contractTypeCombo; @FXML private TextField addressField; @FXML private TextField neighborhoodField; @FXML private TextField phoneField; @FXML private TextField emailField; @FXML private ComboBox<String> paymentMethodCombo; @FXML private TextField firstBeneficiaryField; @FXML private TextField secondBeneficiaryField; @FXML private TextField saleDescriptionField; @FXML private TextField annuityField; @FXML private TextField blockField; @FXML private TextField lotField; @FXML private TextField managementFeeField; @FXML private TextField advanceField; @FXML private TextField totalBalanceField; @FXML private DatePicker firstPaymentDatePicker; @FXML private DatePicker contractDatePicker; @FXML private TextField paymentDayField; @FXML private TextField totalPaymentsField; @FXML private TextField monthlyPaymentField; @FXML private Button saveButton; @FXML private Button editButton; @FXML private Label formTitleLabel; @FXML private Label formErrorLabel; @FXML private Label formSuccessLabel; @FXML private Label labelFolio; @FXML private Label labelFullName; @FXML private Label labelIne; @FXML private Label labelContractType; @FXML private Label labelAddress; @FXML private Label labelNeighborhood; @FXML private Label labelPhone; @FXML private Label labelEmail; @FXML private Label labelPaymentMethod; @FXML private Label labelFirstBeneficiary; @FXML private Label labelSecondBeneficiary; @FXML private Label labelSaleDescription; @FXML private Label labelAnnuity; @FXML private Label labelBlock; @FXML private Label labelLot; @FXML private Label labelManagementFee; @FXML private Label labelAdvance; @FXML private Label labelTotalBalance; @FXML private Label labelFirstPaymentDate; @FXML private Label labelContractDate; @FXML private Label labelPaymentDay; @FXML private Label labelTotalPayments; @FXML private Label labelMonthlyPayment; @FXML private HBox managementFeeBox; @FXML private HBox advanceBox; @FXML private HBox totalBalanceBox; @FXML private HBox monthlyPaymentBox; @FXML @FXML @FXML
  @fxml x58

src/main/java/com/dafi/desktop/adapters/inbound/fx/ClientViewModel.java > src/main/java/com/dafi/desktop/domain/client/Client.java
  cl ClientViewModel

src/main/java/com/dafi/desktop/adapters/inbound/fx/ClientsController.java > src/main/java/com/dafi/desktop/application/client/BulkClientImportUseCase.java, src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java, src/main/java/com/dafi/desktop/application/client/GetClientsUseCase.java, src/main/java/com/dafi/desktop/application/contracttype/GetContractTypesUseCase.java, src/main/java/com/dafi/desktop/application/paymentmethod/GetPaymentMethodsUseCase.java, src/main/java/com/dafi/desktop/domain/client/Client.java, src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl ClientsController
  @FXML private TableView<ClientViewModel> clientsTable; @FXML private TableColumn<ClientViewModel, String> folioColumn; @FXML private TableColumn<ClientViewModel, String> nameColumn; @FXML private TableColumn<ClientViewModel, String> vencimientoColumn; @FXML private TableColumn<ClientViewModel, String> contractTypeColumn; @FXML private TableColumn<ClientViewModel, String> phoneColumn; @FXML private TableColumn<ClientViewModel, String> neighborhoodColumn; @FXML private TableColumn<ClientViewModel, String> paymentMethodColumn; @FXML private TableColumn<ClientViewModel, String> contractDateColumn; @FXML private TableColumn<ClientViewModel, Integer> totalPaymentsColumn; @FXML private TableColumn<ClientViewModel, BigDecimal> monthlyPaymentColumn; @FXML private TableColumn<ClientViewModel, BigDecimal> totalBalanceColumn; @FXML private Label statusLabel; @FXML private Label loadingLabel; @FXML private Label clientsTitle; @FXML private Label clientsSubtitle; @FXML private Button newClientButton; @FXML private Button bulkLoadButton; @FXML private Button keyToolsButton; @FXML private TextField searchField; @FXML private ProgressIndicator searchSpinner; @FXML private Label searchResultsLabel; @FXML private VBox tableContainer; @FXML private VBox clientForm; @FXML private ClientFormController clientFormController; @FXML @FXML @FXML @FXML
  @fxml x29

src/main/java/com/dafi/desktop/adapters/inbound/fx/ContractTypeCatalogController.java > src/main/java/com/dafi/desktop/application/contracttype/ContractTypeCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/application/contracttype/GetContractTypesUseCase.java, src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java
  cl ContractTypeCatalogController
  @FXML
  @fxml x1

src/main/java/com/dafi/desktop/adapters/inbound/fx/KeyToolsHelper.java > src/main/java/com/dafi/desktop/application/security/ExportEncryptionKeyUseCase.java, src/main/java/com/dafi/desktop/application/security/ImportEncryptionKeyUseCase.java, src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl KeyToolsHelper

src/main/java/com/dafi/desktop/adapters/inbound/fx/LoginController.java > src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl LoginController
  @FXML @FXML @FXML @FXML @FXML @FXML
  @fxml x6

src/main/java/com/dafi/desktop/adapters/inbound/fx/MainController.java > src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl MainController
  @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML
  @fxml x15

src/main/java/com/dafi/desktop/adapters/inbound/fx/PaymentMethodCatalogController.java > src/main/java/com/dafi/desktop/application/paymentmethod/GetPaymentMethodsUseCase.java, src/main/java/com/dafi/desktop/application/paymentmethod/PaymentMethodCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java
  cl PaymentMethodCatalogController

src/main/java/com/dafi/desktop/adapters/inbound/fx/SceneFactory.java > src/main/java/com/dafi/desktop/adapters/outbound/json/FileCredentialsStorageAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/json/FileKeyStorageAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/json/JsonClientRepositoryAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/json/JsonContractTypeCatalogRepositoryAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/json/JsonPaymentMethodCatalogRepositoryAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/security/AesGcmEncryptionAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/security/Argon2PasswordHasherAdapter.java, src/main/java/com/dafi/desktop/adapters/outbound/security/OsKeyringKeyStorageAdapter.java, src/main/java/com/dafi/desktop/application/auth/AuthenticateUserUseCase.java, src/main/java/com/dafi/desktop/application/client/BulkClientImportUseCase.java, src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java, src/main/java/com/dafi/desktop/application/client/GetClientsUseCase.java, src/main/java/com/dafi/desktop/application/contracttype/ContractTypeCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/application/contracttype/GetContractTypesUseCase.java, src/main/java/com/dafi/desktop/application/paymentmethod/GetPaymentMethodsUseCase.java, src/main/java/com/dafi/desktop/application/paymentmethod/PaymentMethodCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/application/security/EncryptionPort.java, src/main/java/com/dafi/desktop/application/security/ExportEncryptionKeyUseCase.java, src/main/java/com/dafi/desktop/application/security/ImportEncryptionKeyUseCase.java, src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java, src/main/java/com/dafi/desktop/adapters/outbound/CryptoUtils.java
  cl SceneFactory

src/main/java/com/dafi/desktop/adapters/inbound/fx/SearchDebounceUtils.java > 
  cl SearchDebounceUtils

src/main/java/com/dafi/desktop/adapters/outbound/BulkImportReportWriter.java > src/main/java/com/dafi/desktop/application/client/BulkImportResult.java, src/main/java/com/dafi/desktop/application/client/RowRejection.java
  cl BulkImportReportWriter

src/main/java/com/dafi/desktop/adapters/outbound/CryptoUtils.java > src/main/java/com/dafi/desktop/application/security/EncryptionPort.java, src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java
  cl CryptoUtils

src/main/java/com/dafi/desktop/adapters/outbound/json/AbstractJsonCatalogRepositoryAdapter.java > src/main/java/com/dafi/desktop/application/catalog/CatalogEntryRepositoryPort.java, src/main/java/com/dafi/desktop/domain/shared/CatalogEntry.java, src/main/java/com/dafi/desktop/adapters/outbound/CryptoUtils.java, src/main/java/com/dafi/desktop/shared/utils/JsonObjectReader.java
  cl AbstractJsonCatalogRepositoryAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/FileCredentialsStorageAdapter.java > src/main/java/com/dafi/desktop/application/auth/CredentialsStoragePort.java
  cl FileCredentialsStorageAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/FileKeyStorageAdapter.java > src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java
  cl FileKeyStorageAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/JsonClientRepositoryAdapter.java > src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java, src/main/java/com/dafi/desktop/domain/DomainException.java, src/main/java/com/dafi/desktop/domain/client/Client.java, src/main/java/com/dafi/desktop/adapters/outbound/CryptoUtils.java, src/main/java/com/dafi/desktop/shared/utils/JsonObjectReader.java
  cl JsonClientRepositoryAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/JsonContractTypeCatalogRepositoryAdapter.java > src/main/java/com/dafi/desktop/application/contracttype/ContractTypeCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java, src/main/java/com/dafi/desktop/adapters/outbound/CryptoUtils.java
  cl JsonContractTypeCatalogRepositoryAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/JsonPaymentMethodCatalogRepositoryAdapter.java > src/main/java/com/dafi/desktop/application/paymentmethod/PaymentMethodCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java, src/main/java/com/dafi/desktop/adapters/outbound/CryptoUtils.java
  cl JsonPaymentMethodCatalogRepositoryAdapter

src/main/java/com/dafi/desktop/adapters/outbound/security/AesGcmEncryptionAdapter.java > src/main/java/com/dafi/desktop/application/security/EncryptionPort.java
  cl AesGcmEncryptionAdapter

src/main/java/com/dafi/desktop/adapters/outbound/security/Argon2PasswordHasherAdapter.java > src/main/java/com/dafi/desktop/application/auth/PasswordHasherPort.java
  cl Argon2PasswordHasherAdapter

src/main/java/com/dafi/desktop/adapters/outbound/security/OsKeyringKeyStorageAdapter.java > src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java
  cl OsKeyringKeyStorageAdapter

src/main/java/com/dafi/desktop/application/auth/AuthenticateUserUseCase.java > 
  cl AuthenticateUserUseCase

src/main/java/com/dafi/desktop/application/auth/CredentialsStoragePort.java > 
  if CredentialsStoragePort

src/main/java/com/dafi/desktop/application/auth/PasswordHasherPort.java > 
  if PasswordHasherPort

src/main/java/com/dafi/desktop/application/catalog/CatalogEntryRepositoryPort.java > src/main/java/com/dafi/desktop/domain/shared/CatalogEntry.java
  if CatalogEntryRepositoryPort

src/main/java/com/dafi/desktop/application/client/BulkClientImportUseCase.java > src/main/java/com/dafi/desktop/application/contracttype/ContractTypeCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/application/paymentmethod/PaymentMethodCatalogRepositoryPort.java, src/main/java/com/dafi/desktop/domain/DomainException.java, src/main/java/com/dafi/desktop/domain/client/Client.java, src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java, src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java, src/main/java/com/dafi/desktop/domain/shared/Email.java, src/main/java/com/dafi/desktop/adapters/inbound/ExcelRow.java, src/main/java/com/dafi/desktop/adapters/inbound/ExcelRowReader.java, src/main/java/com/dafi/desktop/adapters/outbound/BulkImportReportWriter.java
  cl BulkClientImportUseCase

src/main/java/com/dafi/desktop/application/client/BulkImportResult.java > 
  rc BulkImportResult

src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java > src/main/java/com/dafi/desktop/domain/client/Client.java
  if ClientRepositoryPort

src/main/java/com/dafi/desktop/application/client/GetClientsUseCase.java > src/main/java/com/dafi/desktop/domain/client/Client.java
  cl GetClientsUseCase

src/main/java/com/dafi/desktop/application/client/RowRejection.java > 
  rc RowRejection

src/main/java/com/dafi/desktop/application/contracttype/ContractTypeCatalogRepositoryPort.java > src/main/java/com/dafi/desktop/application/catalog/CatalogEntryRepositoryPort.java, src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java
  if ContractTypeCatalogRepositoryPort

src/main/java/com/dafi/desktop/application/contracttype/GetContractTypesUseCase.java > src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java
  cl GetContractTypesUseCase

src/main/java/com/dafi/desktop/application/paymentmethod/GetPaymentMethodsUseCase.java > src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java
  cl GetPaymentMethodsUseCase

src/main/java/com/dafi/desktop/application/paymentmethod/PaymentMethodCatalogRepositoryPort.java > src/main/java/com/dafi/desktop/application/catalog/CatalogEntryRepositoryPort.java, src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java
  if PaymentMethodCatalogRepositoryPort

src/main/java/com/dafi/desktop/application/security/EncryptionPort.java > 
  if EncryptionPort

src/main/java/com/dafi/desktop/application/security/ExportEncryptionKeyUseCase.java > 
  cl ExportEncryptionKeyUseCase

src/main/java/com/dafi/desktop/application/security/ImportEncryptionKeyUseCase.java > 
  cl ImportEncryptionKeyUseCase

src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java > 
  if KeyStoragePort

src/main/java/com/dafi/desktop/domain/DomainException.java > 
  cl DomainException

src/main/java/com/dafi/desktop/domain/client/Client.java > src/main/java/com/dafi/desktop/domain/DomainException.java, src/main/java/com/dafi/desktop/domain/shared/Email.java
  cl Client

src/main/java/com/dafi/desktop/domain/contracttype/ContractTypeCatalog.java > src/main/java/com/dafi/desktop/domain/shared/AbstractCatalogEntry.java
  cl ContractTypeCatalog

src/main/java/com/dafi/desktop/domain/paymentmethod/PaymentMethodCatalog.java > src/main/java/com/dafi/desktop/domain/shared/AbstractCatalogEntry.java
  cl PaymentMethodCatalog

src/main/java/com/dafi/desktop/domain/shared/AbstractCatalogEntry.java > 
  cl AbstractCatalogEntry

src/main/java/com/dafi/desktop/domain/shared/CatalogEntry.java > 
  if CatalogEntry

src/main/java/com/dafi/desktop/domain/shared/Email.java > 
  cl Email

src/main/java/com/dafi/desktop/infrastructure/DafiApplication.java > src/main/java/com/dafi/desktop/adapters/inbound/fx/SceneFactory.java, src/main/java/com/dafi/desktop/application/auth/AuthenticateUserUseCase.java
  cl of

src/main/java/com/dafi/desktop/infrastructure/DafiLauncher.java > 
  cl DafiLauncher

src/main/java/com/dafi/desktop/infrastructure/I18n.java > 
  cl I18n

src/main/java/com/dafi/desktop/shared/utils/JsonObjectReader.java > 
  cl JsonObjectReader
