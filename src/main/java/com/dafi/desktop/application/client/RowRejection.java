package com.dafi.desktop.application.client;

import java.util.List;

/**
 * One Excel row rejected during a bulk import, together with every
 * validation error found on it. The row number is the 1-based number
 * shown in the spreadsheet application.
 *
 * @param rowNumber 1-based sheet row number of the rejected record
 * @param errors    human readable validation messages (never empty)
 */
public record RowRejection(int rowNumber, List<String> errors) {
}
