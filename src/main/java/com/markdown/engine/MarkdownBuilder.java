package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @class MarkdownBuilder
 * @brief Markdown文档构建器，用于Converter集成
 * @details 提供流畅API创建结构化markdown内容，支持所有常用元素
 *          包括标题、段落、列表、表格、代码块、链接、图片等
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class MarkdownBuilder {

    private final StringBuilder content;
    private static  RenderContext context;

      /**
     * @brief 默认构造函数 - 使用默认配置创建构建器
     * @details 使用MarkdownConfig默认配置创建构建器实例
     */
    public MarkdownBuilder() {
        this(MarkdownConfig.builder().build());
    }

    /**
     * @brief 配置构造函数 - 使用指定配置创建构建器
     * @param config 渲染配置，不能为null
     */
    public MarkdownBuilder(MarkdownConfig config) {
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }

    /**
     * @brief 上下文构造函数 - 使用现有渲染上下文创建构建器
     * @param context 渲染上下文，不能为null
     */
    public MarkdownBuilder(RenderContext context) {
        this.context = context;
        this.content = new StringBuilder();
    }

    /**
     * @brief 快速配置构造函数 - 为Converter集成提供便捷配置选项
     * @param includeTables  是否支持表格
     * @param escapeHtml     是否转义HTML字符
     * @param wrapCodeBlocks 是否使用```包装代码块
     */
    public MarkdownBuilder(boolean includeTables, boolean escapeHtml, boolean wrapCodeBlocks) {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(includeTables)
                .escapeHtml(escapeHtml)
                .wrapCodeBlocks(wrapCodeBlocks)
                .build();
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }

    /**
     * @brief 直接添加原始文本
     * @param text
     * @return
     */
    public MarkdownBuilder append(String text) {
        content.append(text);
        return this;
    }

    public MarkdownBuilder append(StringBuilder text) {
        content.append(text);
        return this;
    }

    // ==================== 标题方法 ====================

    /**
     * @brief 添加标题
     * @details 添加指定级别的标题，支持ATX和setext两种风格
     * @param text  标题文本，不能为null或空字符串
     * @param level 标题级别(1-6)
     * @return MarkdownBuilder 构建器实例，支持链式调用
     */
    public static StringBuilder heading(String text, int level) {
        StringBuilder ans = new StringBuilder();
        if (text == null || text.trim().isEmpty()) {
            return ans;
        }

        int safeLevel = Math.max(1, Math.min(6, level));
        String headingStyle = context.getHeadingStyle();

        if ("setext".equals(headingStyle) && safeLevel <= 2) {
            // 使用setext风格标题（下划线）
            ans.append(text.trim()).append(System.lineSeparator());
            if (safeLevel == 1) {
                ans.append("=".repeat(text.trim().length()));
            } else {
                ans.append("-".repeat(text.trim().length()));
            }
        } else {
            // 使用ATX风格标题（带#）
            ans.append("#".repeat(safeLevel))
                   .append(" ")
                   .append(text.trim());
        }

        ans.append(System.lineSeparator())
               .append(System.lineSeparator());
        return ans;
    }

    /**
     * @brief 添加1级标题
     * @param text 标题文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder h1(String text) {
        return heading(text, 1);
    }

    /**
     * @brief 添加2级标题
     * @param text 标题文本
     * @return StringBuilder 构建器实例
     */
    public StringBuilder h2(String text) {
        return heading(text, 2);
    }

    /**
     * @brief 添加3级标题
     * @param text 标题文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder h3(String text) {
        return heading(text, 3);
    }

    // ==================== 文本方法 ====================

    /**
     * @brief 添加段落
     * @details 添加段落到文档中，自动进行特殊字符转义
     * @param text 段落文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder paragraph(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null && !text.trim().isEmpty()) {
            ans.append(text.trim())
                   .append(System.lineSeparator())
                   .append(System.lineSeparator());
        }
        return ans;
    }

    /**
     * @brief 添加纯文本
     * @details 与paragraph()方法功能相同，提供别名方便使用
     * @param text 文本内容
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder text(String text) {
        content.append(paragraph(text));
        return this;
    }


    /**
     * @brief 添加粗体文本
     * @details 使用**包裹文本实现粗体效果
     * @param text 需要加粗的文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder bold(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("**").append(text).append("**");
        }
        return ans;
    }

    /**
     * @brief 添加斜体文本
     * @details 使用*包裹文本实现斜体效果
     * @param text 需要斜体的文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder italic(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("*").append(text).append("*");
        }
        return ans;
    }

    /**
     * @brief 添加删除线文本
     * @details 使用~~包裹文本实现删除线效果
     * @param text 需要添加删除线的文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder strikethrough(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("~~").append(text).append("~~");
        }
        return ans;
    }

    /**
     * @brief 添加行内代码
     * @details 使用`包裹文本实现行内代码效果
     * @param text 代码内容
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder inlineCode(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("`").append(text).append("`");
        }
        return ans;
    }

    // ==================== 代码块方法 ====================

    /**
     * @brief 添加代码块
     * @details 添加带语言标识的围栏代码块，支持语法高亮
     * @param code     代码内容
     * @param language 编程语言标识，可选
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder codeBlock(String code, String language) {
        StringBuilder ans = new StringBuilder();
        if (code != null) {
            if (context.shouldWrapCodeBlocks()) {
                ans.append("```");
                if (language != null && !language.trim().isEmpty()) {
                    ans.append(language.trim());
                }
                ans.append(System.lineSeparator());
            }
            ans.append(code);
            if (context.shouldWrapCodeBlocks()) {
                ans.append(System.lineSeparator()).append("```");
            }
            ans.append(System.lineSeparator()).append(System.lineSeparator());
        }
        return ans;
    }

    /**
     * @brief 添加无语言标识的代码块
     * @param code 代码内容
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder codeBlock(String code) {
        return codeBlock(code, null);
    }

    // ==================== 列表方法 ====================

    /**
     * @brief 添加无序列表
     * @details 添加使用项目符号的无序列表，支持多行文本
     * @param items 列表项数组
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder unorderedList(StringBuilder... items) {
        return unorderedList(0, items);
    }

    /**
     * @brief 添加带缩进的无序列表
     * @details 添加指定缩进级别的无序列表，支持嵌套列表
     * @param level 列表缩进级别(0-起始)
     * @param items 列表项数组
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder unorderedList(int level, StringBuilder... items) {
        StringBuilder ans = new StringBuilder();
        if (items != null) {
            String marker = getListMarker("unordered");
            String indent = "  ".repeat(level);

            for (StringBuilder item : items) {
                if (item != null && !item.isEmpty()) {
                    ans.append(indent)
                           .append(marker)
                           .append(" ")
                           .append(item)
                           .append(System.lineSeparator());
                }
            }
            ans.append(System.lineSeparator());
        }
        return ans;
    }

    /**
     * @brief 添加有序列表
     * @details 添加从1开始编号的有序列表
     * @param items 列表项数组
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder orderedList(String[] items) {
        return orderedList(1, items);
    }

    /**
     * @brief 添加带起始编号的有序列表
     * @details 添加指定起始编号的有序列表
     * @param items      列表项数组
     * @param startNumber 起始编号
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder orderedList(int startNumber, String[] items) {
        return orderedList(0, startNumber, items);
    }

    /**
     * @brief 添加带缩进和起始编号的有序列表
     * @details 添加指定缩进级别和起始编号的有序列表，支持嵌套列表
     * @param level       列表缩进级别(0-起始)
     * @param startNumber 起始编号
     * @param items       列表项数组
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder orderedList(int level, int startNumber, String[] items) {
        StringBuilder ans = new StringBuilder();
        if (items != null) {
            String indent = "  ".repeat(level);

            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (item != null && !item.trim().isEmpty()) {
                    ans.append(indent)
                           .append((startNumber + i) + ". ")
                           .append(item.trim())
                           .append(System.lineSeparator());
                }
            }
            ans.append(System.lineSeparator());
        }
        return ans;
    }

    // ==================== 表格方法 ====================

    /**
     * @brief 添加表格
     * @details 添加GitHub风格的表格，支持表头和数据行
     * @param headers 表格标题数组
     * @param rows    表格数据二维数组，每个子数组代表一行
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder table(String[] headers, String[][] rows) {
        StringBuilder ans = new StringBuilder();
        if (!context.shouldIncludeTables() || headers == null || headers.length == 0) {
            return ans;
        }

        // 表格标题行
        ans.append("| ");
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) ans.append(" | ");
            ans.append(headers[i] != null ? headers[i].trim() : "");
        }
        ans.append(" |").append(System.lineSeparator());

        // 表格分隔线
        ans.append("|");
        for (int i = 0; i < headers.length; i++) {
            ans.append("-----|");
        }
        ans.append(System.lineSeparator());

        // 表格数据行
        if (rows != null) {
            for (String[] row : rows) {
                ans.append("| ");
                for (int i = 0; i < headers.length; i++) {
                    if (i > 0) ans.append(" | ");
                    String cell = (row != null && i < row.length) ? row[i] : "";
                    ans.append(cell != null ? cell.trim() : "");
                }
                ans.append(" |").append(System.lineSeparator());
            }
        }

        ans.append(System.lineSeparator());
        return ans;
    }

    // ==================== 其他元素方法 ====================

    /**
     * @brief 添加引用块
     * @details 添加使用>标记的引用块，支持多行文本
     * @param text 被引用的文本
     * @return MarkdownBuilder 构建器实例
     */
    public StringBuilder blockquote(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                ans.append("> ").append(line).append(System.lineSeparator());
            }
            ans.append(System.lineSeparator());
        }
        return ans;
    }

    /**
     * @brief 添加水平分割线
     * @details 添加---格式的水平分割线，用于内容分隔
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder horizontalRule() {
        content.append("---")
               .append(System.lineSeparator())
               .append(System.lineSeparator());
        return this;
    }

    /**
     * @brief 添加链接
     * @details 添加[text](url)格式的链接，链接文本会被转义
     * @param text 链接显示文本
     * @param url  链接URL，不会被转义
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder link(String text, String url) {
        if (text != null && url != null) {
            content.append("[").append(escapeMarkdown(text)).append("](")
                   .append(url).append(")"); // URL should not be escaped
        }
        return this;
    }

    /**
     * @brief 添加图片
     * @details 添加![alt](url)格式的图片，支持标题属性
     * @param altText 图片的替代文本
     * @param url     图片URL，不会被转义
     * @param title   图片标题(可选)
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder image(String altText, String url, String title) {
        if (url != null) {
            content.append("![")
                   .append(altText != null ? escapeMarkdown(altText) : "")
                   .append("](")
                   .append(url); // URL should not be escaped

            if (title != null && !title.trim().isEmpty()) {
                content.append(" \"").append(escapeMarkdown(title.trim())).append("\"");
            }
            content.append(")");
        }
        return this;
    }

    /**
     * @brief 添加换行符
     * @details 添加两个空格实现的Markdown换行符
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder lineBreak() {
        content.append("  ").append(System.lineSeparator());
        return this;
    }

    /**
     * @brief 添加换行符
     * @details 添加系统换行符
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder newline() {
        content.append(System.lineSeparator());
        return this;
    }

    /**
     * @brief 添加多个换行符
     * @details 添加指定数量的系统换行符
     * @param count 换行符数量
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder newline(int count) {
        for (int i = 0; i < count; i++) {
            content.append(System.lineSeparator());
        }
        return this;
    }

    /**
     * @brief 添加原始文本
     * @details 添加不进行任何转义的原始文本
     * @param text 原始文本内容
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder raw(String text) {
        if (text != null) {
            content.append(text);
        }
        return this;
    }


    // ==================== 工具方法 ====================

    /**
     * @brief 构建最终内容
     * @details 将当前构建器中的所有内容合并成完整的Markdown字符串
     * @return String 完整的markdown内容
     */
    public String build() {
        return content.toString();
    }

    /**
     * @brief 清空构建器
     * @details 清空当前构建器内容，但保留上下文中的输出内容
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder clear() {
        content.setLength(0);
        return this;
    }

    /**
     * @brief 输出到上下文并清空构建器
     * @details 将当前内容输出到渲染上下文并清空构建器，用于内存管理
     * @return String 输出的内容字符串
     */
    public String flush() {
        String flushedContent = content.toString();
        context.getOutput().append(flushedContent);
        content.setLength(0);
        return flushedContent;
    }

    /**
     * @brief 获取累积内容
     * @details 获取所有已输出到上下文的内容，包括当前构建器内容
     * @return String 所有累积的完整内容
     */
    public String getAccumulatedContent() {
        String currentContent = content.toString();
        String contextContent = context.getOutput().toString();
        return contextContent + currentContent;
    }

    /**
     * @brief 获取已输出内容
     * @details 获取仅已输出到上下文的内容，不包括当前构建器内容
     * @return String 已输出的内容
     */
    public String getFlushedContent() {
        return context.getOutput().toString();
    }

    /**
     * @brief 重置所有内容
     * @details 清空构建器和上下文的所有内容
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder resetAll() {
        content.setLength(0);
        context.getOutput().setLength(0);
        context.reset();
        return this;
    }

    /**
     * @brief 获取当前内容长度
     * @return int 内容字符数
     */
    public int length() {
        return content.length();
    }

    /**
     * @brief 获取渲染上下文
     * @return RenderContext 渲染上下文实例
     */
    public RenderContext getContext() {
        return context;
    }

    // ==================== 文档结构方法 ====================

    /**
     * @brief 创建完整文档
     * @details 创建包含标题、元数据和内容的完整文档结构
     * @param title    文档标题
     * @param metadata 文档元数据
     * @param content  文档内容
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder document(String title, Map<String, Object> metadata, String content) {
        if (title != null && !title.trim().isEmpty()) {
            heading(title, 1);
        }

        if (metadata != null && !metadata.isEmpty()) {
            heading("Document Information", 2);
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    String key = formatMetadataKey(entry.getKey());
                    String value = formatMetadataValue(entry.getValue());
                    text("- **").text(key).text(":** ").text(value).newline();
                }
            }
            newline();
        }

        if (content != null && !content.trim().isEmpty()) {
            heading("Content", 2);
            raw(content).newline();
        }

        return this;
    }

    /**
     * @brief 添加文档头部
     * @details 添加文档标题和元数据信息
     * @param title    文档标题
     * @param metadata 文档元数据
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder header(String title, Map<String, Object> metadata) {
        if (title != null && !title.trim().isEmpty()) {
            heading(title, 1);
        }

        if (metadata != null && !metadata.isEmpty()) {
            heading("Document Information", 2);
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    String key = formatMetadataKey(entry.getKey());
                    String value = formatMetadataValue(entry.getValue());
                    text("- **").text(key).text(":** ").text(value).newline();
                }
            }
            newline();
        }

        return this;
    }

    /**
     * @brief 从集合添加列表
     * @details 将集合转换为Markdown列表并添加到构建器
     * @param items 集合中的项目
     * @return MarkdownBuilder 构建器实例
     */
