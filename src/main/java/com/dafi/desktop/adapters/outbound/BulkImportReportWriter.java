package com.dafi.desktop.adapters.outbound;

import com.dafi.desktop.application.client.BulkImportResult;
import com.dafi.desktop.application.client.RowRejection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes a human readable report file for a bulk client import so the user
 * can review which rows were rejected and why. Reports are stored under the
 * user's documents directory (either {@code Documentos} or {@code Documents},
 * whichever exists on the system), inside a {@code DAFI} subfolder, keeping
 * them easy to find without technical knowledge.
 */
public final class BulkImportReportWriter {

    private static final Logger log = LoggerFactory.getLogger(BulkImportReportWriter.class);

    private static final String REPORT_FOLDER = "DAFI";
    private static final String FILE_PREFIX = "carga-clientes-";
    private static final DateTimeFormatter TIMESTAMP_FILE =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter TIMESTAMP_HUMAN =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private BulkImportReportWriter() {
    }

    /**
     * Writes the report for the given import result into the user's
     * documents folder.
     *
     * @param sourceFileName name of the Excel file that was imported
     * @param result         completed import result to describe
     * @return absolute path of the generated report, or {@code null} when
     *         the report could not be written (the import itself is never
     *         affected by a reporting failure)
     */
    public static Path writeReport(String sourceFileName, BulkImportResult result) {
        try {
            Path reportDirectory = resolveReportDirectory();
            Path reportFile = nextAvailableReportFile(reportDirectory);
            Files.writeString(reportFile, render(sourceFileName, result), StandardCharsets.UTF_8);
            log.info("Reporte de carga masiva generado: {}", reportFile);
            return reportFile;
        } catch (IOException | RuntimeException e) {
            log.error("No se pudo generar el reporte de carga masiva", e);
            return null;
        }
    }

    /**
     * Resolves the documents directory of the current user, accepting both
     * the Spanish ({@code Documentos}) and English ({@code Documents})
     * naming; falls back to creating {@code Documents} and finally to the
     * home directory when nothing else works.
     */
    private static Path resolveDocumentsDirectory() throws IOException {
        String userHome = System.getProperty("user.home");
        for (String folderName : new String[]{"Documentos", "Documents"}) {
            Path candidate = Path.of(userHome, folderName);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        Path created = Path.of(userHome, "Documents");
        Files.createDirectories(created);
        return created;
    }

    private static Path resolveReportDirectory() throws IOException {
        Path reportDirectory = resolveDocumentsDirectory().resolve(REPORT_FOLDER);
        Files.createDirectories(reportDirectory);
        return reportDirectory;
    }

    private static Path nextAvailableReportFile(Path reportDirectory) {
        Path candidate = reportDirectory.resolve(FILE_PREFIX
                + TIMESTAMP_FILE.format(LocalDateTime.now()) + ".log");
        int sequence = 1;
        while (Files.exists(candidate)) {
            candidate = reportDirectory.resolve(FILE_PREFIX
                    + TIMESTAMP_FILE.format(LocalDateTime.now()) + "-" + (++sequence) + ".log");
        }
        return candidate;
    }

    private static String render(String sourceFileName, BulkImportResult result) {
        StringBuilder report = new StringBuilder();
        appendHeader(report, sourceFileName);
        appendSummary(report, result);
        appendUnknownColumns(report, result.unknownColumns());
        appendRejections(report, result.rejections());
        return report.toString();
    }

    private static void appendHeader(StringBuilder report, String sourceFileName) {
        report.append("==========================================================\n")
              .append(" DAFI - Carga masiva de clientes\n")
              .append(" Archivo: ").append(sourceFileName).append('\n')
              .append(" Fecha:   ").append(TIMESTAMP_HUMAN.format(LocalDateTime.now())).append('\n')
              .append("==========================================================\n\n");
    }

    private static void appendSummary(StringBuilder report, BulkImportResult result) {
        report.append("Registros encontrados: ").append(result.totalRows()).append('\n')
              .append("Clientes agregados:    ").append(result.importedCount()).append('\n')
              .append("Registros con errores: ").append(result.rejectedCount()).append('\n');
    }

    private static void appendUnknownColumns(StringBuilder report, List<String> unknownColumns) {
        if (unknownColumns.isEmpty()) {
            return;
        }
        report.append("\nColumnas no reconocidas (se ignoraron): ")
              .append(String.join(", ", unknownColumns)).append('\n');
    }

    private static void appendRejections(StringBuilder report, List<RowRejection> rejections) {
        if (rejections.isEmpty()) {
            return;
        }
        report.append("\n--- Detalle de errores por fila ---\n");
        for (RowRejection rejection : rejections) {
            report.append("\nFila ").append(rejection.rowNumber()).append(":\n");
            for (String error : rejection.errors()) {
                report.append("  - ").append(error).append('\n');
            }
        }
    }
}
