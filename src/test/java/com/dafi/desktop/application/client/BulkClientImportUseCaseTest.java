package com.dafi.desktop.application.client;

import com.dafi.desktop.application.contracttype.ContractTypeCatalogRepositoryPort;
import com.dafi.desktop.application.paymentmethod.PaymentMethodCatalogRepositoryPort;
import com.dafi.desktop.domain.client.Client;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulkClientImportUseCaseTest {

    @TempDir
    Path tempDir;

    private ClientRepositoryPort clientRepositoryPort;
    private ContractTypeCatalogRepositoryPort contractTypeCatalogRepositoryPort;
    private PaymentMethodCatalogRepositoryPort paymentMethodCatalogRepositoryPort;
    private BulkClientImportUseCase useCase;

    private static final String[] HEADERS = {
            "Folio", "Nombre Completo", "INE", "Tipo de Contrato", "Domicilio", "Colonia",
            "Teléfono", "E-mail", "Modo de Pago", "Primer Beneficiario", "Segundo Beneficiario",
            "Descripción de la Venta", "Anualidad", "Manzana", "Lote", "Gasto de Gestión",
            "Anticipo", "Saldo Total", "Fecha Primer Pago", "Fecha de Contratación",
            "Día de Pago", "Núm. Mensualidades", "Costo de Mensualidad"
    };

    @BeforeEach
    void setUp() {
        clientRepositoryPort = mock(ClientRepositoryPort.class);
        contractTypeCatalogRepositoryPort = mock(ContractTypeCatalogRepositoryPort.class);
        paymentMethodCatalogRepositoryPort = mock(PaymentMethodCatalogRepositoryPort.class);
        when(clientRepositoryPort.findAll()).thenReturn(List.of());
        when(contractTypeCatalogRepositoryPort.findAll()).thenReturn(List.of());
        when(paymentMethodCatalogRepositoryPort.findAll()).thenReturn(List.of());

        useCase = new BulkClientImportUseCase(clientRepositoryPort,
                contractTypeCatalogRepositoryPort, paymentMethodCatalogRepositoryPort);
    }

    @Test
    void importsValidRowAndCreatesMissingCatalogs() throws Exception {
        Path file = writeWorkbook(validRow("A-001", "Juan Pérez"));

        BulkImportResult result = useCase.importFromFile(file);

        assertEquals(1, result.totalRows());
        assertEquals(1, result.importedCount());
        assertEquals(0, result.rejectedCount());
        assertNull(result.reportPath());

        ArgumentCaptor<List<Client>> savedClients = ArgumentCaptor.forClass(List.class);
        verify(clientRepositoryPort).saveAll(savedClients.capture());
        Client imported = savedClients.getValue().getFirst();
        assertEquals("A-001", imported.getContractFolio());
        assertEquals("Juan Pérez", imported.getFullName());
        assertEquals("Plan Integral", imported.getContractType());
        assertEquals("Mensual", imported.getPaymentMethod());
        assertEquals(new BigDecimal("900"), imported.getMonthlyPayment());
        assertEquals(LocalDate.of(2024, 1, 5), imported.getFirstPaymentDate());
        assertEquals(LocalDate.of(2025, 1, 10), imported.getContractEndDate());

        verify(contractTypeCatalogRepositoryPort).save(any());
        verify(paymentMethodCatalogRepositoryPort).save(any());
    }

    @Test
    void collectsEveryErrorOfARowAndContinuesWithTheRest() throws Exception {
        String[] invalidRow = validRow("A-001", "Juan Pérez");
        invalidRow[18] = "fecha-mala";
        invalidRow[22] = "abc";
        Path file = writeWorkbook(invalidRow, validRow("A-002", "María López"));

        BulkImportResult result = useCase.importFromFile(file);

        assertEquals(2, result.totalRows());
        assertEquals(1, result.importedCount());
        assertEquals(1, result.rejectedCount());

        List<String> errors = result.rejections().getFirst().errors();
        assertTrue(errors.stream().anyMatch(error -> error.contains("Fecha Primer Pago")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("Costo de Mensualidad")));

        ArgumentCaptor<List<Client>> savedClients = ArgumentCaptor.forClass(List.class);
        verify(clientRepositoryPort).saveAll(savedClients.capture());
        assertEquals("María López", savedClients.getValue().getFirst().getFullName());
    }

    @Test
    void rejectsRowsMissingAnyRequiredFieldButContinues() throws Exception {
        String[] incompleteRow = validRow("A-001", "Juan Pérez");
        incompleteRow[2] = "";
        incompleteRow[10] = "";
        Path file = writeWorkbook(incompleteRow, validRow("A-002", "María López"));

        BulkImportResult result = useCase.importFromFile(file);

        assertEquals(1, result.rejectedCount());
        List<String> errors = result.rejections().getFirst().errors();
        assertTrue(errors.stream().anyMatch(error -> error.contains("'INE'")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("'Segundo Beneficiario'")));
        assertEquals(1, result.importedCount());
    }

    @Test
    void allowsBlankOptionalEmailAndAdvanceFields() throws Exception {
        String[] minimalRow = validRow("A-001", "Juan Pérez");
        minimalRow[7] = "";
        minimalRow[16] = "";

        BulkImportResult result = useCase.importFromFile(writeWorkbook(minimalRow));

        assertEquals(1, result.importedCount());
        assertEquals(0, result.rejectedCount());
        ArgumentCaptor<List<Client>> savedClients = ArgumentCaptor.forClass(List.class);
        verify(clientRepositoryPort).saveAll(savedClients.capture());
        assertEquals(BigDecimal.ZERO, savedClients.getValue().getFirst().getAdvance());
    }

    @Test
    void rejectsFoliosDuplicatedInSystemAndInsideFile() throws Exception {
        when(clientRepositoryPort.findAll()).thenReturn(
                List.of(clientWithFolio("EXISTENTE")));
        Path file = writeWorkbook(
                withFolio(validRow("A-001", "Uno Uno"), "existente"),
                validRow("A-002", "Dos Dos"),
                withFolio(validRow("A-003", "Tres Tres"), "a-002"));

        BulkImportResult result = useCase.importFromFile(file);

        assertEquals(1, result.importedCount());
        assertEquals(2, result.rejectedCount());
        assertTrue(result.rejections().get(0).errors().getFirst()
                .contains("ya está registrado en el sistema"));
        assertTrue(result.rejections().get(1).errors().getFirst()
                .contains("duplicado dentro del archivo"));
    }

    @Test
    void rejectsRowsWithUnknownColumnContentAndIgnoresEmptyOnes() throws Exception {
        String[] headersWithExtra = Arrays.copyOf(HEADERS, HEADERS.length + 1);
        headersWithExtra[HEADERS.length] = "Gasolina";

        String[] withContent = extend(validRow("A-001", "Con Columna"), "Sí");
        String[] withEmpty = extend(validRow("A-002", "Sin Contenido"), "");

        Path file = writeWorkbookWithHeaders(headersWithExtra, withContent, withEmpty);

        BulkImportResult result = useCase.importFromFile(file);

        assertEquals(1, result.importedCount());
        assertEquals(1, result.rejectedCount());
        assertTrue(result.rejections().getFirst().errors().getFirst().contains("Gasolina"));
        assertTrue(result.unknownColumns().contains("Gasolina"));
    }

    @Test
    void preservesExistingClientsWhenPersisting() throws Exception {
        Client existing = clientWithFolio("VIEJO-1");
        when(clientRepositoryPort.findAll()).thenReturn(List.of(existing));

        useCase.importFromFile(writeWorkbook(validRow("A-001", "Nuevo Cliente")));

        ArgumentCaptor<List<Client>> savedClients = ArgumentCaptor.forClass(List.class);
        verify(clientRepositoryPort).saveAll(savedClients.capture());
        assertEquals(2, savedClients.getValue().size());
        assertEquals("VIEJO-1", savedClients.getValue().get(0).getContractFolio());
        assertEquals("A-001", savedClients.getValue().get(1).getContractFolio());
    }

    @Test
    void skipsFullyBlankRowsWithoutPersistingAnything() throws Exception {
        String[] blankRow = new String[HEADERS.length];

        BulkImportResult result = useCase.importFromFile(writeWorkbook(blankRow));

        assertEquals(0, result.totalRows());
        assertEquals(0, result.rejectedCount());
        verify(clientRepositoryPort, never()).saveAll(any());
        verify(contractTypeCatalogRepositoryPort, never()).save(any());
        verify(paymentMethodCatalogRepositoryPort, never()).save(any());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Returns a fully valid data row for the given folio and name; every
     * required field is filled and the optional ones (e-mail, anticipo)
     * are left blank.
     */
    private static String[] validRow(String folio, String fullName) {
        return new String[]{
                folio, fullName, "INE-" + folio, "Plan Integral",
                "Av. Reforma 120", "Centro", "5551234567", "", "Mensual",
                "Beneficiario Uno", "Beneficiario Dos", "Contrato de servicios funerarios",
                "No", "A", "12", "500", "", "10800",
                "05/01/2024", "10/01/2024", "15", "12", "900"
        };
    }

    private static String[] withFolio(String[] row, String folio) {
        row[0] = folio;
        return row;
    }

    private static String[] extend(String[] row, String extraValue) {
        String[] extended = Arrays.copyOf(row, row.length + 1);
        extended[row.length] = extraValue;
        return extended;
    }

    private Path writeWorkbook(String[]... dataRows) throws Exception {
        return writeWorkbookWithHeaders(HEADERS.clone(), dataRows);
    }

    private Path writeWorkbookWithHeaders(String[] headers, String[]... dataRows) throws Exception {
        Path file = tempDir.resolve("import-" + System.nanoTime() + ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream output = new FileOutputStream(file.toFile())) {
            var sheet = workbook.createSheet();
            Row headerRow = sheet.createRow(0);
            for (int column = 0; column < headers.length; column++) {
                headerRow.createCell(column).setCellValue(headers[column]);
            }
            for (int rowIndex = 0; rowIndex < dataRows.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                for (int column = 0; column < dataRows[rowIndex].length; column++) {
                    row.createCell(column).setCellValue(dataRows[rowIndex][column]);
                }
            }
            workbook.write(output);
        }
        return file;
    }

    private Client clientWithFolio(String folio) {
        return Client.builder()
                .id("id-" + folio)
                .contractFolio(folio)
                .fullName(folio)
                .contractDate(LocalDate.of(2024, 1, 10))
                .firstPaymentDate(LocalDate.of(2024, 1, 5))
                .paymentDay(15)
                .totalPayments(12)
                .build();
    }
}
