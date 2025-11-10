package com.markitdown.converter;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author duan yan
 * @version 2.0.0
 * @class DocxConverter
 * @brief Word文档转换器，用于将DOCX文件转换为Markdown格式
 * @details 使用Apache POI库解析DOCX文件，提取文本、格式和结构信息
 * 支持段落样式、表格、列表、字体格式等元素的转换
 * 保持文档的层次结构和重要格式信息
 * @since 2.0.0
 */
// Todo: 基础转换没问题, 图片遗漏了，还有文字与其他位置的相对位置
public class DocxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocxConverter.class);

    private MarkdownBuilder mb;

    /**
     * @param filePath 要转换的DOCX文件路径，不能为null
     * @param options  转换选项配置，不能为null
     * @return ConversionResult 包含Markdown内容、元数据和警告信息的转换结果
     * @throws ConversionException 当文件读取失败或转换过程中出现错误时抛出
     * @brief 将DOCX文件转换为Markdown格式
     * @details 主转换方法，解析Word文档并提取元数据和内容，转换为标准Markdown格式
     * 支持文档结构、表格、格式化文本等完整内容的转换
     */
    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("开始转换 DOCX 文件: {}", filePath);
        mb = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {

            // 提取元数据

            Map<String, Object> metadata = extractMetadata(document, options);

            if (options.isIncludeMetadata()) {
                // 文件基本信息
                metadata.put("文件名", filePath.getFileName().toString());
                metadata.put("文件大小", filePath.toFile().length());
            }

            // 将文档转换为Markdown
            String markdownContent = convertToMarkdown(document, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "处理DOCX文件失败: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    /**
     * @param mimeType 要检查的MIME类型，不能为null
     * @return boolean true表示支持该MIME类型，false表示不支持
     * @brief 检查是否支持指定的MIME类型
     * @details 判断转换器是否能够处理Word文档格式
     */
    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType) ||
                "application/msword".equals(mimeType);
    }

    /**
     * @return int 转换器优先级值，设置为100
     * @brief 获取转换器优先级
     * @details 设置较高的优先级值，确保在多个转换器支持同一类型时优先选择此转换器
     */
    @Override
    public int getPriority() {
        return 100;
    }

    /**
     * @return String 转换器名称
     * @brief 获取转换器名称
     * @details 返回转换器的唯一标识名称
     */
    @Override
    public String getName() {
        return "DocxConverter";
    }

    // ==================== 私有辅助方法 ====================

    /**
     * @param document Word文档对象，不能为null
     * @param options  转换选项，用于控制是否包含元数据
     * @return Map<String, Object> 包含元数据的映射
     * @brief 从Word文档中提取元数据
     * @details 提取文档的统计信息和转换时间等元数据
     * 当前使用简化的元数据提取，主要关注文档统计信息
     */
    private Map<String, Object> extractMetadata(XWPFDocument document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // 简化的元数据提取 - POI CoreProperties API可能有所不同
            // 文档统计信息更可靠

            // 文档统计信息
            metadata.put("段落数量", document.getParagraphs().size());
            metadata.put("表格数量", document.getTables().size());
            metadata.put("转换时刻", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * @param document Word文档对象，不能为null
     * @param metadata 文档元数据映射
     * @param options  转换选项配置，不能为null
     * @return String 格式化的Markdown内容
     * @brief 将Word文档转换为Markdown格式
     * @details 生成完整的Markdown文档结构，包括标题、元数据信息和主要内容
     * 根据转换选项控制是否包含元数据部分
     */
    private String convertToMarkdown(XWPFDocument document, Map<String, Object> metadata, ConversionOptions options) {
        // 如果有标题则添加标题
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            mb.header(metadata);
        }
        // 处理文档内容
        processDocumentBody(document, options);

        return mb.flush().toString();
    }

    /**
     * @param document Word文档对象，不能为null
     * @param options  转换选项配置，不能为null
     * @brief 处理文档的主体内容
     * @details 遍历文档中的所有段落和表格，将它们转换为Markdown格式
     * 根据转换选项控制是否包含表格内容
     */
    // 使用getParagraph 获取文档主要内容
    private void processDocumentBody(XWPFDocument document, ConversionOptions options) {
        mb.append(mb.heading("内容", 2));
        // 处理段落
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            processParagraph(document, paragraph, options);
        }

        // 处理表格
        if (options.isIncludeTables()) {
            for (XWPFTable table : document.getTables()) {
                processTable(table, options);
            }
        }
    }

    private String getStyle(XWPFDocument document, XWPFParagraph paragraph) {
        XWPFStyles styles = document.getStyles();
        XWPFStyle style = null;
        if (styles != null) {
            // 通过样式ID获取样式对象
            style = styles.getStyle(paragraph.getStyleID());
        }
        return style == null ? "" : style.getName();
    }

    /**
     * @param paragraph 要处理的段落对象，不能为null
     * @param options   转换选项配置，不能为null
     * @brief 处理单个段落并转换为Markdown格式
     * @details 根据段落样式识别标题、列表项和普通段落，并应用相应的Markdown格式
     * 支持多层标题、列表缩进和格式化文本的处理
     */

    private void processParagraph(XWPFDocument document, XWPFParagraph paragraph, ConversionOptions options) {
        String text = paragraph.getText();
        if (text == null || text.trim().isEmpty()) {
            mb.newline();
            return;
        }
        //根据样式处理标题
        String style = getStyle(document, paragraph);
        if (style != null) {
            switch (style) {
                case "Title":
                    mb.append(mb.heading(mb.escapeMarkdown(text), 1));
                    mb.horizontalRule();
                    break;
                case "heading 1":
                    mb.append(mb.heading(mb.escapeMarkdown(text), 2));
                    break;
                case "heading 2":
                    mb.append(mb.heading(mb.escapeMarkdown(text), 3));
                    break;
                case "heading 3":
                    mb.append(mb.heading(mb.escapeMarkdown(text), 4));
                    break;
                case "heading 4":
                    mb.append(mb.heading(mb.escapeMarkdown(text), 5));
                    break;
                case "heading 5":
                    mb.append(mb.heading(mb.escapeMarkdown(text), 6));
                    break;
                case "List Bullet":
                    mb.append(mb.unorder_item(mb.escapeMarkdown(text)));
                    break;
                case "List Number":
                    // Todo: 如何获取文字
                    break;
            }

            // 处理列表项, 感觉缩进没必要搞
//            if (isListItem(paragraph)) {
//                String indent = getIndent(paragraph);
//                markdown.append(indent).append("- ").append(text.trim()).append("\n");
//                return;
//            }

            // 处理带格式化的普通段落
            mb.append(processParagraphFormatting(paragraph, text));
            mb.newline(2);
        }
    }

        /**
         * @param paragraph 要处理的段落对象，不能为null
         * @param text      段落的原始文本内容
         * @return String 格式化后的文本内容
         * @brief 处理段落内的格式化
         * @details 处理段落中的文本格式，包括粗体、斜体、删除线等Markdown格式
         * 遍历段落中的所有文本运行(run)，应用相应的格式标记
         */
        private StringBuilder processParagraphFormatting (XWPFParagraph paragraph, String text){
            StringBuilder formatted = new StringBuilder();

            for (XWPFRun run : paragraph.getRuns()) {
                String runText = run.getText(0);
                if (runText == null || runText.isEmpty()) {
                    continue;
                }

                // 应用格式化
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

            return formatted;
        }

        /**
         * @param table   要处理的表格对象，不能为null
         * @param options 转换选项配置，用于控制是否包含表格
         * @brief 处理表格并转换为Markdown格式
         * @details 将Word表格转换为标准的Markdown表格格式
         * 处理表格行和单元格，添加表头分隔符，确保表格格式正确
         */
        private void processTable (XWPFTable table, ConversionOptions options){
            if (!options.isIncludeTables()) {
                return;
            }

            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) {
                return;
            }
            mb.newline();
            List<String> header_list = new ArrayList<>();
            XWPFTableRow header_row = rows.get(0);
            List<XWPFTableCell> header_cells = header_row.getTableCells();
            for (XWPFTableCell cell : header_cells) {
                header_list.add(cell.getText().replace("\n", " ").trim());
            }
            List<List<String>> cells_list = new ArrayList<>();
            // 处理每一行
            for (int i = 1; i < rows.size(); i++) {
                XWPFTableRow row = rows.get(i);
                List<XWPFTableCell> cells = row.getTableCells();

                if (cells.isEmpty()) {
                    continue;
                }
                List<String> cell_list = new ArrayList<>();
                cells_list.add(cell_list);
                for (XWPFTableCell cell : cells) {
                    cell_list.add(cell.getText().replace("\n", " ").trim());
                }
            }
            mb.append(mb.table(header_list.toArray(new String[0]), cells_list.stream().map(list -> list.toArray(new String[0])).toArray(String[][]::new)));
            mb.newline();
        }



        /**
         * @param paragraph 要处理的段落对象，不能为null
         * @return String 缩进空格字符串，每级缩进两个空格
         * @brief 获取列表项的缩进
         * @details 根据段落的左缩进值计算列表项的缩进级别
         * 使用twips到空格的近似转换，最多支持5级缩进
         */
        private String getIndent (XWPFParagraph paragraph){
            int indentLevel = (int) (paragraph.getIndentationLeft() / 360); // twips到空格的近似转换
            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < Math.max(0, Math.min(indentLevel, 5)); i++) {
                indent.append("  ");
            }
            return indent.toString();
        }

        /**
         * @param key 要格式化的元数据键名，不能为null
         * @return String 格式化后的键名
         * @brief 格式化元数据键名用于显示
         * @details 将驼峰命名的键名转换为标题格式的大写形式
         * 提供更好的元数据显示效果
         */
        private String formatMetadataKey (String key){
            // 将驼峰命名转换为标题格式
            return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                    .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                    .toLowerCase();
        }
    }