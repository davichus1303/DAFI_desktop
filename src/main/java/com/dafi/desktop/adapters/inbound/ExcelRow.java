package com.dafi.desktop.adapters.inbound;

import java.util.Map;

/**
 * Immutable value holding one data row read from an Excel sheet.
 * The row number is 1-based as shown in the spreadsheet application,
 * so it can be reported directly to the user when the row is rejected.
 *
 * @param rowNumber 1-based sheet row number (header row included in counting)
 * @param values    column header mapped to the raw text of its cell
 */
public record ExcelRow(int rowNumber, Map<String, String> values) {

    /**
     * Returns the trimmed cell text for the given header, or an empty
     * string when the column is absent or the cell is blank.
     *
     * @param header normalized column header to look up
     * @return trimmed cell content, never {@code null}
     */
    public String value(String header) {
        String raw = values.get(header);
        return raw == null ? "" : raw.trim();
    }
}
