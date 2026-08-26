package com.dafi.desktop.application.client;

import com.dafi.desktop.application.contracttype.ContractTypeCatalogRepositoryPort;
import com.dafi.desktop.application.paymentmethod.PaymentMethodCatalogRepositoryPort;
import com.dafi.desktop.domain.DomainException;
import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.domain.contracttype.ContractTypeCatalog;
import com.dafi.desktop.domain.paymentmethod.PaymentMethodCatalog;
import com.dafi.desktop.domain.shared.Email;
import com.dafi.desktop.adapters.inbound.ExcelRow;
import com.dafi.desktop.adapters.inbound.ExcelRowReader;
import com.dafi.desktop.adapters.outbound.BulkImportReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Application use case that bulk imports clients from an Excel workbook into
 * the encrypted JSON repository. Every data row of the first sheet is
 * processed independently inside a single loop: a row with validation errors
 * is recorded and skipped without interrupting the remaining rows. Valid rows
 * create any missing catalog entry (contract types and payment methods) and
 * become clients persisted with the existing encryption and storage
 * mechanisms in one batch at the end of the run.
 * <p>
 * Accepted column headers are Spanish aliases of the client fields (for
 * example {@code "Folio"}, {@code "Nombre Completo"}, {@code "Fecha Primer
 * Pago"}); dates must be {@code dd/MM/yyyy} and amounts plain numbers.
 */
public class BulkClientImportUseCase {

    private static final Logger log = LoggerFactory.getLogger(BulkClientImportUseCase.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClientRepositoryPort clientRepositoryPort;
    private final ContractTypeCatalogRepositoryPort contractTypeCatalogRepositoryPort;
    private final PaymentMethodCatalogRepositoryPort paymentMethodCatalogRepositoryPort;

    /**
     * Creates the use case.
     *
     * @param clientRepositoryPort                port persisting clients
     * @param contractTypeCatalogRepositoryPort   port persisting contract types
     * @param paymentMethodCatalogRepositoryPort  port persisting payment methods
     */
    public BulkClientImportUseCase(ClientRepositoryPort clientRepositoryPort,
                                   ContractTypeCatalogRepositoryPort contractTypeCatalogRepositoryPort,
                                   PaymentMethodCatalogRepositoryPort paymentMethodCatalogRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.contractTypeCatalogRepositoryPort = contractTypeCatalogRepositoryPort;
        this.paymentMethodCatalogRepositoryPort = paymentMethodCatalogRepositoryPort;
    }

    /**
     * Runs the full import: reads the workbook, validates each row inside a
     * loop that never stops on errors, creates missing catalog entries,
     * appends the valid clients to the existing ones and persists everything
     * through the current encrypted repository.
     *
     * @param excelFile path to the {@code .xlsx} or {@code .xls} file
     * @return summary of the run, including the report path when some rows
     *         were rejected
     * @throws IOException if the workbook cannot be read
     */
    public BulkImportResult importFromFile(Path excelFile) throws IOException {
        List<ExcelRow> rows = ExcelRowReader.readRows(excelFile);
        log.info("Carga masiva iniciada: {} filas leídas de {}", rows.size(), excelFile.getFileName());

        Set<String> foliosInSystem = loadExistingFolios();
        Set<String> contractTypesInSystem = loadNormalizedCatalogNames(
                contractTypeCatalogRepositoryPort.findAll());
        Set<String> paymentMethodsInSystem = loadNormalizedCatalogNames(
                paymentMethodCatalogRepositoryPort.findAll());

        List<Client> importedClients = new ArrayList<>();
        List<RowRejection> rejections = new ArrayList<>();
        Set<String> foliosSeenInFile = new HashSet<>();

        for (ExcelRow row : rows) {
            processRow(row, foliosInSystem, foliosSeenInFile, contractTypesInSystem,
                    paymentMethodsInSystem, importedClients, rejections);
        }

        persistImportedClients(importedClients);

        String reportPath = writeReportWhenNeeded(excelFile, importedClients.size(), rejections);
        log.info("Carga masiva terminada: {} agregados, {} rechazados de {} filas",
                importedClients.size(), rejections.size(), rows.size());
        return new BulkImportResult(rows.size(), importedClients.size(),
                rejections, collectUnknownColumns(rows), reportPath);
    }

    private void processRow(ExcelRow row,
                            Set<String> foliosInSystem,
                            Set<String> foliosSeenInFile,
                            Set<String> contractTypesInSystem,
                            Set<String> paymentMethodsInSystem,
                            List<Client> importedClients,
                            List<RowRejection> rejections) {
        List<String> errors = new ArrayList<>();
        Client client = buildValidatedClient(row, errors, foliosInSystem, foliosSeenInFile);

        if (!errors.isEmpty()) {
            log.warn("Fila {} rechazada: {}", row.rowNumber(), errors);
            rejections.add(new RowRejection(row.rowNumber(), List.copyOf(errors)));
            return;
        }

        ensureContractTypeExists(client.getContractType(), contractTypesInSystem);
        ensurePaymentMethodExists(client.getPaymentMethod(), paymentMethodsInSystem);

        importedClients.add(client);
        foliosSeenInFile.add(normalizeText(client.getContractFolio()));
    }

    /**
     * Maps the raw Excel values onto a {@link Client} while accumulating every
     * problem found (unknown columns with content, missing required fields,
     * malformed numbers or dates, duplicate folios). Returns {@code null} when
     * any error was collected.
     */
    private Client buildValidatedClient(ExcelRow row, List<String> errors,
                                        Set<String> foliosInSystem, Set<String> foliosSeenInFile) {
        Map<String, String> fields = mapHeadersToFields(row, errors);
        if (!errors.isEmpty()) {
            return null;
        }

        // Required text fields (everything except e-mail)
        String contractFolio = requireText(fields, "contractFolio", "Folio de Contrato", errors);
        requireText(fields, "fullName", "Nombre Completo", errors);
        requireText(fields, "ine", "INE", errors);
        requireText(fields, "contractType", "Tipo de Contrato", errors);
        requireText(fields, "address", "Domicilio", errors);
        requireText(fields, "neighborhood", "Colonia", errors);
        requireText(fields, "phone", "Teléfono", errors);
        requireText(fields, "paymentMethod", "Modo de Pago", errors);
        requireText(fields, "firstBeneficiary", "Primer Beneficiario", errors);
        requireText(fields, "secondBeneficiary", "Segundo Beneficiario", errors);
        requireText(fields, "saleDescription", "Descripción de la Venta", errors);
        requireText(fields, "annuity", "Anualidad", errors);
        requireText(fields, "block", "Manzana", errors);
        requireText(fields, "lot", "Lote", errors);

        // Required dates and numbers
        LocalDate contractDate = requireDate(fields, "contractDate", "Fecha de Contratación", errors);
        LocalDate firstPaymentDate = requireDate(fields, "firstPaymentDate", "Fecha Primer Pago", errors);
        Integer totalPayments = requireIntInRange(fields, "totalPayments",
                "Núm. Mensualidades", 1, Integer.MAX_VALUE, errors);
        requireIntInRange(fields, "paymentDay", "Día de Pago", 1, 31, errors);

        BigDecimal managementFee = parseAmount(fields, "managementFee", "Gasto de Gestión", true, errors);
        BigDecimal advance = parseAmount(fields, "advance", "Anticipo", false, errors);
        BigDecimal totalBalance = parseAmount(fields, "totalBalance", "Saldo Total", true, errors);
        BigDecimal monthlyPayment = parseAmount(fields, "monthlyPayment", "Costo de Mensualidad", true, errors);

        // Optional field
        validateEmail(fields.getOrDefault("email", ""), errors);

        validateUniqueFolio(contractFolio, foliosInSystem, foliosSeenInFile, errors);

        LocalDate contractEndDate = parseOptionalDate(fields, "contractEndDate",
                "Fecha Fin Contrato", errors);

        if (!errors.isEmpty()) {
            return null;
        }

        try {
            return assembleClient(fields, contractDate, firstPaymentDate, totalPayments,
                    managementFee, advance, totalBalance, monthlyPayment, contractEndDate);
        } catch (DomainException e) {
            errors.add(e.getMessage());
            return null;
        }
    }

    private Client assembleClient(Map<String, String> fields, LocalDate contractDate,
                                  LocalDate firstPaymentDate, Integer totalPayments,
                                  BigDecimal managementFee, BigDecimal advance,
                                  BigDecimal totalBalance, BigDecimal monthlyPayment,
                                  LocalDate contractEndDate) {
        LocalDate effectiveEndDate = contractEndDate != null
                ? contractEndDate
                : Client.calculateContractEndDate(contractDate, totalPayments);
        return Client.builder()
                .id(UUID.randomUUID().toString())
                .contractFolio(textOrEmpty(fields, "contractFolio"))
                .fullName(textOrEmpty(fields, "fullName"))
                .ine(textOrEmpty(fields, "ine"))
                .contractType(textOrEmpty(fields, "contractType"))
                .address(textOrEmpty(fields, "address"))
                .neighborhood(textOrEmpty(fields, "neighborhood"))
                .phone(textOrEmpty(fields, "phone"))
                .email(textOrEmpty(fields, "email"))
                .paymentMethod(textOrEmpty(fields, "paymentMethod"))
                .firstBeneficiary(textOrEmpty(fields, "firstBeneficiary"))
                .secondBeneficiary(textOrEmpty(fields, "secondBeneficiary"))
                .saleDescription(textOrEmpty(fields, "saleDescription"))
                .annuity(textOrEmpty(fields, "annuity"))
                .block(textOrEmpty(fields, "block"))
                .lot(textOrEmpty(fields, "lot"))
                .managementFee(managementFee)
                .advance(advance)
                .totalBalance(totalBalance)
                .firstPaymentDate(firstPaymentDate)
                .contractDate(contractDate)
                .paymentDay(Integer.parseInt(fields.getOrDefault("paymentDay", "0")))
                .totalPayments(totalPayments)
                .monthlyPayment(monthlyPayment)
                .contractEndDate(effectiveEndDate)
                .build();
    }

    // ------------------------------------------------------------------
    // Header mapping
    // ------------------------------------------------------------------

    private Map<String, String> mapHeadersToFields(ExcelRow row, List<String> errors) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Map.Entry<String, String> column : row.values().entrySet()) {
            String fieldKey = HeaderAliases.resolve(column.getKey());
            if (fieldKey == null) {
                rejectUnknownColumn(errors, column.getKey(), column.getValue());
            } else {
                fields.put(fieldKey, column.getValue() == null ? "" : column.getValue().trim());
            }
        }
        return fields;
    }

    private void rejectUnknownColumn(List<String> errors, String header, String value) {
        if (value != null && !value.isBlank()) {
            errors.add("La columna '" + header + "' no es reconocida por el sistema");
        }
    }

    private List<String> collectUnknownColumns(List<ExcelRow> rows) {
        Set<String> unknown = new HashSet<>();
        for (ExcelRow row : rows) {
            for (Map.Entry<String, String> column : row.values().entrySet()) {
                if (HeaderAliases.resolve(column.getKey()) == null && hasContent(row.values())) {
                    unknown.add(column.getKey());
                }
            }
        }
        return List.copyOf(unknown);
    }

    private boolean hasContent(Map<String, String> values) {
        return values.values().stream().anyMatch(value -> value != null && !value.isBlank());
    }

    // ------------------------------------------------------------------
    // Field parsing and validation
    // ------------------------------------------------------------------

    private String requireText(Map<String, String> fields, String fieldKey, String label,
                               List<String> errors) {
        String value = fields.getOrDefault(fieldKey, "");
        if (value.isBlank()) {
            errors.add("Falta el campo obligatorio '" + label + "'");
            return "";
        }
        return value;
    }

    private LocalDate requireDate(Map<String, String> fields, String fieldKey, String label,
                                  List<String> errors) {
        LocalDate parsed = parseDate(fields.getOrDefault(fieldKey, ""), label, errors);
        if (parsed == null && !hasDateFormatError(errors, label)) {
            errors.add("Falta el campo obligatorio '" + label + "'");
        }
        return parsed;
    }

    private LocalDate parseOptionalDate(Map<String, String> fields, String fieldKey,
                                        String label, List<String> errors) {
        String raw = fields.getOrDefault(fieldKey, "");
        if (raw.isBlank()) {
            return null;
        }
        return parseDate(raw, label, errors);
    }

    private LocalDate parseDate(String raw, String label, List<String> errors) {
        if (raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw, DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
            // try the ISO fallback before reporting an error
        }
        try {
            return LocalDate.parse(raw, ISO_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            errors.add("El campo '" + label + "' no tiene una fecha válida "
                    + "(formato esperado dd/mm/aaaa): \"" + raw + "\"");
            return null;
        }
    }

    private boolean hasDateFormatError(List<String> errors, String label) {
        return errors.stream().anyMatch(error -> error.startsWith("El campo '" + label + "'"));
    }

    private Integer requireIntInRange(Map<String, String> fields, String fieldKey, String label,
                                      int minimum, int maximum, List<String> errors) {
        String raw = fields.getOrDefault(fieldKey, "");
        if (raw.isBlank()) {
            errors.add("Falta el campo obligatorio '" + label + "'");
            return null;
        }
        try {
            String cleaned = raw.trim();
            if (cleaned.matches("\\d+\\.0+")) {
                cleaned = cleaned.substring(0, cleaned.indexOf('.'));
            }
            int value = Integer.parseInt(cleaned);
            if (value < minimum || value > maximum) {
                errors.add("El campo '" + label + "' debe estar entre " + minimum
                        + " y " + maximum + ": " + raw);
            }
            return value;
        } catch (NumberFormatException e) {
            errors.add("El campo '" + label + "' no es un número válido: \"" + raw + "\"");
            return null;
        }
    }

    private BigDecimal parseAmount(Map<String, String> fields, String fieldKey, String label,
                                   boolean required, List<String> errors) {
        String raw = fields.getOrDefault(fieldKey, "");
        if (raw.isBlank()) {
            if (required) {
                errors.add("Falta el campo obligatorio '" + label + "'");
            }
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal value = new BigDecimal(raw.replace("$", "").replace(",", "").trim());
            if (value.signum() < 0) {
                errors.add("El campo '" + label + "' no puede ser negativo: " + raw);
            }
            return value;
        } catch (NumberFormatException e) {
            errors.add("El campo '" + label + "' no tiene un formato numérico válido: \"" + raw + "\"");
            return BigDecimal.ZERO;
        }
    }

    private void validateEmail(String email, List<String> errors) {
        if (!email.isBlank()) {
            try {
                new Email(email);
            } catch (IllegalArgumentException e) {
                errors.add("El campo 'E-mail' no tiene un formato válido: \"" + email + "\"");
            }
        }
    }

    private void validateUniqueFolio(String folio, Set<String> foliosInSystem,
                                     Set<String> foliosSeenInFile, List<String> errors) {
        if (folio == null || folio.isBlank()) {
            return;
        }
        String normalized = normalizeText(folio);
        if (foliosInSystem.contains(normalized)) {
            errors.add("El folio '" + folio + "' ya está registrado en el sistema");
        } else if (foliosSeenInFile.contains(normalized)) {
            errors.add("El folio '" + folio + "' está duplicado dentro del archivo");
        }
    }

    // ------------------------------------------------------------------
    // Catalog handling
    // ------------------------------------------------------------------

    private void ensureContractTypeExists(String value, Set<String> existingNames) {
        if (value != null && !value.isBlank()
                && !containsIgnoringCase(existingNames, value)) {
            contractTypeCatalogRepositoryPort.save(new ContractTypeCatalog(
                    UUID.randomUUID().toString(), value, ""));
            existingNames.add(normalizeText(value));
            log.info("Tipo de contrato creado durante la carga masiva: {}", value);
        }
    }

    private void ensurePaymentMethodExists(String value, Set<String> existingNames) {
        if (value != null && !value.isBlank()
                && !containsIgnoringCase(existingNames, value)) {
            paymentMethodCatalogRepositoryPort.save(new PaymentMethodCatalog(
                    UUID.randomUUID().toString(), value, ""));
            existingNames.add(normalizeText(value));
            log.info("Modo de pago creado durante la carga masiva: {}", value);
        }
    }

    private boolean containsIgnoringCase(Set<String> normalizedNames, String candidate) {
        return normalizedNames.contains(normalizeText(candidate));
    }

    private Set<String> loadExistingFolios() {
        Set<String> folios = new HashSet<>();
        for (Client client : clientRepositoryPort.findAll()) {
            folios.add(normalizeText(client.getContractFolio()));
        }
        return folios;
    }

    private <E extends com.dafi.desktop.domain.shared.CatalogEntry> Set<String>
            loadNormalizedCatalogNames(List<E> entries) {
        Set<String> names = new HashSet<>();
        for (E entry : entries) {
            names.add(normalizeText(entry.getName()));
        }
        return names;
    }

    // ------------------------------------------------------------------
    // Persistence and reporting
    // ------------------------------------------------------------------

    /**
     * Appends the imported clients to the currently stored ones and writes
     * the whole repository once, reusing the existing encryption pipeline.
     */
    private void persistImportedClients(List<Client> importedClients) {
        if (importedClients.isEmpty()) {
            return;
        }
        List<Client> allClients = new ArrayList<>(clientRepositoryPort.findAll());
        allClients.addAll(importedClients);
        clientRepositoryPort.saveAll(allClients);
    }

    private String writeReportWhenNeeded(Path excelFile, int importedCount,
                                         List<RowRejection> rejections) {
        if (rejections.isEmpty()) {
            return null;
        }
        Path reportPath = BulkImportReportWriter.writeReport(
                excelFile.getFileName().toString(),
                new BulkImportResult(0, importedCount, rejections, List.of(), null));
        return reportPath == null ? null : reportPath.toString();
    }

    /**
     * Lowercases, strips accents and collapses whitespace so headers, folios
     * and catalog names can be compared reliably regardless of typing style.
     */
    static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String lowerAndStripped = Normalizer.normalize(text.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return lowerAndStripped.replaceAll("\\s+", " ").trim();
    }

    private String textOrEmpty(Map<String, String> fields, String key) {
        return fields.getOrDefault(key, "");
    }

    /**
     * Maps every accepted Excel header alias to the canonical client field
     * key used by the mapper. Comparison is done on normalized text, so
     * accents, casing and extra spaces do not matter.
     */
    private static final class HeaderAliases {

        private static final Map<String, String> ALIASES = buildAliases();

        private HeaderAliases() {
        }

        static String resolve(String header) {
            return ALIASES.get(normalizeHeader(header));
        }

        private static Map<String, String> buildAliases() {
            Map<String, String> aliases = new LinkedHashMap<>();
            put(aliases, "contractFolio", "folio", "folio de contrato", "folio contrato");
            put(aliases, "fullName", "nombre completo", "nombre");
            put(aliases, "ine", "ine", "clave ine");
            put(aliases, "contractType", "tipo de contrato", "tipo contrato");
            put(aliases, "address", "domicilio", "direccion");
            put(aliases, "neighborhood", "colonia");
            put(aliases, "phone", "telefono");
            put(aliases, "email", "email", "e-mail", "correo electronico", "correo");
            put(aliases, "paymentMethod", "modo de pago", "forma de pago", "tipo de pago");
            put(aliases, "firstBeneficiary", "primer beneficiario", "beneficiario 1");
            put(aliases, "secondBeneficiary", "segundo beneficiario", "beneficiario 2");
            put(aliases, "saleDescription", "descripcion de la venta", "descripcion venta", "descripcion");
            put(aliases, "annuity", "anualidad");
            put(aliases, "block", "manzana");
            put(aliases, "lot", "lote");
            put(aliases, "managementFee", "gasto de gestion", "gasto gestion");
            put(aliases, "advance", "anticipo");
            put(aliases, "totalBalance", "saldo total", "saldo");
            put(aliases, "firstPaymentDate", "fecha primer pago", "fecha del primer pago");
            put(aliases, "contractDate", "fecha de contratacion", "fecha contratacion", "fecha contrato");
            put(aliases, "paymentDay", "dia de pago");
            put(aliases, "totalPayments", "num mensualidades", "numero de mensualidades",
                    "no mensualidades", "total mensualidades", "mensualidades");
            put(aliases, "monthlyPayment", "costo de mensualidad", "costo mensualidad",
                    "mensualidad", "mensual");
            put(aliases, "contractEndDate", "fecha fin contrato", "fecha fin",
                    "fecha de termino", "vigencia");
            return aliases;
        }

        /**
         * Header normalization additionally drops punctuation such as the
         * period in {@code "Núm."} so abbreviated spreadsheet titles match
         * their alias regardless of typing style.
         */
        private static String normalizeHeader(String header) {
            return normalizeText(header).replace(".", "").replace(",", "");
        }

        private static void put(Map<String, String> aliases, String fieldKey, String... aliasesList) {
            for (String alias : aliasesList) {
                aliases.put(normalizeHeader(alias), fieldKey);
            }
        }
    }
}
