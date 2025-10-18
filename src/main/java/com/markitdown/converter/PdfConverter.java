package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * PDF document converter that extracts text and metadata from PDF files.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class PdfConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting PDF file: {}", filePath);

        try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath.toFile());
             PDDocument document = Loader.loadPDF(fis.readAllBytes())) {
            // Extract metadata
            Map<String, Object> metadata = extractMetadata(document, options);

            // Extract text content
            String textContent = extractTextContent(document, options);

            // Convert to Markdown
            String markdownContent = convertToMarkdown(textContent, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process PDF file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "PdfConverter";
    }

    /**
     * Extracts metadata from the PDF document.
     *
     * @param document the PDF document
     * @param options  conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(PDDocument document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            metadata.put("title", document.getDocumentInformation().getTitle());
            metadata.put("author", document.getDocumentInformation().getAuthor());
            metadata.put("subject", document.getDocumentInformation().getSubject());
            metadata.put("creator", document.getDocumentInformation().getCreator());
            metadata.put("producer", document.getDocumentInformation().getProducer());
            metadata.put("creationDate", document.getDocumentInformation().getCreationDate());
            metadata.put("modificationDate", document.getDocumentInformation().getModificationDate());
            metadata.put("keywords", document.getDocumentInformation().getKeywords());
            metadata.put("pageCount", document.getNumberOfPages());
            metadata.put("conversionTime", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * Extracts text content from the PDF document.
     *
     * @param document the PDF document
     * @param options  conversion options
     * @return extracted text content
     * @throws IOException if text extraction fails
     */
    private String extractTextContent(PDDocument document, ConversionOptions options) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();

        // Configure text stripper
        textStripper.setSortByPosition(true);
        textStripper.setLineSeparator("\n");

        String text = textStripper.getText(document);

        // Clean up the extracted text
        return cleanupText(text);
    }

    /**
     * Converts extracted text to Markdown format.
     *
     * @param textContent the extracted text
     * @param metadata    the document metadata
     * @param options     conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(String textContent, Map<String, Object> metadata, ConversionOptions options) {
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

        // Add the main content
        markdown.append("## Content\n\n");
        markdown.append(formatTextContent(textContent));

        return markdown.toString();
    }

    /**
     * Cleans up extracted text by removing excessive whitespace and fixing formatting.
     *
     * @param text the raw extracted text
     * @return cleaned text
     */
    private String cleanupText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Replace multiple consecutive whitespace with single space
        String cleaned = text.replaceAll("\\s+", " ");

        // Fix common PDF extraction issues
        cleaned = cleaned.replaceAll("-\\s+", "-"); // Fix hyphenated words
        cleaned = cleaned.replaceAll("\\s*\\f\\s*", "\n\n"); // Form feeds to paragraph breaks
        cleaned = cleaned.replaceAll("\\s*\\r\\n\\s*", "\n"); // Normalize line endings

        return cleaned.trim();
    }

    /**
     * Formats text content for better Markdown output.
     *
     * @param textContent the text content to format
     * @return formatted content
     */
    private String formatTextContent(String textContent) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return "";
        }

        // Split into paragraphs and format
        String[] paragraphs = textContent.split("\\n\\s*\\n");
        StringBuilder formatted = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!trimmed.isEmpty()) {
                // Detect potential headings (short lines followed by longer text)
                if (trimmed.length() < 100 && trimmed.length() > 0 &&
                    (Character.isUpperCase(trimmed.charAt(0)) || trimmed.matches("\\d+\\..*"))) {
                    formatted.append("### ").append(trimmed).append("\n\n");
                } else {
                    formatted.append(trimmed).append("\n\n");
                }
            }
        }

        return formatted.toString();
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