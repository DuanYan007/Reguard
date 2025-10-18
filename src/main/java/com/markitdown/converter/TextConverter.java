package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Text document converter that processes plain text files and converts them to Markdown.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class TextConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(TextConverter.class);
    private static final Set<String> SUPPORTED_FORMATS = Set.of("txt", "md", "markdown", "csv", "log", "json", "xml");

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting text file: {}", filePath);

        try {
            // Read the file content
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            // Detect file format
            String format = detectFileFormat(filePath);

            // Extract metadata
            Map<String, Object> metadata = extractMetadata(filePath, content, format, options);

            // Convert content to Markdown
            String markdownContent = convertToMarkdown(content, format, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to read text file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "text/plain".equals(mimeType) ||
               "text/markdown".equals(mimeType) ||
               "text/csv".equals(mimeType) ||
               "application/json".equals(mimeType) ||
               "application/xml".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 50; // Lower priority as it's a fallback converter
    }

    @Override
    public String getName() {
        return "TextConverter";
    }

    /**
     * Detects the format of the text file based on its extension.
     *
     * @param filePath the file path
     * @return the detected format
     */
    private String detectFileFormat(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();

        switch (extension) {
            case "md":
            case "markdown":
                return "markdown";
            case "csv":
                return "csv";
            case "json":
                return "json";
            case "xml":
                return "xml";
            case "log":
                return "log";
            default:
                return "plain";
        }
    }

    /**
     * Extracts metadata from the text file.
     *
     * @param filePath the file path
     * @param content  the file content
     * @param format   the detected format
     * @param options  conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(Path filePath, String content, String format, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // File information
            metadata.put("fileName", filePath.getFileName().toString());
            metadata.put("fileSize", filePath.toFile().length());
            metadata.put("format", format);

            // Content statistics
            String[] lines = content.split("\\r?\\n");
            metadata.put("lineCount", lines.length);
            metadata.put("characterCount", content.length());
            metadata.put("wordCount", countWords(content));

            // Format-specific metadata
            if ("csv".equals(format)) {
                extractCsvMetadata(content, metadata);
            } else if ("json".equals(format)) {
                extractJsonMetadata(content, metadata);
            } else if ("xml".equals(format)) {
                extractXmlMetadata(content, metadata);
            }

            metadata.put("conversionTime", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * Extracts metadata specific to CSV files.
     *
     * @param content  the CSV content
     * @param metadata the metadata map to update
     */
    private void extractCsvMetadata(String content, Map<String, Object> metadata) {
        String[] lines = content.split("\\r?\\n");
        if (lines.length > 0) {
            String firstLine = lines[0];
            String[] columns = firstLine.split(",");
            metadata.put("columnCount", columns.length);
            metadata.put("rowCount", lines.length - 1); // Exclude header
            metadata.put("hasHeader", true);
        }
    }

    /**
     * Extracts metadata specific to JSON files.
     *
     * @param content  the JSON content
     * @param metadata the metadata map to update
     */
    private void extractJsonMetadata(String content, Map<String, Object> metadata) {
        try {
            // Simple validation - check if it looks like valid JSON
            String trimmed = content.trim();
            boolean isValidJson = (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                                 (trimmed.startsWith("[") && trimmed.endsWith("]"));
            metadata.put("isValidJson", isValidJson);
        } catch (Exception e) {
            metadata.put("isValidJson", false);
        }
    }

    /**
     * Extracts metadata specific to XML files.
     *
     * @param content  the XML content
     * @param metadata the metadata map to update
     */
    private void extractXmlMetadata(String content, Map<String, Object> metadata) {
        try {
            // Simple validation - check if it looks like valid XML
            String trimmed = content.trim();
            boolean isValidXml = trimmed.startsWith("<") && trimmed.endsWith(">");
            metadata.put("isValidXml", isValidXml);

            if (isValidXml) {
                // Count XML tags (simple approach)
                int tagCount = 0;
                int index = 0;
                while ((index = content.indexOf('<', index)) != -1) {
                    tagCount++;
                    index++;
                }
                metadata.put("tagCount", tagCount);
            }
        } catch (Exception e) {
            metadata.put("isValidXml", false);
        }
    }

    /**
     * Converts text content to Markdown format.
     *
     * @param content  the original text content
     * @param format   the detected format
     * @param metadata the document metadata
     * @param options  conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(String content, String format, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        // Add title if available
        if (options.isIncludeMetadata() && metadata.containsKey("fileName")) {
            String fileName = (String) metadata.get("fileName");
            String title = getFileNameWithoutExtension(fileName);
            markdown.append("# ").append(title).append("\n\n");
        }

        // Add metadata section if enabled
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## File Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null && !entry.getKey().equals("fileName")) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Process content based on format
        markdown.append("## Content\n\n");

        switch (format) {
            case "markdown":
                markdown.append(content);
                break;
            case "csv":
                markdown.append(convertCsvToMarkdown(content));
                break;
            case "json":
                markdown.append(convertJsonToMarkdown(content));
                break;
            case "xml":
                markdown.append(convertXmlToMarkdown(content));
                break;
            case "log":
                markdown.append(convertLogToMarkdown(content));
                break;
            default:
                markdown.append(convertPlainTextToMarkdown(content));
                break;
        }

        return markdown.toString();
    }

    /**
     * Converts CSV content to Markdown table.
     *
     * @param csvContent the CSV content
     * @return Markdown table
     */
    private String convertCsvToMarkdown(String csvContent) {
        String[] lines = csvContent.split("\\r?\\n");
        if (lines.length == 0) {
            return "*Empty CSV file*\n\n";
        }

        StringBuilder markdown = new StringBuilder();

        // Process header row
        String[] headers = lines[0].split(",");
        markdown.append("| ");
        for (String header : headers) {
            markdown.append(header.trim()).append(" | ");
        }
        markdown.append("\n");

        // Add separator
        markdown.append("| ");
        for (int i = 0; i < headers.length; i++) {
            markdown.append(" --- | ");
        }
        markdown.append("\n");

        // Process data rows
        for (int i = 1; i < lines.length; i++) {
            String[] cells = lines[i].split(",");
            markdown.append("| ");
            for (String cell : cells) {
                markdown.append(cell.trim()).append(" | ");
            }
            markdown.append("\n");
        }

        markdown.append("\n");
        return markdown.toString();
    }

    /**
     * Converts JSON content to Markdown code block.
     *
     * @param jsonContent the JSON content
     * @return Markdown with JSON code block
     */
    private String convertJsonToMarkdown(String jsonContent) {
        return "```json\n" + jsonContent + "\n```\n\n";
    }

    /**
     * Converts XML content to Markdown code block.
     *
     * @param xmlContent the XML content
     * @return Markdown with XML code block
     */
    private String convertXmlToMarkdown(String xmlContent) {
        return "```xml\n" + xmlContent + "\n```\n\n";
    }

    /**
     * Converts log content to formatted Markdown.
     *
     * @param logContent the log content
     * @return formatted Markdown
     */
    private String convertLogToMarkdown(String logContent) {
        String[] lines = logContent.split("\\r?\\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // Detect log levels and format accordingly
                if (trimmed.toUpperCase().contains("ERROR")) {
                    markdown.append("**ERROR:** ").append(trimmed).append("\n");
                } else if (trimmed.toUpperCase().contains("WARN")) {
                    markdown.append("**WARNING:** ").append(trimmed).append("\n");
                } else if (trimmed.toUpperCase().contains("INFO")) {
                    markdown.append("*INFO:* ").append(trimmed).append("\n");
                } else {
                    markdown.append(trimmed).append("\n");
                }
            }
        }

        markdown.append("\n");
        return markdown.toString();
    }

    /**
     * Converts plain text content to Markdown.
     *
     * @param textContent the plain text content
     * @return formatted Markdown
     */
    private String convertPlainTextToMarkdown(String textContent) {
        String[] lines = textContent.split("\\r?\\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                markdown.append("\n");
            } else if (trimmed.startsWith("#")) {
                // Preserve existing headings
                markdown.append(trimmed).append("\n\n");
            } else if (trimmed.startsWith(" ") || trimmed.startsWith("\t")) {
                // Code block or indented text
                markdown.append("    ").append(trimmed).append("\n");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                // List items
                markdown.append(trimmed).append("\n");
            } else {
                // Regular paragraph
                markdown.append(trimmed).append("\n\n");
            }
        }

        return markdown.toString();
    }

    /**
     * Counts the number of words in the content.
     *
     * @param content the content to analyze
     * @return word count
     */
    private int countWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }

    /**
     * Gets the file extension from a file name.
     *
     * @param fileName the file name
     * @return the file extension (without the dot), or empty string if no extension
     */
    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * Gets the file name without extension.
     *
     * @param fileName the file name
     * @return the file name without extension
     */
    private String getFileNameWithoutExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
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

    /**
     * Checks if a file format is supported by this converter.
     *
     * @param fileExtension the file extension
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedFormat(String fileExtension) {
        return SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    /**
     * Gets all supported text formats.
     *
     * @return a set of supported file extensions
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}