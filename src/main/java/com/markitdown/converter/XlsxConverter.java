package com.markitdown.converter;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Excel spreadsheet converter that extracts data and structure from XLSX files.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class XlsxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(XlsxConverter.class);

    private MarkdownBuilder markdownBuilder;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting XLSX file: {}", filePath);
        // ToDo: MarkdownConfig 并未和 Conversion Options 共享元素，待解决
        markdownBuilder = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Extract metadata
            Map<String, Object> metadata = extractMetadata(workbook, options);

            // Convert workbook to Markdown
            String markdownContent = convertToMarkdown(workbook, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process XLSX file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType) ||
               "application/vnd.ms-excel".equals(mimeType) ||
               "application/vnd.ms-excel.sheet.macroEnabled.12".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "XlsxConverter";
    }

    /**
     * Extracts metadata from the Excel workbook.
     *
     * @param workbook the Excel workbook
     * @param options  conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(Workbook workbook, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // Workbook statistics
            metadata.put("sheetCount", workbook.getNumberOfSheets());
            metadata.put("activeSheet", workbook.getActiveSheetIndex());
            metadata.put("conversionTime", LocalDateTime.now());

            // Calculate total cells (approximate)
            int totalCells = 0;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                totalCells += estimateSheetSize(sheet);
            }
            metadata.put("estimatedCellCount", totalCells);
        }

        return metadata;
    }

    /**
     * Estimates the number of cells in a sheet.
     *
     * @param sheet the sheet to analyze
     * @return estimated cell count
     */
    private int estimateSheetSize(Sheet sheet) {
        int cellCount = 0;
        for (Row row : sheet) {
            cellCount += row.getPhysicalNumberOfCells();
        }
        return cellCount;
    }

    /**
     * Converts Excel workbook to Markdown format.
     *
     * @param workbook the Excel workbook
     * @param metadata the document metadata
     * @param options  conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(Workbook workbook, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        // Add title if available
        if (options.isIncludeMetadata() && metadata.containsKey("title")) {
            String title = (String) metadata.get("title");
            if (title != null && !title.trim().isEmpty()) {
                markdown.append("# ").append(title.trim()).append("\n\n");
            }
        }

        // Add metadata section if enabled
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Workbook Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Process all sheets
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            processSheet(sheet, i + 1, markdown, options);
        }

        return markdown.toString();
    }

    /**
     * Processes a single sheet and converts it to Markdown.
     *
     * @param sheet    the sheet to process
     * @param sheetNum the sheet number (1-based)
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processSheet(Sheet sheet, int sheetNum, StringBuilder markdown, ConversionOptions options) {
        String sheetName = sheet.getSheetName();
        markdown.append("## Sheet ").append(sheetNum).append(": ").append(sheetName).append("\n\n");

        if (!options.isIncludeTables()) {
            markdown.append("*Tables are disabled in conversion options*\n\n");
            return;
        }

        // Find the data range
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();

        if (firstRow < 0 || lastRow < 0 || lastRow < firstRow) {
            markdown.append("*Empty sheet*\n\n---\n\n");
            return;
        }

        // Determine if the first row is likely a header
        boolean hasHeader = detectHeaderRow(sheet, firstRow);

        // Process the data
        if (hasHeader) {
            processTableWithHeader(sheet, firstRow, lastRow, markdown, options);
        } else {
            processTableWithoutHeader(sheet, firstRow, lastRow, markdown, options);
        }

        markdown.append("---\n\n");
    }

    /**
     * Detects if the first row is likely a header row.
     *
     * @param sheet    the sheet to analyze
     * @param firstRow the first row index
     * @return true if first row is likely a header
     */
    private boolean detectHeaderRow(Sheet sheet, int firstRow) {
        Row firstRowData = sheet.getRow(firstRow);
        if (firstRowData == null) {
            return false;
        }

        int nonEmptyCells = 0;
        int stringCells = 0;
        int totalCells = firstRowData.getPhysicalNumberOfCells();

        for (Cell cell : firstRowData) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                nonEmptyCells++;
                if (cell.getCellType() == CellType.STRING) {
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        stringCells++;
                    }
                }
            }
        }

        // Consider it a header if most cells are non-empty and many are strings
        return totalCells > 0 && (double) nonEmptyCells / totalCells > 0.7 && (double) stringCells / totalCells > 0.5;
    }

    /**
     * Processes a table with a header row.
     *
     * @param sheet    the sheet containing the table
     * @param firstRow the first row index
     * @param lastRow  the last row index
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processTableWithHeader(Sheet sheet, int firstRow, int lastRow,
                                       StringBuilder markdown, ConversionOptions options) {
        // Process header row
        Row headerRow = sheet.getRow(firstRow);
        if (headerRow != null) {
            markdown.append("| ");
            for (Cell cell : headerRow) {
                String cellValue = getCellValueAsString(cell).trim();
                markdown.append(cellValue).append(" | ");
            }
            markdown.append("\n");

            // Add separator
            markdown.append("| ");
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                markdown.append(" --- | ");
            }
            markdown.append("\n");
        }

        // Process data rows
        for (int rowNum = firstRow + 1; rowNum <= lastRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;

            markdown.append("| ");
            for (Cell cell : row) {
                String cellValue = getCellValueAsString(cell).trim();
                markdown.append(cellValue).append(" | ");
            }
            markdown.append("\n");
        }

        markdown.append("\n");
    }

    /**
     * Processes a table without a header row.
     *
     * @param sheet    the sheet containing the table
     * @param firstRow the first row index
     * @param lastRow  the last row index
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processTableWithoutHeader(Sheet sheet, int firstRow, int lastRow,
                                         StringBuilder markdown, ConversionOptions options) {
        // Process all rows as data
        for (int rowNum = firstRow; rowNum <= lastRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;

            markdown.append("| ");
            for (Cell cell : row) {
                String cellValue = getCellValueAsString(cell).trim();
                markdown.append(cellValue).append(" | ");
            }
            markdown.append("\n");
        }

        markdown.append("\n");
    }

    /**
     * Converts a cell value to string representation.
     *
     * @param cell the cell to convert
     * @return string representation of the cell value
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Handle large numbers and decimals
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.format("%d", (long) numValue);
                    } else {
                        return String.format("%s", numValue);
                    }
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // Try to evaluate the formula
                    CellValue evaluatedValue = cell.getSheet().getWorkbook().getCreationHelper()
                            .createFormulaEvaluator().evaluate(cell);
                    if (evaluatedValue != null) {
                        switch (evaluatedValue.getCellType()) {
                            case STRING:
                                return evaluatedValue.getStringValue();
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    return cell.getDateCellValue().toString();
                                } else {
                                    double numValue = evaluatedValue.getNumberValue();
                                    if (numValue == (long) numValue) {
                                        return String.format("%d", (long) numValue);
                                    } else {
                                        return String.format("%s", numValue);
                                    }
                                }
                            case BOOLEAN:
                                return Boolean.toString(evaluatedValue.getBooleanValue());
                            default:
                                return "";
                        }
                    }
                } catch (Exception e) {
                    // If evaluation fails, return the formula itself
                    return cell.getCellFormula();
                }
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Formats metadata keys for display.
     *
     * @param key the metadata key
     * @return formatted key
     */
    private String formatMetadataKey(String key) {
        // Convert camelCase to Title Case
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }
}