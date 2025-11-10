package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class PptxConverter
 * @brief PowerPoint文档转换器，用于将PPTX文件转换为Markdown格式
 * @details 使用Apache POI库解析PowerPoint文档，提取幻灯片内容和结构信息
 *          支持文本格式、表格、分组形状等元素的转换
 *          保持演示文稿的幻灯片顺序和层次结构
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class PptxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PptxConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting PPTX file: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XMLSlideShow pptx = new XMLSlideShow(fis)) {

            // Extract metadata
            Map<String, Object> metadata = extractMetadata(pptx, options);

            // Convert presentation to Markdown
            String markdownContent = convertToMarkdown(pptx, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process PPTX file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(mimeType) ||
               "application/vnd.ms-powerpoint".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "PptxConverter";
    }

    /**
     * Extracts metadata from the PowerPoint presentation.
     *
     * @param pptx    the PowerPoint presentation
     * @param options conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(XMLSlideShow pptx, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // Presentation statistics
            metadata.put("slideCount", pptx.getSlides().size());
            metadata.put("conversionTime", LocalDateTime.now());

            // Slide size information
            Dimension pageSize = pptx.getPageSize();
            metadata.put("slideWidth", pageSize.width);
            metadata.put("slideHeight", pageSize.height);
        }

        return metadata;
    }

    /**
     * Converts PowerPoint presentation to Markdown format.
     *
     * @param pptx     the PowerPoint presentation
     * @param metadata the document metadata
     * @param options  conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(XMLSlideShow pptx, Map<String, Object> metadata, ConversionOptions options) {
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
            markdown.append("## Presentation Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Process slides
        List<XSLFSlide> slides = pptx.getSlides();
        for (int i = 0; i < slides.size(); i++) {
            processSlide(slides.get(i), i + 1, markdown, options);
        }

        return markdown.toString();
    }

    /**
     * Processes a single slide and converts it to Markdown.
     *
     * @param slide    the slide to process
     * @param slideNum the slide number (1-based)
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processSlide(XSLFSlide slide, int slideNum, StringBuilder markdown, ConversionOptions options) {
        markdown.append("## Slide ").append(slideNum).append("\n\n");

        // Process slide title and content
        processSlideShapes(slide, markdown, options);

        markdown.append("---\n\n");
    }

    /**
     * Processes all shapes in a slide.
     *
     * @param slide    the slide containing shapes
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processSlideShapes(XSLFSlide slide, StringBuilder markdown, ConversionOptions options) {
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                processTextShape((XSLFTextShape) shape, markdown, options);
            } else if (shape instanceof XSLFTable && options.isIncludeTables()) {
                processTable((XSLFTable) shape, markdown, options);
            } else if (shape instanceof XSLFGroupShape) {
                processGroupShape((XSLFGroupShape) shape, markdown, options);
            }
        }
    }

    /**
     * Processes a text shape and converts it to Markdown.
     *
     * @param textShape the text shape to process
     * @param markdown  the markdown output builder
     * @param options   conversion options
     */
    private void processTextShape(XSLFTextShape textShape, StringBuilder markdown, ConversionOptions options) {
        List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();

        for (XSLFTextParagraph paragraph : paragraphs) {
            String text = paragraph.getText();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }

            // Determine if this is a title based on position or formatting
            boolean isTitle = isTitleShape(textShape, paragraph);

            if (isTitle) {
                markdown.append("### ").append(text.trim()).append("\n\n");
            } else {
                // Process paragraph with formatting
                String formattedText = processTextParagraph(paragraph);
                if (!formattedText.trim().isEmpty()) {
                    markdown.append(formattedText).append("\n\n");
                }
            }
        }
    }

    /**
     * Processes a text paragraph with formatting.
     *
     * @param paragraph the text paragraph
     * @return formatted text
     */
    private String processTextParagraph(XSLFTextParagraph paragraph) {
        StringBuilder formatted = new StringBuilder();

        for (XSLFTextRun run : paragraph.getTextRuns()) {
            String runText = run.getRawText();
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
            } else if (run.isUnderlined()) {
                formatted.append("<u>").append(runText).append("</u>");
            } else if (run.isStrikethrough()) {
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
    private void processTable(XSLFTable table, StringBuilder markdown, ConversionOptions options) {
        if (!options.isIncludeTables()) {
            return;
        }

        List<XSLFTableRow> rows = table.getRows();
        if (rows.isEmpty()) {
            return;
        }

        markdown.append("\n");

        // Process each row
        for (int i = 0; i < rows.size(); i++) {
            XSLFTableRow row = rows.get(i);
            List<XSLFTableCell> cells = row.getCells();

            if (cells.isEmpty()) {
                continue;
            }

            // Create table row
            markdown.append("| ");
            for (XSLFTableCell cell : cells) {
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
     * Processes a group shape by processing its contained shapes.
     *
     * @param groupShape the group shape to process
     * @param markdown   the markdown output builder
     * @param options    conversion options
     */
    private void processGroupShape(XSLFGroupShape groupShape, StringBuilder markdown, ConversionOptions options) {
        for (XSLFShape shape : groupShape.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                processTextShape((XSLFTextShape) shape, markdown, options);
            } else if (shape instanceof XSLFTable && options.isIncludeTables()) {
                processTable((XSLFTable) shape, markdown, options);
            }
        }
    }

    /**
     * Determines if a text shape is likely a title based on its properties.
     *
     * @param textShape the text shape to check
     * @param paragraph the text paragraph
     * @return true if it's likely a title
     */
    private boolean isTitleShape(XSLFTextShape textShape, XSLFTextParagraph paragraph) {
        // Check if it's in a title placeholder position
        try {
            if (textShape.getPlaceholder() != null) {
                // Simplified check - if it has a placeholder, it might be important
                return true;
            }
        } catch (Exception e) {
            // Ignore placeholder checking if API is not available
        }

        // Check font size (titles are usually larger)
        if (!paragraph.getTextRuns().isEmpty()) {
            XSLFTextRun run = paragraph.getTextRuns().get(0);
            Double fontSize = run.getFontSize();
            if (fontSize != null && fontSize > 30) { // Larger than 30pt is likely a title
                return true;
            }
        }

        // Check text length (titles are usually shorter)
        String text = paragraph.getText();
        if (text != null && text.trim().length() < 100 && text.trim().length() > 0) {
            // Check if it's likely a title based on capitalization
            String trimmed = text.trim();
            return Character.isUpperCase(trimmed.charAt(0)) &&
                   (!trimmed.contains(".") || trimmed.split("\\.").length == 1);
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