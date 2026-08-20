# dafi-desktop structural map | imports shown only for com.dafi.desktop
# legend: cl=class if=interface rc=record en=enum; m=public/protected method; @fxml=FXML fields

src/main/java/com/dafi/desktop/adapters/inbound/fx/ClientViewModel.java > src/main/java/com/dafi/desktop/domain/client/Client.java
  cl ClientViewModel

src/main/java/com/dafi/desktop/adapters/inbound/fx/ClientsController.java > src/main/java/com/dafi/desktop/application/client/GetClientsUseCase.java, src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java, src/main/java/com/dafi/desktop/domain/client/Client.java, src/main/java/com/dafi/desktop/domain/client/ContractType.java, src/main/java/com/dafi/desktop/domain/client/PaymentMethod.java, src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl ClientsController
  @FXML private TableView<ClientViewModel> clientsTable; @FXML private TableColumn<ClientViewModel, String> folioColumn; @FXML private TableColumn<ClientViewModel, String> nameColumn; @FXML private TableColumn<ClientViewModel, String> vencimientoColumn; @FXML private TableColumn<ClientViewModel, String> contractTypeColumn; @FXML private TableColumn<ClientViewModel, String> phoneColumn; @FXML private TableColumn<ClientViewModel, String> neighborhoodColumn; @FXML private TableColumn<ClientViewModel, String> paymentMethodColumn; @FXML private TableColumn<ClientViewModel, String> contractDateColumn; @FXML private TableColumn<ClientViewModel, Integer> totalPaymentsColumn; @FXML private TableColumn<ClientViewModel, java.math.BigDecimal> monthlyPaymentColumn; @FXML private TableColumn<ClientViewModel, java.math.BigDecimal> totalBalanceColumn; @FXML private Label statusLabel; @FXML private Label loadingLabel; @FXML private Label clientsTitle; @FXML private Label clientsSubtitle; @FXML private Button newClientButton; @FXML private TextField searchField; @FXML private ProgressIndicator searchSpinner; @FXML private Label searchResultsLabel; @FXML private VBox tableContainer; @FXML private VBox formContainer; @FXML private ScrollPane formScrollPane; @FXML private TextField folioField; @FXML private TextField fullNameField; @FXML private TextField ineField; @FXML private ComboBox<ContractType> contractTypeCombo; @FXML private TextField addressField; @FXML private TextField neighborhoodField; @FXML private TextField phoneField; @FXML private TextField emailField; @FXML private ComboBox<PaymentMethod> paymentMethodCombo; @FXML private TextField firstBeneficiaryField; @FXML private TextField secondBeneficiaryField; @FXML private TextField saleDescriptionField; @FXML private TextField annuityField; @FXML private TextField blockField; @FXML private TextField lotField; @FXML private TextField managementFeeField; @FXML private TextField advanceField; @FXML private TextField totalBalanceField; @FXML private DatePicker firstPaymentDatePicker; @FXML private DatePicker contractDatePicker; @FXML private TextField paymentDayField; @FXML private TextField totalPaymentsField; @FXML private TextField monthlyPaymentField; @FXML private Button saveButton; @FXML private Label formErrorLabel; @FXML private Label formSuccessLabel; @FXML private Label labelFolio; @FXML private Label labelFullName; @FXML private Label labelIne; @FXML private Label labelContractType; @FXML private Label labelAddress; @FXML private Label labelNeighborhood; @FXML private Label labelPhone; @FXML private Label labelEmail; @FXML private Label labelPaymentMethod; @FXML private Label labelFirstBeneficiary; @FXML private Label labelSecondBeneficiary; @FXML private Label labelSaleDescription; @FXML private Label labelAnnuity; @FXML private Label labelBlock; @FXML private Label labelLot; @FXML private Label labelManagementFee; @FXML private Label labelAdvance; @FXML private Label labelTotalBalance; @FXML private Label labelFirstPaymentDate; @FXML private Label labelContractDate; @FXML private Label labelPaymentDay; @FXML private Label labelTotalPayments; @FXML private Label labelMonthlyPayment; @FXML private HBox managementFeeBox; @FXML private HBox advanceBox; @FXML private HBox totalBalanceBox; @FXML private HBox monthlyPaymentBox; @FXML @FXML @FXML
  @fxml x79

