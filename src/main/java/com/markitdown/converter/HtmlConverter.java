package com.markitdown.converter;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
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
    private MarkdownBuilder mb;
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
        mb = new MarkdownBuilder(new MarkdownConfig());

        try {
            // 解析HTML文档
            Document document = Jsoup.parse(filePath.toFile(), "UTF-8");

            // 提取元数据
            Map<String, Object> metadata = extractMetadata(document, options);

            // 将HTML转换为Markdown
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
     * @brief 从HTML文档中提取元数据
     * @details 提取HTML文档的标题、元标签、语言信息和统计数据
     *          处理各种meta标签类型，包括name和property属性
     * @param document HTML文档对象，不能为null
     * @param options  转换选项配置，用于控制是否包含元数据
     * @return Map<String, Object> 包含HTML元数据的映射
     */
    private Map<String, Object> extractMetadata(Document document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // 提取标题
            String title = document.title();
            if (title != null && !title.trim().isEmpty()) {
                metadata.put("文件名", title.trim());
            }

            // 提取元标签
            Elements metaTags = document.select("meta");
            for (Element meta : metaTags) {
                String name = meta.attr("name");
                String property = meta.attr("属性");
                String content = meta.attr("内容");

                if (content != null && !content.trim().isEmpty()) {
                    if (name != null && !name.trim().isEmpty()) {
                        metadata.put(name.toLowerCase().replace(":", "_"), content.trim());
                    } else if (property != null && !property.trim().isEmpty()) {
                        metadata.put(property.toLowerCase().replace(":", "_"), content.trim());
                    }
                }
            }

            // 提取语言信息
            String language = document.select("html").attr("lang");
            if (!language.isEmpty()) {
                metadata.put("语言", language);
            }

            // 文档统计信息
            metadata.put("标题数量", document.select("h1, h2, h3, h4, h5, h6").size());
            metadata.put("链接数量", document.select("a[href]").size());
            metadata.put("图片数量", document.select("img[src]").size());
            metadata.put("表格数量", document.select("table").size());
            metadata.put("转换时刻", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * @brief 将HTML文档转换为Markdown格式
     * @details 生成包含标题、元数据信息和文档内容的完整Markdown文档
     *          根据转换选项控制各个部分的显示内容
     * @param document HTML文档对象，不能为null
     * @param metadata 文档元数据映射
     * @param options  转换选项配置，控制输出内容
     * @return String 格式化的Markdown内容
     */
    private String convertToMarkdown(Document document, Map<String, Object> metadata, ConversionOptions options) {
        if (options.isIncludeMetadata() && !metadata.isEmpty()){
            mb.header(metadata);
        }
        // 处理文档内容
        Element body = document.body();
        if (body != null) {
            processNode(body, options, 0);
        }

        return mb.flush();
    }

    /**
     * @brief 处理节点及其子节点，转换为Markdown格式
     * @details 递归处理HTML节点树，根据节点类型转换为对应的Markdown语法
     *          支持所有常见的HTML元素类型，包括标题、段落、列表、表格等
     * @param node     要处理的HTML节点，不能为null
     * @param options  转换选项配置，用于控制转换行为
     * @param depth    在文档树中的当前深度，用于处理嵌套结构
     */
    private void processNode(Node node, ConversionOptions options, int depth) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (!text.isEmpty()) {
                mb.text(mb.escapeMarkdown(text)).text(" ");
            }
        } else if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName().toLowerCase();

            switch (tagName) {
                case "h1":
                    mb.text("\n").append(mb.h1(mb.escapeMarkdown(element.text())));
                    break;
                case "h2":
                    mb.text("\n").append(mb.h2(mb.escapeMarkdown(element.text())));
                    break;
                case "h3":
                    mb.text("\n").append(mb.h3(mb.escapeMarkdown(element.text())));
                    break;
                case "h4":
                    mb.text("\n").append(mb.h4(mb.escapeMarkdown(element.text())));
                    break;
                case "h5":
                    mb.text("\n").append(mb.h5(mb.escapeMarkdown(element.text())));
                    break;
                case "h6":
                    mb.text("\n").append(mb.h6(mb.escapeMarkdown(element.text())));
                    break;
                case "p":
                    mb.newline();
                    processChildren(element, options, depth + 1);
                    mb.newline(2);
                    break;
                case "br":
                    mb.lineBreak();
                    break;
                case "strong":
                case "b":
                    mb.append("**");
                    processChildren(element, options, depth + 1);
                    mb.append("**");
                    break;
                case "em":
                case "i":
                    mb.append("*");
                    processChildren(element, options, depth + 1);
                    mb.append("*");
                    break;
                case "u":
                    mb.append("<u>");
                    processChildren(element, options, depth + 1);
                    mb.append("</u>");
                    break;
                case "del":
                case "s":
                case "strike":
                    mb.append("~~");
                    processChildren(element, options, depth + 1);
                    mb.append("~~");
                    break;
                case "code":
                    mb.append("`");
                    processChildren(element, options, depth + 1);
                    mb.append("`");
                    break;
                case "pre":
                    String codeText = element.text();
                    mb.append("\n```\n").append((codeText)).append("\n```\n\n");
                    break;
                case "blockquote":
                    String quoteText = element.text();
                    mb.append("\n> ").append(quoteText.replace("\n", "\n> ")).append("\n\n");
                    break;
                case "ul":
                case "ol":
                    processList(element, options, depth + 1);
                    break;
                case "li":
                    processListItem(element, options, depth + 1);
                    break;
                case "a":
                    processLink(element, options);
                    break;
                case "img":
                    processImage(element, options);
                    break;
                case "table":
                    if (options.isIncludeTables()) {
                        processTable(element, options);
                    }
                    break;
                case "hr":
                    mb.text("\n").horizontalRule();
                    break;
                case "div":
                case "section":
                case "article":
                case "main":
                case "header":
                case "footer":
                case "nav":
                case "aside":
                    // 块级元素 - 处理子元素并添加适当的间距
                    mb.newline();
                    processChildren(element, options, depth + 1);
                    mb.newline();
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
                    // 内联元素 - 仅处理子元素
                    processChildren(element, options, depth + 1);
                    break;
                default:
                    // 未知元素 - 尝试处理子元素
                    processChildren(element, options, depth + 1);
                    break;
            }
        }
    }

    /**
     * @brief 处理元素的所有子节点
     * @details 遍历指定元素的所有子节点，递归调用processNode进行处理
     *          保持文档的层次结构和嵌套关系
     * @param element  要处理子节点的父元素，不能为null
     * @param options  转换选项配置，用于控制转换行为
     * @param depth    在文档树中的当前深度
     */
    private void processChildren(Element element, ConversionOptions options, int depth) {
        for (Node child : element.childNodes()) {
            processNode(child, options, depth);
        }
    }

    /**
     * @brief 处理列表元素
     * @details 处理有序列表(ol)和无序列表(ul)，支持多层嵌套结构
     *          根据列表类型应用相应的Markdown列表语法
     * @param list    要处理的列表元素，不能为null
     * @param options  转换选项配置，用于控制转换行为
     * @param depth    在文档树中的当前深度，用于计算缩进级别
     */
    private void processList(Element list, ConversionOptions options, int depth) {
        mb.append("\n");
        for (Element li : list.select("li")) {
            String indent = "  ".repeat(Math.max(0, depth - 1));
            if (list.tagName().equals("ol")) {
                mb.append(indent).append("1. ");
            } else {
                mb.append(indent).append("- ");
            }
            processListItemContent(li, options, depth);
            mb.append("\n");
        }
        mb.append("\n");
    }

    /**
     * @brief 处理列表项
     * @details 处理单个列表项元素，提取其文本内容
     *          简单的列表项处理，复杂的嵌套结构由processListItemContent处理
     * @param li       要处理的列表项元素，不能为null
     * @param options  转换选项配置，用于控制转换行为
     * @param depth    在文档树中的当前深度
     */
    private void processListItem(Element li, ConversionOptions options, int depth) {
        String text = li.text().trim();
        mb.append(text);
    }

    /**
     * @brief 处理列表项的内容，包括嵌套列表
     * @details 处理列表项中的复杂内容，特别是嵌套的列表结构
     *          区分普通内容和嵌套列表，采用不同的处理策略
     * @param li       列表项元素，不能为null
     * @param options  转换选项配置，用于控制转换行为
     * @param depth    在文档树中的当前深度，用于计算嵌套级别
     */
    private void processListItemContent(Element li, ConversionOptions options, int depth) {
        for (Node child : li.childNodes()) {
            if (child instanceof Element) {
                Element childElement = (Element) child;
                if (childElement.tagName().equals("ul") || childElement.tagName().equals("ol")) {
                    // 处理嵌套列表
                    processList(childElement, options, depth + 1);
                } else {
                    processNode(child, options, depth);
                }
            } else {
                processNode(child, options, depth);
            }
        }
    }

    /**
     * @brief 处理链接元素
     * @details 将HTML的<a>标签转换为Markdown链接格式
     *          提取href属性和链接文本，生成标准的Markdown链接语法
     * @param link     要处理的链接元素，不能为null
     * @param options  转换选项配置，用于控制转换行为
     */
    private void processLink(Element link, ConversionOptions options) {
        String href = link.attr("href");
        String text = link.text();

        if (!text.isEmpty()) {
            if (!href.isEmpty()) {
                mb.append("[").append(text).append("](").append(href).append(")");
            } else {
                mb.append(text);
            }
        }
    }

    /**
     * @brief 处理图片元素
     * @details 将HTML的<img>标签转换为Markdown图片格式
     *          提取src和alt属性，生成标准的Markdown图片语法
     *          根据转换选项决定是否包含图片内容
     * @param img      要处理的图片元素，不能为null
     * @param options  转换选项配置，用于控制是否包含图片
     */
    private void processImage(Element img, ConversionOptions options) {
        if (!options.isIncludeImages()) {
            return;
        }

        String src = img.attr("src");
        String alt = img.attr("alt");

        if (!src.isEmpty()) {
            mb.append("![");
            if (!alt.isEmpty()) {
                mb.append(alt);
            }
            mb.append("](").append(src).append(")");
        }
    }

    /**
     * @brief 处理表格元素
     * @details 将HTML的<table>标签转换为Markdown表格格式
     *          处理表格行(tr)、表头(th)和数据单元格(td)
     *          自动添加表头分隔符，确保Markdown表格格式正确
     * @param table    要处理的表格元素，不能为null
     * @param options  转换选项配置，用于控制是否包含表格
     */
    private void processTable(Element table, ConversionOptions options) {
        if (!options.isIncludeTables()) {
            return;
        }

        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return;
        }

        mb.append("\n");

        boolean isFirstRow = true;
        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) {
                continue;
            }

            mb.append("| ");
            for (Element cell : cells) {
                String cellText = cell.text().trim().replace("|", "\\|");
                mb.append(cellText).append(" | ");
            }
            mb.append("\n");

            // 在第一行后添加表头分隔符
            if (isFirstRow) {
                mb.append("|");
                for (int i = 0; i < cells.size(); i++) {
                    mb.append(" --- |");
                }
                mb.append("\n");
                isFirstRow = false;
            }
        }

        mb.append("\n");
    }

}