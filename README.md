# DAFI Desktop

**Despacho de Asesoria Funeraria Integral** - Client management system

## Description

Desktop application for managing clients of a funeral services firm. Allows managing contract information, clients, contract types, payment methods, and performing bulk imports from Excel.

## Features

- Secure authentication with Argon2id
- Data encryption with AES-256-GCM
- Exportable/importable encryption key across machines
- Local persistence in encrypted JSON under XDG dirs (`~/.config/dafi/`, `~/.local/share/dafi/`)
- Graphical interface with JavaFX 21
- Bulk client import from Excel (.xlsx/.xls) with validation and error reporting
- Real-time search with debounce
- Internationalization (i18n) in Spanish
- Custom application icon
- Windows packaging with jpackage

## Architecture

Hexagonal Architecture (Ports and Adapters):

```
domain/           Immutable entities, value objects, business rules
application/      Use cases and ports (interfaces)
adapters/inbound/ FXML controllers, Excel readers
adapters/outbound/ JSON repositories, encryption, password hashing
infrastructure/   Entry point, configuration, i18n
```

## Project structure

```
dafi-desktop/
├── src/
│   ├── main/java/com/dafi/desktop/
│   │   ├── domain/
│   │   │   ├── client/Client.java                 # Main entity (25 fields, Builder)
│   │   │   ├── contracttype/ContractTypeCatalog.java
│   │   │   ├── paymentmethod/PaymentMethodCatalog.java
│   │   │   ├── shared/
│   │   │   │   ├── AbstractCatalogEntry.java       # Immutable base for catalogs
│   │   │   │   ├── CatalogEntry.java               # Catalog interface
│   │   │   │   └── Email.java                      # Validated value object
│   │   │   └── DomainException.java
│   │   ├── application/
│   │   │   ├── auth/                               # Authentication (ports + use case)
│   │   │   ├── client/                             # Clients (CRUD + bulk import)
│   │   │   ├── contracttype/                       # Contract type catalog
│   │   │   ├── paymentmethod/                      # Payment method catalog
│   │   │   ├── catalog/                            # Generic repository port
│   │   │   └── security/                           # Encryption, keys, export/import
│   │   ├── adapters/
│   │   │   ├── inbound/
│   │   │   │   ├── ExcelRow.java                   # Excel row record
│   │   │   │   ├── ExcelRowReader.java             # Generic Excel reader
│   │   │   │   └── fx/                             # JavaFX controllers
│   │   │   └── outbound/
│   │   │       ├── json/                           # Encrypted JSON repositories
│   │   │       ├── security/                       # AES-GCM, Argon2, OS-keyring
│   │   │       ├── CryptoUtils.java                # Encryption/file utility
│   │   │       └── BulkImportReportWriter.java     # Report generator
│   │   ├── infrastructure/
│   │   │   ├── DafiApplication.java                # JavaFX entry point
│   │   │   ├── DafiLauncher.java                   # Non-JavaFX launcher
│   │   │   └── I18n.java                           # Internationalization
│   │   └── shared/utils/
│   │       ├── BaseDirectory.java                    # XDG base directory resolution
│   │       └── JsonObjectReader.java                 # Typed JSON reader
│   ├── main/resources/
│   │   ├── fxml/                                     # FXML views
│   │   ├── css/                                      # CSS styles
│   │   ├── i18n/es.json                              # Spanish text strings
│   │   └── icons/                                    # Window icons (16-256px)
│   └── test/                                         # 48 unit tests
├── packaging/
│   ├── dafi.ico                                    # Windows icon
│   ├── io.github.davichus1303.DafiDesktop.desktop  # Linux desktop entry
│   └── flatpak/io.github.davichus1303.DafiDesktop.yml  # Flatpak manifest
├── pom.xml
└── run.sh                                          # Quick run script
```

## Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Java | 17+ | 21 |
| Maven | 3.6+ | 3.9+ |
| OS | Linux, Windows, macOS | Any with JavaFX 21 |

## Running

### Linux / macOS

```bash
# Compile
mvn clean compile

# Run
mvn javafx:run

# Or use the quick script
chmod +x run.sh
./run.sh
```

### Windows

```bash
# Compile
mvn clean compile

# Run
mvn javafx:run
```

### Running tests

```bash
mvn test
```

## Initial setup

On first launch, the application prompts for:

1. Creating an admin user
2. Setting a password (stored with Argon2id)

Data is automatically encrypted with AES-256-GCM and stored under the user XDG directories (config in `~/.config/dafi/`, data in `~/.local/share/dafi/`). Installations upgraded from previous versions keep their legacy `~/.dafi/data` files, which are migrated automatically on first launch.

## Data

Directories (XDG Base Directory / Flatpak-friendly layout):

```
~/.config/dafi/                 # Configuration (XDG_CONFIG_HOME)
├── credentials.json            # User credentials (Argon2 hash)
└── encryption.key              # Encryption key (file backup)
~/.local/share/dafi/            # Data (XDG_DATA_HOME)
    ├── clients.json            # Encrypted clients
    ├── contract-types.json     # Encrypted contract types
    └── payment-methods.json    # Encrypted payment methods
```

Legacy `~/.dafi/` (pre-XDG) content is preserved: config is still used as-is and data is migrated once to the XDG location, leaving the original untouched as a backup.

