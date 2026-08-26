package com.dafi.desktop.adapters.inbound;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic reader that converts the first sheet of an Excel workbook
 * ({@code .xlsx} or {@code .xls}) into a list of header-to-value rows.
 * The first non-empty row is taken as the header row; every remaining
 * non-empty row becomes an {@link ExcelRow} whose keys are the trimmed
 * header texts and whose values are the cell contents rendered as plain
 * text. Cell rendering uses {@link DataFormatter}, so numeric and date
 * cells are returned exactly as the spreadsheet displays them.
 */
public final class ExcelRowReader {

    private ExcelRowReader() {
    }

    /**
     * Reads the first sheet of the given workbook file.
     *
     * @param excelFile path to a {@code .xlsx} or {@code .xls} file
     * @return one entry per data row, in sheet order (never {@code null})
     * @throws IOException if the file cannot be opened or parsed
     */
    public static List<ExcelRow> readRows(Path excelFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            return readSheet(workbook.getSheetAt(0));
        }
    }

    private static List<ExcelRow> readSheet(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        int headerRowIndex = findFirstNonEmptyRow(sheet, formatter);
        if (headerRowIndex < 0) {
            return new ArrayList<>();
        }

        List<String> headers = extractNonBlankCells(sheet.getRow(headerRowIndex), formatter);
        return readDataRowsAfterHeader(sheet, formatter, headers, headerRowIndex);
    }

    private static int findFirstNonEmptyRow(Sheet sheet, DataFormatter formatter) {
        for (Row row : sheet) {
            if (!extractNonBlankCells(row, formatter).isEmpty()) {
                return row.getRowNum();
            }
        }
        return -1;
    }

    private static List<String> extractNonBlankCells(Row row, DataFormatter formatter) {
        List<String> cells = new ArrayList<>();
        for (int column = 0; column < row.getLastCellNum(); column++) {
            String text = formattedCell(row, column, formatter);
            if (!text.isBlank()) {
                cells.add(text);
            }
        }
        return cells;
    }

    private static List<ExcelRow> readDataRowsAfterHeader(Sheet sheet, DataFormatter formatter,
                                                          List<String> headers, int headerRowIndex) {
        List<ExcelRow> rows = new ArrayList<>();
        for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            appendRowIfAnyValuePresent(rows, row, headers, formatter);
        }
        return rows;
    }

    private static void appendRowIfAnyValuePresent(List<ExcelRow> rows, Row row,
                                                   List<String> headers, DataFormatter formatter) {
        if (row == null) {
            return;
        }

        Map<String, String> values = new LinkedHashMap<>();
        boolean anyValuePresent = false;
        for (int column = 0; column < headers.size(); column++) {
            String text = formattedCell(row, column, formatter);
            values.put(headers.get(column), text);
            anyValuePresent = anyValuePresent || !text.isBlank();
        }
        if (anyValuePresent) {
            rows.add(new ExcelRow(row.getRowNum() + 1, values));
        }
    }

    private static String formattedCell(Row row, int columnIndex, DataFormatter formatter) {
        if (row.getCell(columnIndex) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(columnIndex)).trim();
    }
}
