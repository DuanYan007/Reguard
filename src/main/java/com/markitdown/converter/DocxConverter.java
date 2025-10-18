package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Word document converter that extracts text, formatting, and structure from DOCX files.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class
DocxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocxConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting DOCX file: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {

            // Extract metadata
            Map<String, Object> metadata = extractMetadata(document, options);

            // Convert document to Markdown
            String markdownContent = convertToMarkdown(document, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process DOCX file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType) ||
               "application/msword".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "DocxConverter";
    }

    /**
     * Extracts metadata from the Word document.
     *
     * @param document the Word document
     * @param options  conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(XWPFDocument document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // Simplified metadata extraction - POI CoreProperties API may vary
            // Document statistics are more reliable

            // Document statistics
            metadata.put("paragraphCount", document.getParagraphs().size());
            metadata.put("tableCount", document.getTables().size());
            metadata.put("conversionTime", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * Converts Word document to Markdown format.
     *
     * @param document the Word document
     * @param metadata the document metadata
     * @param options  conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(XWPFDocument document, Map<String, Object> metadata, ConversionOptions options) {
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
            markdown.append("## Document Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Process document content
        processDocumentBody(document, markdown, options);

        return markdown.toString();
    }

    /**
     * Processes the main body of the document.
     *
     * @param document the Word document
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processDocumentBody(XWPFDocument document, StringBuilder markdown, ConversionOptions options) {
        markdown.append("## Content\n\n");

        // Process paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            processParagraph(paragraph, markdown, options);
        }

        // Process tables
        if (options.isIncludeTables()) {
            for (XWPFTable table : document.getTables()) {
                processTable(table, markdown, options);
            }
        }
    }

    /**
     * Processes a single paragraph and converts it to Markdown.
     *
     * @param paragraph the paragraph to process
     * @param markdown  the markdown output builder
     * @param options   conversion options
     */
    private void processParagraph(XWPFParagraph paragraph, StringBuilder markdown, ConversionOptions options) {
        String text = paragraph.getText();
        if (text == null || text.trim().isEmpty()) {
            markdown.append("\n");
            return;
        }

        // Handle headings based on style
        String style = paragraph.getStyle();
        if (style != null) {
            if (style.toLowerCase().contains("heading 1") || style.toLowerCase().contains("title")) {
                markdown.append("## ").append(text.trim()).append("\n\n");
                return;
            } else if (style.toLowerCase().contains("heading 2")) {
                markdown.append("### ").append(text.trim()).append("\n\n");
                return;
            } else if (style.toLowerCase().contains("heading 3")) {
                markdown.append("#### ").append(text.trim()).append("\n\n");
                return;
            } else if (style.toLowerCase().contains("heading 4")) {
                markdown.append("##### ").append(text.trim()).append("\n\n");
                return;
            } else if (style.toLowerCase().contains("heading 5")) {
                markdown.append("###### ").append(text.trim()).append("\n\n");
                return;
            }
        }

        // Handle list items
        if (isListItem(paragraph)) {
            String indent = getIndent(paragraph);
            markdown.append(indent).append("- ").append(text.trim()).append("\n");
            return;
        }

        // Handle regular paragraphs with formatting
        String formattedText = processParagraphFormatting(paragraph, text);
        markdown.append(formattedText).append("\n\n");
    }

    /**
     * Processes formatting within a paragraph.
     *
     * @param paragraph the paragraph
     * @param text      the paragraph text
     * @return formatted text
     */
    private String processParagraphFormatting(XWPFParagraph paragraph, String text) {
        StringBuilder formatted = new StringBuilder();

        for (XWPFRun run : paragraph.getRuns()) {
            String runText = run.getText(0);
            if (runText == null || runText.isEmpty()) {
                continue;
            }

            // Apply formatting
            if (run.isBold() && run.isItalic()) {
                formatted.append("***").append(runText).append("***");
            } else if (run.isBold()) {
                formatted.append("**").append(runText).append("**");
            } else if (run.isItalic()) {
                formatted.append("*").append(runText).append("*");
            } else if (run.isStrikeThrough()) {
                formatted.append("~~").append(runText).append("~~");
            } else {
                formatted.append(runText);
            }
        }

        return formatted.toString();
    }

    /**
     * Processes a table and converts it to Markdown.
     *
     * @param table    the table to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processTable(XWPFTable table, StringBuilder markdown, ConversionOptions options) {
        if (!options.isIncludeTables()) {
            return;
        }

        List<XWPFTableRow> rows = table.getRows();
        if (rows.isEmpty()) {
            return;
        }

        markdown.append("\n");

        // Process each row
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();

            if (cells.isEmpty()) {
                continue;
            }

            // Create table row
            markdown.append("| ");
            for (XWPFTableCell cell : cells) {
                String cellText = cell.getText().replace("\n", " ").trim();
                markdown.append(cellText).append(" | ");
            }
            markdown.append("\n");

            // Add header separator after first row
            if (i == 0) {
                markdown.append("|");
                for (int j = 0; j < cells.size(); j++) {
                    markdown.append(" --- |");
                }
                markdown.append("\n");
            }
        }

        markdown.append("\n");
    }

    /**
     * Checks if a paragraph is a list item.
     *
     * @param paragraph the paragraph to check
     * @return true if it's a list item
     */
    private boolean isListItem(XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        if (style != null) {
            return style.toLowerCase().contains("list") ||
                   style.toLowerCase().contains("bullet");
        }

        // Check numbering
        if (paragraph.getNumID() != null) {
            return true;
        }

        // Check indentation (common for list items)
        return paragraph.getIndentationLeft() > 0;
    }

    /**
     * Gets the indentation for list items.
     *
     * @param paragraph the paragraph
     * @return indentation string
     */
    private String getIndent(XWPFParagraph paragraph) {
        int indentLevel = (int) (paragraph.getIndentationLeft() / 360); // Approximate twips to spaces
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < Math.max(0, Math.min(indentLevel, 5)); i++) {
            indent.append("  ");
        }
        return indent.toString();
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