**IMPORTANT**: These files contain sensitive data and should not be shared.

## Security

| Component | Technology | Detail |
|-----------|-----------|--------|
| Passwords | Argon2id | Unique salt per hash, no plaintext |
| Data | AES-256-GCM | Authenticated encryption (integrity + confidentiality) |
| Encryption key | OS Keyring / File | Preference: OS keyring; fallback: file on disk |
| Key export/import | .txt/.key file | Enables data migration between machines |

## Bulk Excel import

### Expected format

All columns are **required** except **E-mail** and **Anticipo (Advance)**:

| Column | Required | Format |
|--------|:--------:|--------|
| Folio | Yes | Text |
| Nombre Completo (Full Name) | Yes | Text |
| INE | Yes | Text |
| Tipo de Contrato (Contract Type) | Yes | Text (auto-created if missing) |
| Domicilio (Address) | Yes | Text |
| Colonia (Neighborhood) | Yes | Text |
| Telefono (Phone) | Yes | Text |
| E-mail | No | text@domain.com |
| Modo de Pago (Payment Method) | Yes | Text (auto-created if missing) |
| Primer Beneficiario (First Beneficiary) | Yes | Text |
| Segundo Beneficiario (Second Beneficiary) | Yes | Text |
| Descripcion de la Venta (Sale Description) | Yes | Text |
| Anualidad (Annuity) | Yes | Text |
| Manzana (Block) | Yes | Text |
| Lote (Lot) | Yes | Text |
| Gasto de Gestion (Management Fee) | Yes | Number |
| Anticipo (Advance) | No | Number |
| Saldo Total (Total Balance) | Yes | Number |
| Fecha Primer Pago (First Payment Date) | Yes | dd/MM/yyyy |
| Fecha Contrato (Contract Date) | Yes | dd/MM/yyyy |
| Dia de Pago (Payment Day) | Yes | 1-31 |
| Mensualidades (Total Payments) | Yes | Number >= 1 |
| Mensualidad (Monthly Payment) | Yes | Number |

### Rules

- Duplicate folios (case/accent insensitive) are skipped
- Unrecognized columns with content reject the row
- Dates accept `dd/MM/yyyy` and `yyyy-MM-dd`
- A report is generated at `~/Documents/DAFI/carga-clientes-*.log`

## Windows packaging

```bash
# Run the build script (requires JDK 17+ with jpackage)
construir-instalador.bat
```

Generates a Windows installer at `packaging/target/jpackage/`.

## Flatpak / Flathub packaging

Canonical application ID: `io.github.davichus1303.DafiDesktop`

This ID is required by Flathub (reverse-DNS recommended for projects without a
dedicated domain) and is the only identifier used by the Flatpak manifest,
desktop file and icon. It is **not** the Java package name.

| Artifact | Location |
|----------|----------|
| Flatpak manifest | `packaging/flatpak/io.github.davichus1303.DafiDesktop.yml` |
| Desktop file | `packaging/io.github.davichus1303.DafiDesktop.desktop` |
| Icon (256 px PNG) | `src/main/resources/icons/icon-256.png` → installed as `io.github.davichus1303.DafiDesktop.png` |
| Executable | `bin/DAFI-Desktop` (jpackage launcher, relative to `/app` in the sandbox) |

Design notes:

- The `.desktop` lands at `/app/share/applications/io.github.davichus1303.DafiDesktop.desktop`
  inside the sandbox; Flathub validates it against the appstream spec.
- `Exec=/app/bin/DAFI-Desktop` is absolute on purpose: Flathub rejects desktop
  entries whose `Exec` is not an absolute path inside the sandbox.
- `StartupWMClass` is intentionally omitted. The freedesktop desktop-entry
  spec does not require it, and it is only meaningful when it matches the
  window class the runtime actually reports; the value will be determined with
  `xprop`/`xwininfo` during Phase 6 (local build validation) before it is
  declared.
- The manifest does not export `--share=network`; the app performs no network
  I/O and reads/writes only under XDG dirs, the user's documents folder and the
  legacy `~/.dafi` storage.
- `finish-args` follow least-privilege: the session bus is not granted as a
  whole; the app talks only to `org.freedesktop.secrets` (keyring adapter) and
  `org.freedesktop.portal.*` (desktop portal used by the JavaFX `FileChooser`
  for key export/import and the bulk import). `--filesystem=home/.dafi` is
  granted read-write because logback writes `~/.dafi/logs/` and the config
  adapter may keep the legacy `~/.dafi/config`; it is scoped to that folder,
  not the whole home.

## Key tools

From the tools button (gear icon) in the clients view:

- **Export key**: Saves the encryption key to a .txt file
- **Import key**: Restores an encryption key from a file

Useful for migrating data between machines or creating backups.

## Development

### Conventions

- Immutable domain entities (no public setters)
- Builder pattern for construction with validation
- Value objects for validated concepts (Email)
- Ports (interfaces) in the application layer
- Concrete adapters in adapters/inbound and adapters/outbound
- SLF4J logging in the serialization layer
- Javadoc on public classes and methods

### Useful commands

```bash
mvn clean compile          # Compile
mvn test                   # Run 48 tests
mvn javafx:run             # Run application
mvn javafx:run -Djavafx.args="--width=1280 --height=800"
```

## License

Proprietary - Internal use
