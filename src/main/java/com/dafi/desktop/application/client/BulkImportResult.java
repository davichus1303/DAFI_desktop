package com.dafi.desktop.application.client;

import java.util.List;

/**
 * Summary of a bulk client import run: how many rows were read, how many
 * became valid clients and which rows were rejected with their reasons.
 * When at least one row was rejected, {@code reportPath} points to the
 * detailed report file written to the user's documents folder.
 *
 * @param totalRows      number of data rows found in the spreadsheet
 * @param importedCount  number of clients successfully imported
 * @param rejections     rejected rows with their validation errors
 * @param unknownColumns column headers present in the sheet but not mapped
 *                       to any known client field
 * @param reportPath     absolute path of the generated report file,
 *                       or {@code null} when no report was needed or the
 *                       report could not be written
 */
public record BulkImportResult(int totalRows,
                               int importedCount,
                               List<RowRejection> rejections,
                               List<String> unknownColumns,
                               String reportPath) {

    /**
     * Returns how many rows were rejected.
     *
     * @return rejection count
     */
    public int rejectedCount() {
        return rejections.size();
    }
}
