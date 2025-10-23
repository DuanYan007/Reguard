package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class HtmlConverter
 * @brief HTML文档转换器，用于将HTML文件转换为Markdown格式
 * @details 使用Jsoup库解析HTML文档，提取内容结构和元数据信息
 *          支持完整的HTML元素转换，包括标题、段落、列表、表格、链接、图片等
 *          保持文档的层次结构和格式信息，生成标准的Markdown文档
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class HtmlConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlConverter.class);

    /**
     * @brief 将HTML文件转换为Markdown格式
     * @details 主转换方法，使用Jsoup解析HTML文档，提取元数据和结构化内容
     *          递归处理HTML节点树，将各种HTML元素转换为对应的Markdown语法
     * @param filePath 要转换的HTML文件路径，不能为null
     * @param options  转换选项配置，不能为null
     * @return ConversionResult 包含Markdown内容、元数据和警告信息的转换结果
     * @throws ConversionException 当文件读取失败或转换过程中出现错误时抛出
     */
    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting HTML file: {}", filePath);

        try {
            // Parse HTML document
            Document document = Jsoup.parse(filePath.toFile(), "UTF-8");

            // Extract metadata
            Map<String, Object> metadata = extractMetadata(document, options);

            // Convert HTML to Markdown
            String markdownContent = convertToMarkdown(document, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process HTML file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    /**
     * @brief 检查是否支持指定的MIME类型
     * @details 判断转换器是否能够处理HTML文档格式
     * @param mimeType 要检查的MIME类型，不能为null
     * @return boolean true表示支持该MIME类型，false表示不支持
     */
    @Override
    public boolean supports(String mimeType) {
        return "text/html".equals(mimeType) || "application/xhtml+xml".equals(mimeType);
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
        return "HtmlConverter";
    }

    /**
     * Extracts metadata from the HTML document.
     *
     * @param document the HTML document
     * @param options  conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(Document document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // Extract title
            String title = document.title();
            if (title != null && !title.trim().isEmpty()) {
                metadata.put("title", title.trim());
            }

            // Extract meta tags
            Elements metaTags = document.select("meta");
            for (Element meta : metaTags) {
                String name = meta.attr("name");
                String property = meta.attr("property");
                String content = meta.attr("content");

                if (content != null && !content.trim().isEmpty()) {
                    if (name != null && !name.trim().isEmpty()) {
                        metadata.put(name.toLowerCase().replace(":", "_"), content.trim());
                    } else if (property != null && !property.trim().isEmpty()) {
                        metadata.put(property.toLowerCase().replace(":", "_"), content.trim());
                    }
                }
            }

            // Extract language
            String language = document.select("html").attr("lang");
            if (!language.isEmpty()) {
                metadata.put("language", language);
            }

            // Document statistics
            metadata.put("headingCount", document.select("h1, h2, h3, h4, h5, h6").size());
            metadata.put("linkCount", document.select("a[href]").size());
            metadata.put("imageCount", document.select("img[src]").size());
            metadata.put("tableCount", document.select("table").size());
            metadata.put("conversionTime", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * Converts HTML document to Markdown format.
     *
     * @param document the HTML document
     * @param metadata the document metadata
     * @param options  conversion options
     * @return Markdown formatted content
     */
    private String convertToMarkdown(Document document, Map<String, Object> metadata, ConversionOptions options) {
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
                if (entry.getValue() != null && !entry.getKey().equals("title")) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Process document content
        Element body = document.body();
        if (body != null) {
            processNode(body, markdown, options, 0);
        }

        return markdown.toString();
    }

    /**
     * Processes a node and its children, converting to Markdown.
     *
     * @param node     the node to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     * @param depth    the current depth in the document tree
     */
    private void processNode(Node node, StringBuilder markdown, ConversionOptions options, int depth) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (!text.isEmpty()) {
                markdown.append(text).append(" ");
            }
        } else if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName().toLowerCase();

            switch (tagName) {
                case "h1":
                    markdown.append("\n# ").append(element.text()).append("\n\n");
                    break;
                case "h2":
                    markdown.append("\n## ").append(element.text()).append("\n\n");
                    break;
                case "h3":
                    markdown.append("\n### ").append(element.text()).append("\n\n");
                    break;
                case "h4":
                    markdown.append("\n#### ").append(element.text()).append("\n\n");
                    break;
                case "h5":
                    markdown.append("\n##### ").append(element.text()).append("\n\n");
                    break;
                case "h6":
                    markdown.append("\n###### ").append(element.text()).append("\n\n");
                    break;
                case "p":
                    markdown.append("\n");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("\n\n");
                    break;
                case "br":
                    markdown.append("  \n");
                    break;
                case "strong":
                case "b":
                    markdown.append("**");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("**");
                    break;
                case "em":
                case "i":
                    markdown.append("*");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("*");
                    break;
                case "u":
                    markdown.append("<u>");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("</u>");
                    break;
                case "del":
                case "s":
                case "strike":
                    markdown.append("~~");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("~~");
                    break;
                case "code":
                    markdown.append("`");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("`");
                    break;
                case "pre":
                    String codeText = element.text();
                    markdown.append("\n```\n").append(codeText).append("\n```\n\n");
                    break;
                case "blockquote":
                    String quoteText = element.text();
                    markdown.append("\n> ").append(quoteText.replace("\n", "\n> ")).append("\n\n");
                    break;
                case "ul":
                case "ol":
                    processList(element, markdown, options, depth + 1);
                    break;
                case "li":
                    processListItem(element, markdown, options, depth + 1);
                    break;
                case "a":
                    processLink(element, markdown, options);
                    break;
                case "img":
                    processImage(element, markdown, options);
                    break;
                case "table":
                    if (options.isIncludeTables()) {
                        processTable(element, markdown, options);
                    }
                    break;
                case "hr":
                    markdown.append("\n---\n\n");
                    break;
                case "div":
                case "section":
                case "article":
                case "main":
                case "header":
                case "footer":
                case "nav":
                case "aside":
                    // Block elements - process children with appropriate spacing
                    markdown.append("\n");
                    processChildren(element, markdown, options, depth + 1);
                    markdown.append("\n");
                    break;
                case "span":
                case "small":
                case "big":
                case "sub":
                case "sup":
                case "mark":
                case "q":
                case "cite":
                case "dfn":
                case "abbr":
                case "time":
                case "var":
                case "samp":
                case "kbd":
                    // Inline elements - just process children
                    processChildren(element, markdown, options, depth + 1);
                    break;
                default:
                    // Unknown element - try to process children
                    processChildren(element, markdown, options, depth + 1);
                    break;
            }
        }
    }

    /**
     * Processes all children of an element.
     *
     * @param element  the element whose children to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     * @param depth    the current depth
     */
    private void processChildren(Element element, StringBuilder markdown, ConversionOptions options, int depth) {
        for (Node child : element.childNodes()) {
            processNode(child, markdown, options, depth);
        }
    }

    /**
     * Processes a list element.
     *
     * @param list    the list element to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     * @param depth    the current depth
     */
    private void processList(Element list, StringBuilder markdown, ConversionOptions options, int depth) {
        markdown.append("\n");
        for (Element li : list.select("li")) {
            String indent = "  ".repeat(Math.max(0, depth - 1));
            if (list.tagName().equals("ol")) {
                markdown.append(indent).append("1. ");
            } else {
                markdown.append(indent).append("- ");
            }
            processListItemContent(li, markdown, options, depth);
            markdown.append("\n");
        }
        markdown.append("\n");
    }

    /**
     * Processes a list item.
     *
     * @param li       the list item to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     * @param depth    the current depth
     */
    private void processListItem(Element li, StringBuilder markdown, ConversionOptions options, int depth) {
        String text = li.text().trim();
        markdown.append(text);
    }

    /**
     * Processes the content of a list item, handling nested lists.
     *
     * @param li       the list item
     * @param markdown the markdown output builder
     * @param options  conversion options
     * @param depth    the current depth
     */
    private void processListItemContent(Element li, StringBuilder markdown, ConversionOptions options, int depth) {
        for (Node child : li.childNodes()) {
            if (child instanceof Element) {
                Element childElement = (Element) child;
                if (childElement.tagName().equals("ul") || childElement.tagName().equals("ol")) {
                    // Handle nested list
                    processList(childElement, markdown, options, depth + 1);
                } else {
                    processNode(child, markdown, options, depth);
                }
            } else {
                processNode(child, markdown, options, depth);
            }
        }
    }

    /**
     * Processes a link element.
     *
     * @param link     the link element to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processLink(Element link, StringBuilder markdown, ConversionOptions options) {
        String href = link.attr("href");
        String text = link.text();

        if (!text.isEmpty()) {
            if (!href.isEmpty()) {
                markdown.append("[").append(text).append("](").append(href).append(")");
            } else {
                markdown.append(text);
            }
        }
    }

    /**
     * Processes an image element.
     *
     * @param img      the image element to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processImage(Element img, StringBuilder markdown, ConversionOptions options) {
        if (!options.isIncludeImages()) {
            return;
        }

        String src = img.attr("src");
        String alt = img.attr("alt");

        if (!src.isEmpty()) {
            markdown.append("![");
            if (!alt.isEmpty()) {
                markdown.append(alt);
            }
            markdown.append("](").append(src).append(")");
        }
    }

    /**
     * Processes a table element.
     *
     * @param table    the table element to process
     * @param markdown the markdown output builder
     * @param options  conversion options
     */
    private void processTable(Element table, StringBuilder markdown, ConversionOptions options) {
        if (!options.isIncludeTables()) {
            return;
        }

        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return;
        }

        markdown.append("\n");

        boolean isFirstRow = true;
        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) {
                continue;
            }

            markdown.append("| ");
            for (Element cell : cells) {
                String cellText = cell.text().trim().replace("|", "\\|");
                markdown.append(cellText).append(" | ");
            }
            markdown.append("\n");

            // Add header separator after first row
            if (isFirstRow) {
                markdown.append("|");
                for (int i = 0; i < cells.size(); i++) {
                    markdown.append(" --- |");
                }
                markdown.append("\n");
                isFirstRow = false;
            }
        }

        markdown.append("\n");
    }

    /**
     * Formats metadata keys for display.
     *
     * @param key the metadata key
     * @return formatted key
     */
    private String formatMetadataKey(String key) {
        // Convert underscores and colons to spaces and capitalize
        return key.replaceAll("[:_]", " ")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }
}