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
 * @class TextConverter
 * @brief 文本文档转换器，用于将各种文本格式文件转换为Markdown格式
 * @details 支持多种文本格式包括纯文本、Markdown、CSV、JSON、XML、日志文件等
 *          自动检测文件格式并应用相应的转换策略，保持文本的结构和格式
 *          作为通用文本转换器，为其他专用转换器提供补充
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class TextConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(TextConverter.class);

    /**
     * @brief 支持的文件格式集合
     * @details 包含所有此转换器支持的文件扩展名
     */
    private static final Set<String> SUPPORTED_FORMATS = Set.of("txt", "md", "csv", "log", "json", "xml");

    /**
     * @brief 将文本文件转换为Markdown格式
     * @details 主转换方法，自动检测文件格式并应用相应的转换逻辑
     *          根据文件类型选择不同的转换策略，生成标准Markdown文档
     * @param filePath 要转换的文本文件路径，不能为null
     * @param options  转换选项配置，不能为null
     * @return ConversionResult 包含Markdown内容、元数据和警告信息的转换结果
     * @throws ConversionException 当文件读取失败或转换过程中出现错误时抛出
     */
    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("正在转换文本文件: {}", filePath);

        try {
            // 读文件内容
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            // 检测文件格式
            String format = detectFileFormat(filePath);

            // 提取元数据
            Map<String, Object> metadata = extractMetadata(filePath, content, format, options);

            // 转换文件成markdown
            String markdownContent = convertToMarkdown(content, format, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "读取文本文件失败: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    /**
     * @brief 检查是否支持指定的MIME类型
     * @details 判断转换器是否能够处理各种文本格式
     * @param mimeType 要检查的MIME类型，不能为null
     * @return boolean true表示支持该MIME类型，false表示不支持
     */
    @Override
    public boolean supports(String mimeType) {
        return "text/plain".equals(mimeType) ||
               "text/markdown".equals(mimeType) ||
               "text/csv".equals(mimeType) ||
               "application/json".equals(mimeType) ||
               "application/xml".equals(mimeType);
    }

    /**
     * @brief 获取转换器优先级
     * @details 设置较低的优先级值，作为通用转换器在专用转换器之后使用
     * @return int 转换器优先级值，设置为50
     */
    @Override
    public int getPriority() {
        return 50; // 作为备用转换器使用较低优先级
    }

    /**
     * @brief 获取转换器名称
     * @details 返回转换器的唯一标识名称
     * @return String 转换器名称
     */
    @Override
    public String getName() {
        return "TextConverter";
    }

    /**
     * 根据文件扩展名检测文本文件格式
     *
     * @param filePath 文件路径
     * @return 检测到的文件格式
     */
    private String detectFileFormat(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();

        switch (extension) {
            case "md":
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
     * 从文本文件中提取元数据信息
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @param format   检测到的文件格式
     * @param options  转换选项配置
     * @return 包含元数据信息的映射表
     */
    private Map<String, Object> extractMetadata(Path filePath, String content, String format, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // 文件基本信息
            metadata.put("文件名", filePath.getFileName().toString());
            metadata.put("文件大小", filePath.toFile().length());
            metadata.put("文件类型", format);

            // 内容统计信息
            String[] lines = content.split("\\r?\\n");
            metadata.put("行数量", lines.length);
            metadata.put("字符数量", content.length());
            metadata.put("单词数量", countWords(content));

            // 格式特定的元数据信息
            // ToDo: 纯文本格的特殊元数据待补充 ? 感觉也不需要补充
            if ("csv".equals(format)) {
                extractCsvMetadata(content, metadata);
            } else if ("json".equals(format)) {
                extractJsonMetadata(content, metadata);
            } else if ("xml".equals(format)) {
                extractXmlMetadata(content, metadata);
            }

            metadata.put("转换时刻", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * 提取CSV文件特定的元数据信息
     *
     * @param content  CSV文件内容
     * @param metadata 要更新的元数据映射表
     */
    private void extractCsvMetadata(String content, Map<String, Object> metadata) {
        String[] lines = content.split("\\r?\\n");
        if (lines.length > 0) {
            String firstLine = lines[0];
            String[] columns = firstLine.split(",");
            metadata.put("列数量", columns.length);
            metadata.put("行数量", lines.length - 1); // Exclude header
            metadata.put("有头部", true);
        }
    }

    /**
     * @brief 提取JSON文件特定的元数据信息
     * @details 对JSON内容进行简单验证，检查是否为有效的JSON格式
     *          通过检查开始和结束字符来判断JSON结构的有效性
     *
     * @param content  JSON文件内容
     * @param metadata 要更新的元数据映射表
     */
    private void extractJsonMetadata(String content, Map<String, Object> metadata) {
        try {
            // 简单验证 - 检查是否看起来像有效的JSON
            // Todo: JSON格式判断需要更加复杂的逻辑
            String trimmed = content.trim();
            boolean isValidJson = (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                                 (trimmed.startsWith("[") && trimmed.endsWith("]"));
            metadata.put("Json格式是否有效", isValidJson);
        } catch (Exception e) {
            metadata.put("Json格式是否有效", false);
        }
    }

    /**
     * @brief 提取XML文件特定的元数据信息
     * @details 对XML内容进行简单验证，检查是否为有效的XML格式
     *          统计XML标签数量作为内容复杂度的参考指标
     *
     * @param content  XML文件内容
     * @param metadata 要更新的元数据映射表
     */
    private void extractXmlMetadata(String content, Map<String, Object> metadata) {
        try {
            // 简单验证 - 检查是否看起来像有效的XML
            // Todo: XML格式判断需要更复杂的逻辑
            String trimmed = content.trim();
            boolean isValidXml = trimmed.startsWith("<") && trimmed.endsWith(">");
            metadata.put("isValidXml", isValidXml);

            if (isValidXml) {
                // 统计XML标签数量（简单方法）
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
     * @brief 将文本内容转换为Markdown格式
     * @details 根据检测到的文件格式选择相应的转换策略
     *          添加文档标题、元数据信息和格式化的内容部分
     *
     * @param content  原始文本内容
     * @param format   检测到的文件格式
     * @param metadata 文档元数据信息
     * @param options  转换选项配置
     * @return 格式化的Markdown内容字符串
     */
    private String convertToMarkdown(String content, String format, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        // 如果有标题则添加标题
        // Todo: 可以写一个markdown引擎，封装成一个完整对象
        if (options.isIncludeMetadata() && metadata.containsKey("fileName")) {
            String fileName = (String) metadata.get("fileName");
            String title = getFileNameWithoutExtension(fileName);
            markdown.append("# ").append(title).append("\n\n");
        }

        // 如果启用则添加元数据部分
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## 文件信息\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null && !entry.getKey().equals("fileName")) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // 根据格式处理内容
        markdown.append("## 内容\n\n");

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
     * @brief 将CSV内容转换为Markdown表格格式
     * @details 解析CSV数据，第一行作为表头，后续行作为数据行
     *          自动添加表格分隔符，生成标准的Markdown表格语法
     *
     * @param csvContent CSV格式的内容字符串
     * @return Markdown表格格式的字符串
     */
    private String convertCsvToMarkdown(String csvContent) {
        String[] lines = csvContent.split("\\r?\\n");
        if (lines.length == 0) {
            return "*空CSV文件*\n\n";
        }

        StringBuilder markdown = new StringBuilder();

        // 处理表头行
        String[] headers = lines[0].split(",");
        markdown.append("| ");
        for (String header : headers) {
            markdown.append(header.trim()).append(" | ");
        }
        markdown.append("\n");

        // 添加分隔符
        markdown.append("| ");
        for (int i = 0; i < headers.length; i++) {
            markdown.append(" --- | ");
        }
        markdown.append("\n");

        // 处理数据行
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
     * @brief 将JSON内容转换为Markdown代码块
     * @details 使用代码块语法包装JSON内容，保持原始格式
     *          添加json语言标识符以便语法高亮显示
     *
     * @param jsonContent JSON格式的内容字符串
     * @return 包含JSON代码块的Markdown字符串
     */
    private String convertJsonToMarkdown(String jsonContent) {
        return "```json\n" + jsonContent + "\n```\n\n";
    }

    /**
     * @brief 将XML内容转换为Markdown代码块
     * @details 使用代码块语法包装XML内容，保持原始格式
     *          添加xml语言标识符以便语法高亮显示
     *
     * @param xmlContent XML格式的内容字符串
     * @return 包含XML代码块的Markdown字符串
     */
    private String convertXmlToMarkdown(String xmlContent) {
        return "```xml\n" + xmlContent + "\n```\n\n";
    }

    /**
     * @brief 将日志内容转换为格式化的Markdown
     * @details 解析日志级别，根据不同级别应用不同的格式样式
     *          错误信息加粗显示，警告信息显示，普通信息斜体显示
     *
     * @param logContent 日志格式的内容字符串
     * @return 格式化的Markdown字符串
     */
    private String convertLogToMarkdown(String logContent) {
        String[] lines = logContent.split("\\r?\\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // 检测日志级别并相应格式化
                if (trimmed.toUpperCase().contains("ERROR")) {
                    markdown.append("**错误:** ").append(trimmed).append("\n");
                } else if (trimmed.toUpperCase().contains("WARN")) {
                    markdown.append("**警告:** ").append(trimmed).append("\n");
                } else if (trimmed.toUpperCase().contains("INFO")) {
                    markdown.append("*信息:* ").append(trimmed).append("\n");
                } else {
                    markdown.append(trimmed).append("\n");
                }
            }
        }

        markdown.append("\n");
        return markdown.toString();
    }

    /**
     * @brief 将纯文本内容转换为Markdown格式
     * @details 智能识别文本结构，保持原有标题、列表、代码块的格式
     *          空行保持不变，普通行转换为段落格式
     *
     * @param textContent 纯文本内容字符串
     * @return 格式化的Markdown字符串
     */
    private String convertPlainTextToMarkdown(String textContent) {
        String[] lines = textContent.split("\\r?\\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                markdown.append("\n");
            } else if (trimmed.startsWith("#")) {
                // 保持现有标题格式
                markdown.append(trimmed).append("\n\n");
            } else if (trimmed.startsWith(" ") || trimmed.startsWith("\t")) {
                // 代码块或缩进文本
                markdown.append("    ").append(trimmed).append("\n");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                // 列表项
                markdown.append(trimmed).append("\n");
            } else {
                // 普通段落
                markdown.append(trimmed).append("\n\n");
            }
        }

        return markdown.toString();
    }

    /**
     * @brief 统计内容中的单词数量
     * @details 使用空格符分割文本，计算非空单词的总数
     *          处理null值和空字符串的边界情况
     *
     * @param content 要分析的文本内容
     * @return 单词数量统计结果
     */
    private int countWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }

    /**
     * @brief 从文件名中获取文件扩展名
     * @details 查找最后一个点的位置，提取其后的字符串作为扩展名
     *          不包含点本身，处理无扩展名的情况
     *
     * @param fileName 完整的文件名字符串
     * @return 文件扩展名（不包含点），无扩展名时返回空字符串
     */
    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "文件名不能为空");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * @brief 获取不带扩展名的文件名
     * @details 查找最后一个点的位置，返回其前的部分作为文件名
     *          保留主文件名部分，移除扩展名和点
     *
     * @param fileName 完整的文件名字符串
     * @return 不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String fileName) {
        requireNonNull(fileName, "文件名不能为空");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    /**
     * @brief 格式化元数据键名以供显示
     * @details 将驼峰命名转换为标题格式，首字母大写
     *          在大写字母前插入空格，提高可读性
     *
     * @param key 元数据键名字符串
     * @return 格式化后的键名
     */
    private String formatMetadataKey(String key) {
        // 将驼峰命名转换为标题格式
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }

    /**
     * @brief 检查文件格式是否被此转换器支持
     * @details 在支持格式集合中查找指定的文件扩展名
     *          不区分大小写进行比较，提高兼容性
     *
     * @param fileExtension 要检查的文件扩展名
     * @return 如果支持该格式返回true，否则返回false
     */
    public static boolean isSupportedFormat(String fileExtension) {
        return SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    /**
     * @brief 获取所有支持的文本格式
     * @details 返回支持的文件扩展名集合的副本
     *          防止外部修改原始集合数据
     *
     * @return 支持的文件扩展名集合
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}