src/main/java/com/dafi/desktop/adapters/inbound/fx/LoginController.java > src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl LoginController
  @FXML @FXML @FXML @FXML @FXML @FXML
  @fxml x6

src/main/java/com/dafi/desktop/adapters/inbound/fx/MainController.java > src/main/java/com/dafi/desktop/infrastructure/I18n.java
  cl MainController
  @FXML @FXML @FXML @FXML @FXML @FXML @FXML @FXML
  @fxml x8

src/main/java/com/dafi/desktop/adapters/inbound/fx/SceneFactory.java > src/main/java/com/dafi/desktop/application/auth/AuthenticateUserUseCase.java, src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java, src/main/java/com/dafi/desktop/application/client/GetClientsUseCase.java, src/main/java/com/dafi/desktop/application/security/EncryptionPort.java, src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java
  cl SceneFactory

src/main/java/com/dafi/desktop/adapters/outbound/json/FileCredentialsStorageAdapter.java > src/main/java/com/dafi/desktop/application/auth/CredentialsStoragePort.java
  cl FileCredentialsStorageAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/FileKeyStorageAdapter.java > src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java
  cl FileKeyStorageAdapter

src/main/java/com/dafi/desktop/adapters/outbound/json/JsonClientRepositoryAdapter.java > src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java, src/main/java/com/dafi/desktop/application/security/EncryptionPort.java, src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java, src/main/java/com/dafi/desktop/domain/client/Client.java, src/main/java/com/dafi/desktop/domain/client/ContractType.java, src/main/java/com/dafi/desktop/domain/client/PaymentMethod.java
  cl JsonClientRepositoryAdapter

src/main/java/com/dafi/desktop/adapters/outbound/security/AesGcmEncryptionAdapter.java > src/main/java/com/dafi/desktop/application/security/EncryptionPort.java
  cl AesGcmEncryptionAdapter

src/main/java/com/dafi/desktop/adapters/outbound/security/Argon2PasswordHasherAdapter.java > src/main/java/com/dafi/desktop/application/auth/PasswordHasherPort.java
  cl Argon2PasswordHasherAdapter

src/main/java/com/dafi/desktop/application/auth/AuthenticateUserUseCase.java > 
  cl AuthenticateUserUseCase

src/main/java/com/dafi/desktop/application/auth/CredentialsStoragePort.java > 
  if CredentialsStoragePort

src/main/java/com/dafi/desktop/application/auth/PasswordHasherPort.java > 
  if PasswordHasherPort

src/main/java/com/dafi/desktop/application/client/ClientRepositoryPort.java > src/main/java/com/dafi/desktop/domain/client/Client.java
  if ClientRepositoryPort

src/main/java/com/dafi/desktop/application/client/GetClientsUseCase.java > src/main/java/com/dafi/desktop/domain/client/Client.java
  cl GetClientsUseCase

src/main/java/com/dafi/desktop/application/security/EncryptionPort.java > 
  if EncryptionPort

src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java > 
  if KeyStoragePort

src/main/java/com/dafi/desktop/domain/DomainException.java > 
  cl DomainException

src/main/java/com/dafi/desktop/domain/client/Client.java > 
  cl Client

src/main/java/com/dafi/desktop/domain/client/ContractType.java > 
  en ContractType

src/main/java/com/dafi/desktop/domain/client/PaymentMethod.java > 
  en PaymentMethod

src/main/java/com/dafi/desktop/infrastructure/DafiApplication.java > src/main/java/com/dafi/desktop/adapters/inbound/fx/SceneFactory.java, src/main/java/com/dafi/desktop/application/auth/AuthenticateUserUseCase.java, src/main/java/com/dafi/desktop/application/security/KeyStoragePort.java
  cl DafiApplication

src/main/java/com/dafi/desktop/infrastructure/I18n.java > 
  cl I18n
