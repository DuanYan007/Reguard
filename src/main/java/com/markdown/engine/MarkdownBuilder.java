package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author duan yan
 * @version 2.0.0
 * @class MarkdownBuilder
 * @brief Markdown文档构建器，用于Converter集成
 * @details 提供流畅API创建结构化markdown内容，支持所有常用元素
 * 包括标题、段落、列表、表格、代码块、链接、图片等
 * @since 2.0.0
 */
public class MarkdownBuilder {

    private final StringBuilder content;
    private static RenderContext context;

    //构造函数
    public MarkdownBuilder() {
        this(MarkdownConfig.builder().build());
    }

    public MarkdownBuilder(MarkdownConfig config) {
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }

    public MarkdownBuilder(RenderContext context) {
        this.context = context;
        this.content = new StringBuilder();
    }

    public MarkdownBuilder(boolean includeTables, boolean escapeHtml, boolean wrapCodeBlocks) {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(includeTables)
                .escapeHtml(escapeHtml)
                .wrapCodeBlocks(wrapCodeBlocks)
                .build();
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }
    // markdownd语法拼接
    public MarkdownBuilder append(String text) {
        content.append(text);
        return this;
    }

    public MarkdownBuilder append(StringBuilder text) {
        content.append(text);
        return this;
    }
    // 标题模版
    public StringBuilder heading(String text, int level) {
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
    //各类标题
    public StringBuilder h1(String text) {
        return heading(text, 1);
    }
    public StringBuilder h2(String text) {
        return heading(text, 2);
    }
    public StringBuilder h3(String text) {
        return heading(text, 3);
    }
    public StringBuilder h4(String text) {
        return heading(text, 4);
    }
    public StringBuilder h5(String text) {
        return heading(text, 5);
    }
    public StringBuilder h6(String text) {
        return heading(text, 6);
    }

    // text内部方法
    public StringBuilder paragraph(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null && !text.trim().isEmpty()) {
            ans.append(text.trim());
        }
        return ans;
    }

    //纯文本追加
    public MarkdownBuilder text(String text) {
        content.append(paragraph(text));
        return this;
    }

    // 加粗
    public StringBuilder bold(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("**").append(text).append("**");
        }
        return ans;
    }

    // 斜体
    public StringBuilder italic(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("*").append(text).append("*");
        }
        return ans;
    }

    // 内联
    public StringBuilder inlineCode(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("`").append(text).append("`");
        }
        return ans;
    }

    // 代码块
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

    public String unorder_item(String s){
        return "- " + s;
    }

    // 带level无序列表
    public StringBuilder unorderedList(int level, StringBuilder... items) {
        StringBuilder ans = new StringBuilder();
        if (items != null) {
            String marker = getListMarker("unordered");
            String indent = "  ".repeat(level);

            for (StringBuilder item : items) {
                if (item != null) {
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

    // 有序列表
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
    // 表格
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

    // 引用块
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

    // 水平线
    public MarkdownBuilder horizontalRule() {
        content.append("---")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        return this;
    }
    //链接
    public MarkdownBuilder link(String text, String url) {
        if (text != null && url != null) {
            content.append("[").append(escapeMarkdown(text)).append("](")
                    .append(url).append(")"); // URL should not be escaped
        }
        return this;
    }

    // 图像
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

    // break
    public MarkdownBuilder lineBreak() {
        content.append("  ").append(System.lineSeparator());
        return this;
    }
    // 换行
    public MarkdownBuilder newline() {
        content.append(System.lineSeparator());
        return this;
    }
    // 换行
    public MarkdownBuilder newline(int count) {
        for (int i = 0; i < count; i++) {
            content.append(System.lineSeparator());
        }
        return this;
    }
    // 直接拼接
    public MarkdownBuilder raw(String text) {
        if (text != null) {
            content.append(text);
        }
        return this;
    }

    public String build() {
        return content.toString();
    }

    /**
     * @return MarkdownBuilder 构建器实例
     * @brief 清空构建器
     * @details 清空当前构建器内容，但保留上下文中的输出内容
     */
    public MarkdownBuilder clear() {
        content.setLength(0);
        return this;
    }

    /**
     * @return String 输出的内容字符串
     * @brief 输出到上下文并清空构建器
     * @details 将当前内容输出到渲染上下文并清空构建器，用于内存管理
     */
    public String flush() {
        String flushedContent = content.toString();
        context.getOutput().append(flushedContent);
        content.setLength(0);
        return flushedContent;
    }


    /**
     * @return int 内容字符数
     * @brief 获取当前内容长度
     */
    public int length() {
        return content.length();
    }

    /**
     * @return RenderContext 渲染上下文实例
     * @brief 获取渲染上下文
     */
    public RenderContext getContext() {
        return context;
    }

    // ==================== 文档结构方法 ====================

    /**
     * @param title    文档标题
     * @param metadata 文档元数据
     * @param content  文档内容
     * @return MarkdownBuilder 构建器实例
     * @brief 创建完整文档
     * @details 创建包含标题、元数据和内容的完整文档结构
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
    // 文件元数据转换
    public MarkdownBuilder header(Map<String, Object> metadata) {

        this.append(heading(escapeMarkdown(metadata.get("文件名").toString()), 1));
        this.append(heading("文件信息", 2));
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getValue() != null) {
                String key = formatMetadataKey(entry.getKey());
                String value = entry.getValue().toString();
                text("- **").text(key).text(": ** ").text(value).newline();
            }
        }
        newline();

        return this;
    }

    /**
     * @param text 需要转义的文本
     * @return MarkdownBuilder 构建器实例
     * @brief 添加转义文本
     * @details 添加经过Markdown特殊字符转义的文本
     */
    public MarkdownBuilder escaped(String text) {
        if (text != null) {
            raw(escapeMarkdown(text));
        }
        return this;
    }


    /**
     * @return boolean 是否有效
     * @brief 验证当前内容
     * @details 验证当前构建器内容是否包含有效的Markdown语法
     */
    public boolean isValidContent() {
        return isValidMarkdown(build());
    }

    /**
     * @param markdown 要验证的Markdown字符串
     * @return boolean 是否有效
     * @brief 静态验证Markdown语法
     * @details 验证指定字符串是否包含有效的Markdown语法
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
     * @param key 元数据键名
     * @return String 格式化后的键名
     * @brief 格式化元数据键名
     * @details 将驼峰命名转换为可读格式
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
     * @param value 元数据值
     * @return String 格式化后的字符串
     * @brief 格式化元数据值
     * @details 将不同类型的对象转换为字符串表示
     */
    // Todo: 这里不够严谨
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

    // 列表标记符号
    private String getListMarker(String listType) {
        String style = context.getListStyle();
        if ("unordered".equals(listType)) {
            switch (style) {
                case "asterisk":
                    return "*";
                case "plus":
                    return "+";
                default:
                    return "-";
            }
        }
        return "-";
    }

    // 转义
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

}