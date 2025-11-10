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
 * @class PdfConverter
 * @brief PDF文档转换器，用于将PDF文件转换为Markdown格式
 * @details 使用Apache PDFBox库解析PDF文件，提取文本内容和元数据信息
 *          支持文档属性提取、文本清理和格式化处理
 *          保持文档的基本结构和重要信息
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class PdfConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    /**
     * @brief 将PDF文件转换为Markdown格式
     * @details 主转换方法，使用PDFBox解析PDF文档，提取元数据和文本内容
     *          对提取的文本进行清理和格式化，生成标准Markdown文档
     * @param filePath 要转换的PDF文件路径，不能为null
     * @param options  转换选项配置，不能为null
     * @return ConversionResult 包含Markdown内容、元数据和警告信息的转换结果
     * @throws ConversionException 当文件读取失败或转换过程中出现错误时抛出
     */
    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("正在转换PDF文件: {}", filePath);

        try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath.toFile());
             PDDocument document = Loader.loadPDF(fis.readAllBytes())) {
            // 提取数据
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

    /**
     * @brief 检查是否支持指定的MIME类型
     * @details 判断转换器是否能够处理PDF文档格式
     * @param mimeType 要检查的MIME类型，不能为null
     * @return boolean true表示支持该MIME类型，false表示不支持
     */
    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    /**
     * @brief 获取转换器优先级
     * @details 设置较高的优先级值，确保在多个转换器支持同一类型时优先选择此转换器
     * @return int 转换器优先级值，设置为100
     */
    @Override
    public int getPriority() {
        return 100;
    }

    /**
     * @brief 获取转换器名称
     * @details 返回转换器的唯一标识名称
     * @return String 转换器名称
     */
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
            metadata.put("作者", document.getDocumentInformation().getAuthor());
            metadata.put("主题", document.getDocumentInformation().getSubject());
            metadata.put("创建者", document.getDocumentInformation().getCreator());
            metadata.put("生产者", document.getDocumentInformation().getProducer());
            metadata.put("创建日期", document.getDocumentInformation().getCreationDate());
            metadata.put("修改日期", document.getDocumentInformation().getModificationDate());
            metadata.put("关键词", document.getDocumentInformation().getKeywords());
            metadata.put("页数量", document.getNumberOfPages());
            metadata.put("转换时刻", LocalDateTime.now());
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

        // Fix common PDF extraction issues first
        String cleaned = text.replaceAll("-\\s+", "-"); // Fix hyphenated words
        cleaned = cleaned.replaceAll("\\s*\\f\\s*", "\n\n"); // Form feeds to paragraph breaks
        cleaned = cleaned.replaceAll("\\r\\n", "\n"); // Normalize Windows line endings
        cleaned = cleaned.replaceAll("\\r", "\n"); // Normalize Mac line endings

        // Handle multiple consecutive empty lines (more than 2 newlines)
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        // Replace multiple spaces within lines with single space, but preserve line breaks
        String[] lines = cleaned.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                // Replace multiple spaces with single space within the line
                line = line.replaceAll(" +", " ");
                result.append(line);
            }
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString().trim();
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

        StringBuilder formatted = new StringBuilder();

        // Split by lines and process
        String[] lines = textContent.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                // Add paragraph break
                formatted.append("\n\n");
                continue;
            }

            // Check if this might be a heading
            if (isHeadingLine(line)) {
                // Add extra spacing before headings if not already there
                if (formatted.length() > 0 && !formatted.toString().endsWith("\n\n\n")) {
                    formatted.append("\n");
                }
                formatted.append("### ").append(line).append("\n\n");
            }
            // Check if this might be a list item
            else if (isListItem(line)) {
                formatted.append(line).append("\n");
            }
            // Regular paragraph text
            else {
                // If next line exists and is not empty, this might be a continuation
                if (i + 1 < lines.length && !lines[i + 1].trim().isEmpty() &&
                    !isHeadingLine(lines[i + 1].trim()) && !isListItem(lines[i + 1].trim())) {
                    // Continue current paragraph
                    formatted.append(line).append(" ");
                } else {
                    // End of paragraph
                    formatted.append(line).append("\n\n");
                }
            }
        }

        // Clean up extra newlines
        String result = formatted.toString();
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }

    /**
     * Determines if a line is likely a heading.
     *
     * @param line the line to check
     * @return true if the line appears to be a heading
     */
    private boolean isHeadingLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // Numbered headings like "1. Introduction"
        if (line.matches("^\\d+\\.\\s+.*")) {
            return true;
        }

        // All caps headings (shorter than 80 chars)
        if (line.length() < 80 && line.equals(line.toUpperCase()) &&
            line.matches(".*[A-Z].*") && !line.matches(".*[a-z].*")) {
            return true;
        }

        // Title case headings (first letter capitalized, relatively short)
        if (line.length() < 100 && Character.isUpperCase(line.charAt(0)) &&
            !line.matches(".*\\.$") && !line.contains(",")) {
            return true;
        }

        return false;
    }

    /**
     * Determines if a line is a list item.
     *
     * @param line the line to check
     * @return true if the line appears to be a list item
     */
    private boolean isListItem(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // Bulleted lists
        if (line.matches("^\\s*[-•*]\\s+.*")) {
            return true;
        }

        // Numbered lists
        if (line.matches("^\\s*\\d+[.)]\\s+.*")) {
            return true;
        }

        return false;
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