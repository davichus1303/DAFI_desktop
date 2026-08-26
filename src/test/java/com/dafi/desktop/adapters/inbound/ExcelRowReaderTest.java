package com.dafi.desktop.adapters.inbound;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelRowReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readsHeaderAsKeysAndDataRowsAsValues() throws Exception {
        Path file = createWorkbook(new String[]{"Folio", "Nombre Completo"},
                new String[][]{{"A-001", "Juan Pérez"}, {"A-002", "María López"}});

        List<ExcelRow> rows = ExcelRowReader.readRows(file);

        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).rowNumber());
        assertEquals("Juan Pérez", rows.get(0).value("Nombre Completo"));
        assertEquals("A-001", rows.get(0).value("Folio"));
        assertEquals(3, rows.get(1).rowNumber());
        assertEquals("María López", rows.get(1).value("Nombre Completo"));
    }

    @Test
    void rendersNumericCellsAsPlainText() throws Exception {
        Path file = createWorkbook(new String[]{"Nombre", "Anticipo"},
                new String[][]{{"Juan", "1500.5"}});

        List<ExcelRow> rows = ExcelRowReader.readRows(file);

        assertEquals("1500.5", rows.get(0).value("Anticipo"));
    }

    @Test
    void skipsBlankHeaderRowsAndFullyEmptyDataRows() throws Exception {
        Path file = tempDir.resolve("blank.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream output = new FileOutputStream(file.toFile())) {
            workbook.createSheet().createRow(0);
            Row header = workbook.getSheetAt(0).createRow(1);
            header.createCell(0).setCellValue("Folio");
            workbook.getSheetAt(0).createRow(2);
            Row data = workbook.getSheetAt(0).createRow(3);
            data.createCell(0).setCellValue("A-010");
            workbook.write(output);
        }

        List<ExcelRow> rows = ExcelRowReader.readRows(file);

        assertEquals(1, rows.size());
        assertEquals(4, rows.get(0).rowNumber());
    }

    @Test
    void returnsEmptyListForCompletelyEmptySheet(@TempDir Path empty) throws Exception {
        Path file = empty.resolve("empty.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream output = new FileOutputStream(file.toFile())) {
            workbook.createSheet();
            workbook.write(output);
        }

        List<ExcelRow> rows = ExcelRowReader.readRows(file);

        assertTrue(rows.isEmpty());
    }

    private Path createWorkbook(String[] headers, String[][] dataRows) throws Exception {
        Path file = tempDir.resolve("workbook-" + System.nanoTime() + ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream output = new FileOutputStream(file.toFile())) {
            Row headerRow = workbook.createSheet().createRow(0);
            for (int column = 0; column < headers.length; column++) {
                headerRow.createCell(column).setCellValue(headers[column]);
            }
            for (int rowIndex = 0; rowIndex < dataRows.length; rowIndex++) {
                Row row = workbook.getSheetAt(0).createRow(rowIndex + 1);
                for (int column = 0; column < dataRows[rowIndex].length; column++) {
                    row.createCell(column).setCellValue(dataRows[rowIndex][column]);
                }
            }
            workbook.write(output);
        }
        return file;
    }
}