//    public MarkdownBuilder listFromCollection(Collection<String> items) {
//        if (items != null && !items.isEmpty()) {
//            unorderedList(items.toArray(new String[0]));
//        }
//        return this;
//    }

    /**
     * @brief 从映射添加表格
     * @details 将键值对映射转换为两列表格并添加到构建器
     * @param data 键值对映射数据
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder tableFromMap(Map<String, String> data) {
        if (data != null && !data.isEmpty()) {
            String[] headers = {"Key", "Value"};
            String[][] rows = new String[data.size()][2];

            int i = 0;
            for (Map.Entry<String, String> entry : data.entrySet()) {
                rows[i][0] = entry.getKey() != null ? entry.getKey() : "";
                rows[i][1] = entry.getValue() != null ? entry.getValue() : "";
                i++;
            }

            table(headers, rows);
        }
        return this;
    }

    /**
     * @brief 从映射列表添加表格
     * @details 将映射列表转换为表格并添加到构建器
     * @param headers 表格标题数组
     * @param rows    行数据列表，每个映射代表一行
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder tableFromList(String[] headers, List<Map<String, String>> rows) {
        if (headers != null && headers.length > 0 && rows != null && !rows.isEmpty()) {
            String[][] tableData = new String[rows.size()][headers.length];
            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                for (int j = 0; j < headers.length; j++) {
                    tableData[i][j] = row != null ? row.getOrDefault(headers[j], "") : "";
                }
            }

            table(headers, tableData);
        }
        return this;
    }

    /**
     * @brief 添加多个段落
     * @details 从数组添加多个段落到构建器
     * @param paragraphs 段落数组
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder paragraphs(String... paragraphs) {
        if (paragraphs != null) {
            for (String paragraph : paragraphs) {
                if (paragraph != null && !paragraph.trim().isEmpty()) {
                    paragraph(paragraph);
                }
            }
        }
        return this;
    }

    /**
     * @brief 添加转义文本
     * @details 添加经过Markdown特殊字符转义的文本
     * @param text 需要转义的文本
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder escaped(String text) {
        if (text != null) {
            raw(escapeMarkdown(text));
        }
        return this;
    }

    /**
     * @brief 添加安全文本
     * @details 与escaped()方法功能相同，提供更直观的命名
     * @param text 需要转义的文本
     * @return MarkdownBuilder 构建器实例
     */
    public MarkdownBuilder safeText(String text) {
        return escaped(text);
    }

    /**
     * @brief 验证当前内容
     * @details 验证当前构建器内容是否包含有效的Markdown语法
     * @return boolean 是否有效
     */
    public boolean isValidContent() {
        return isValidMarkdown(build());
    }

    /**
     * @brief 静态验证Markdown语法
     * @details 验证指定字符串是否包含有效的Markdown语法
     * @param markdown 要验证的Markdown字符串
     * @return boolean 是否有效
     */
    // Todo: markdown语法判别有问题，之后修改
    public static boolean isValidMarkdown(String markdown) {
        if (markdown == null) {
            return false;
        }

        // Basic validation checks
        // Check for balanced brackets and parentheses
        int openBrackets = markdown.length() - markdown.replace("[", "").length();
        int closeBrackets = markdown.length() - markdown.replace("]", "").length();
        if (openBrackets != closeBrackets) {
            return false;
        }

        int openParens = markdown.length() - markdown.replace("(", "").length();
        int closeParens = markdown.length() - markdown.replace(")", "").length();
        if (openParens != closeParens) {
            return false;
        }

        // Check for malformed link syntax
        if (markdown.contains("[](")) {
            return false;
        }

        // Check for empty link text
        if (markdown.matches(".*\\[\\s*\\]\\([^)]*\\).*")) {
            return false;
        }

        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * @brief 格式化元数据键名
     * @details 将驼峰命名转换为可读格式
     * @param key 元数据键名
     * @return String 格式化后的键名
     */
    private String formatMetadataKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }

    /**
     * @brief 格式化元数据值
     * @details 将不同类型的对象转换为字符串表示
     * @param value 元数据值
     * @return String 格式化后的字符串
     */
    private String formatMetadataValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format((Date) value);
        }

        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            return "[" + String.join(", ", collection.stream().map(Object::toString).toArray(String[]::new)) + "]";
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return String.valueOf(map.size()) + " items";
        }

        return value.toString();
    }

    /**
     * @brief 获取列表标记
     * @details 根据配置返回列表标记符号
     * @param listType 列表类型
     * @return String 列表标记符号
     */
    private String getListMarker(String listType) {
        String style = context.getListStyle();
        if ("unordered".equals(listType)) {
            switch (style) {
                case "asterisk": return "*";
                case "plus": return "+";
                default: return "-";
            }
        }
        return "-";
    }

    /**
     * @brief 转义Markdown特殊字符
     * @details 转义Markdown中的特殊字符，避免语法错误
     * @param text 要转义的文本
     * @return String 转义后的文本
     */
    public String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }

        if (context.shouldEscapeHtml()) {
            text = text.replace("<", "&lt;").replace(">", "&gt;");
        }

        return text.replace("\\", "\\\\")
                  .replace("*", "\\*")
                  .replace("_", "\\_")
                  .replace("`", "\\`")
                  .replace("[", "\\[")
                  .replace("]", "\\]")
                  .replace("(", "\\(")
                  .replace(")", "\\)")
                  .replace("#", "\\#")
                  .replace("+", "\\+")
                  .replace("-", "\\-")
                  .replace(".", "\\.")
                  .replace("!", "\\!");
    }

    /**
     * @brief 转义行内代码特殊字符
     * @details 为行内代码转义反引号和反斜杠
     * @param text 要转义的代码文本
     * @return String 转义后的文本
     */
    private String escapeCodeInline(String text) {
        if (text == null) {
            return "";
        }
        // For inline code, only escape backticks and backslashes
        return text.replace("`", "\\`").replace("\\", "\\\\");
    }
}