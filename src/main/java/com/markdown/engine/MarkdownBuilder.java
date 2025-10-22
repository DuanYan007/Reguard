package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating complex Markdown documents step by step.
 * Provides fluent API for building structured markdown content.
 * This is the core class for Converter integration.
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class MarkdownBuilder {

    private final StringBuilder content;
    private final RenderContext context;

    /**
     * Creates a new markdown builder with default configuration.
     */
    public MarkdownBuilder() {
        this(MarkdownConfig.builder().build());
    }

    /**
     * Creates a new markdown builder with specified configuration.
     *
     * @param config the rendering configuration
     */
    public MarkdownBuilder(MarkdownConfig config) {
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }

    /**
     * Creates a new markdown builder with the given context.
     *
     * @param context the render context
     */
    public MarkdownBuilder(RenderContext context) {
        this.context = context;
        this.content = new StringBuilder();
    }

    /**
     * Creates a new markdown builder with specific configuration options for converters.
     *
     * @param includeTables    whether to include tables
     * @param escapeHtml       whether to escape HTML
     * @param wrapCodeBlocks   whether to wrap code blocks
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

    // ==================== Heading Methods ====================

    /**
     * Adds a heading.
     *
     * @param text  the heading text
     * @param level the heading level (1-6)
     * @return this builder for chaining
     */
    public MarkdownBuilder heading(String text, int level) {
        if (text == null || text.trim().isEmpty()) {
            return this;
        }

        int safeLevel = Math.max(1, Math.min(6, level));
        String headingStyle = context.getHeadingStyle();

        if ("setext".equals(headingStyle) && safeLevel <= 2) {
            // Use setext-style headings (underlined)
            content.append(text.trim()).append(System.lineSeparator());
            if (safeLevel == 1) {
                content.append("=".repeat(text.trim().length()));
            } else {
                content.append("-".repeat(text.trim().length()));
            }
        } else {
            // Use ATX-style headings (with #)
            content.append("#".repeat(safeLevel))
                   .append(" ")
                   .append(text.trim());
        }

        content.append(System.lineSeparator())
               .append(System.lineSeparator());
        return this;
    }

    /**
     * Adds a level 1 heading.
     */
    public MarkdownBuilder h1(String text) {
        return heading(text, 1);
    }

    /**
     * Adds a level 2 heading.
     */
    public MarkdownBuilder h2(String text) {
        return heading(text, 2);
    }

    /**
     * Adds a level 3 heading.
     */
    public MarkdownBuilder h3(String text) {
        return heading(text, 3);
    }

    // ==================== Text Methods ====================

    /**
     * Adds a paragraph.
     *
     * @param text the paragraph text
     * @return this builder for chaining
     */
    public MarkdownBuilder paragraph(String text) {
        if (text != null && !text.trim().isEmpty()) {
            content.append(escapeMarkdown(text.trim()))
                   .append(System.lineSeparator())
                   .append(System.lineSeparator());
        }
        return this;
    }

    /**
     * Adds plain text (same as paragraph).
     */
    public MarkdownBuilder text(String text) {
        return paragraph(text);
    }

    /**
     * Adds bold text.
     *
     * @param text the text to make bold
     * @return this builder for chaining
     */
    public MarkdownBuilder bold(String text) {
        if (text != null) {
            content.append("**").append(escapeMarkdown(text)).append("**");
        }
        return this;
    }

    /**
     * Adds italic text.
     *
     * @param text the text to make italic
     * @return this builder for chaining
     */
    public MarkdownBuilder italic(String text) {
        if (text != null) {
            content.append("*").append(escapeMarkdown(text)).append("*");
        }
        return this;
    }

    /**
     * Adds strikethrough text.
     *
     * @param text the text to strikethrough
     * @return this builder for chaining
     */
    public MarkdownBuilder strikethrough(String text) {
        if (text != null) {
            content.append("~~").append(escapeMarkdown(text)).append("~~");
        }
        return this;
    }

    /**
     * Adds inline code.
     *
     * @param text the code text
     * @return this builder for chaining
     */
    public MarkdownBuilder inlineCode(String text) {
        if (text != null) {
            content.append("`").append(escapeCodeInline(text)).append("`");
        }
        return this;
    }

    // ==================== Code Block Methods ====================

    /**
     * Adds a fenced code block.
     *
     * @param code     the code content
     * @param language the programming language (optional)
     * @return this builder for chaining
     */
    public MarkdownBuilder codeBlock(String code, String language) {
        if (code != null) {
            if (context.shouldWrapCodeBlocks()) {
                content.append("```");
                if (language != null && !language.trim().isEmpty()) {
                    content.append(language.trim());
                }
                content.append(System.lineSeparator());
            }
            content.append(code);
            if (context.shouldWrapCodeBlocks()) {
                content.append(System.lineSeparator()).append("```");
            }
            content.append(System.lineSeparator()).append(System.lineSeparator());
        }
        return this;
    }

    /**
     * Adds a code block without language specification.
     */
    public MarkdownBuilder codeBlock(String code) {
        return codeBlock(code, null);
    }

    // ==================== List Methods ====================

    /**
     * Adds an unordered list.
     *
     * @param items the list items
     * @return this builder for chaining
     */
    public MarkdownBuilder unorderedList(String... items) {
        return unorderedList(0, items);
    }

    /**
     * Adds an unordered list with indentation level.
     *
     * @param level list indentation level (0-based)
     * @param items the list items
     * @return this builder for chaining
     */
    public MarkdownBuilder unorderedList(int level, String... items) {
        if (items != null) {
            String marker = getListMarker("unordered");
            String indent = "  ".repeat(level);

            for (String item : items) {
                if (item != null && !item.trim().isEmpty()) {
                    content.append(indent)
                           .append(marker)
                           .append(" ")
                           .append(escapeMarkdown(item.trim()))
                           .append(System.lineSeparator());
                }
            }
            content.append(System.lineSeparator());
        }
        return this;
    }

    /**
     * Adds an ordered list.
     *
     * @param items the list items
     * @return this builder for chaining
     */
    public MarkdownBuilder orderedList(String[] items) {
        return orderedList(1, items);
    }

    /**
     * Adds an ordered list with starting number.
     *
     * @param items      the list items
     * @param startNumber the starting number
     * @return this builder for chaining
     */
    public MarkdownBuilder orderedList(int startNumber, String[] items) {
        return orderedList(0, startNumber, items);
    }

    /**
     * Adds an ordered list with indentation level and starting number.
     *
     * @param level       list indentation level (0-based)
     * @param startNumber the starting number
     * @param items       the list items
     * @return this builder for chaining
     */
    public MarkdownBuilder orderedList(int level, int startNumber, String[] items) {
        if (items != null) {
            String indent = "  ".repeat(level);

            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (item != null && !item.trim().isEmpty()) {
                    content.append(indent)
                           .append((startNumber + i) + ". ")
                           .append(escapeMarkdown(item.trim()))
                           .append(System.lineSeparator());
                }
            }
            content.append(System.lineSeparator());
        }
        return this;
    }

    // ==================== Table Methods ====================

    /**
     * Adds a table with headers and rows.
     *
     * @param headers table headers
     * @param rows    table rows (each row is an array of cells)
     * @return this builder for chaining
     */
    public MarkdownBuilder table(String[] headers, String[][] rows) {
        if (!context.shouldIncludeTables() || headers == null || headers.length == 0) {
            return this;
        }

        // Table header
        content.append("| ");
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) content.append(" | ");
            content.append(escapeMarkdown(headers[i] != null ? headers[i].trim() : ""));
        }
        content.append(" |").append(System.lineSeparator());

        // Table separator
        content.append("|");
        for (int i = 0; i < headers.length; i++) {
            content.append("-----|");
        }
        content.append(System.lineSeparator());

        // Table rows
        if (rows != null) {
            for (String[] row : rows) {
                content.append("| ");
                for (int i = 0; i < headers.length; i++) {
                    if (i > 0) content.append(" | ");
                    String cell = (row != null && i < row.length) ? row[i] : "";
                    content.append(escapeMarkdown(cell != null ? cell.trim() : ""));
                }
                content.append(" |").append(System.lineSeparator());
            }
        }

        content.append(System.lineSeparator());
        return this;
    }

    // ==================== Other Elements ====================

    /**
     * Adds a blockquote.
     *
     * @param text the quoted text
     * @return this builder for chaining
     */
    public MarkdownBuilder blockquote(String text) {
        if (text != null) {
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                content.append("> ").append(line).append(System.lineSeparator());
            }
            content.append(System.lineSeparator());
        }
        return this;
    }

    /**
     * Adds a horizontal rule.
     *
     * @return this builder for chaining
     */
    public MarkdownBuilder horizontalRule() {
        content.append("---")
               .append(System.lineSeparator())
               .append(System.lineSeparator());
        return this;
    }

    /**
     * Adds a link.
     *
     * @param text the link text
     * @param url  the link URL
     * @return this builder for chaining
     */
    public MarkdownBuilder link(String text, String url) {
        if (text != null && url != null) {
            content.append("[").append(escapeMarkdown(text)).append("](")
                   .append(url).append(")"); // URL should not be escaped
        }
        return this;
    }

    /**
     * Adds an image.
     *
     * @param altText the alt text for the image
     * @param url     the image URL
     * @param title   the image title (optional)
     * @return this builder for chaining
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
     * Adds a line break.
     *
     * @return this builder for chaining
     */
    public MarkdownBuilder lineBreak() {
        content.append("  ").append(System.lineSeparator());
        return this;
    }

    /**
     * Adds a newline.
     *
     * @return this builder for chaining
     */
    public MarkdownBuilder newline() {
        content.append(System.lineSeparator());
        return this;
    }

    /**
     * Adds multiple newlines.
     *
     * @param count number of newlines
     * @return this builder for chaining
     */
    public MarkdownBuilder newline(int count) {
        for (int i = 0; i < count; i++) {
            content.append(System.lineSeparator());
        }
        return this;
    }

    /**
     * Adds raw text (no escaping).
     *
     * @param text the raw text
     * @return this builder for chaining
     */
    public MarkdownBuilder raw(String text) {
        if (text != null) {
            content.append(text);
        }
        return this;
    }

    // ==================== Utility Methods ====================

    /**
     * Builds the final markdown string.
     *
     * @return the complete markdown content
     */
    public String build() {
        return content.toString();
    }

    /**
     * Clears the builder for reuse.
     *
     * @return this builder for chaining
     */
    public MarkdownBuilder clear() {
        content.setLength(0);
        return this;
    }

    /**
     * Gets the current content length.
     *
     * @return current content length
     */
    public int length() {
        return content.length();
    }

    /**
     * Gets the render context.
     *
     * @return the render context
     */
    public RenderContext getContext() {
        return context;
    }

    // ==================== Document Structure Methods ====================

    /**
     * Creates a complete document with title, metadata, and content.
     * This is a convenience method for creating standard document structures.
     *
     * @param title    document title
     * @param metadata document metadata
     * @param content  document content
     * @return this builder for chaining
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
     * Adds document header with metadata.
     *
     * @param title    document title
     * @param metadata document metadata
     * @return this builder for chaining
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
     * Converts a collection to a list and adds it to the builder.
     *
     * @param items the items to convert
     * @return this builder for chaining
     */
    public MarkdownBuilder listFromCollection(Collection<String> items) {
        if (items != null && !items.isEmpty()) {
            unorderedList(items.toArray(new String[0]));
        }
        return this;
    }

    /**
     * Converts a map to a table and adds it to the builder.
     *
     * @param data the data to convert (key-value pairs)
     * @return this builder for chaining
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
     * Converts a list of maps to a table and adds it to the builder.
     *
     * @param headers table headers
     * @param rows    list of row data (each map represents a row)
     * @return this builder for chaining
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
     * Adds multiple paragraphs from an array.
     *
     * @param paragraphs the paragraphs to add
     * @return this builder for chaining
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
     * Appends escaped text to the builder.
     * This is useful for adding text that may contain special Markdown characters.
     *
     * @param text the text to escape and append
     * @return this builder for chaining
     */
    public MarkdownBuilder escaped(String text) {
        if (text != null) {
            raw(escapeMarkdown(text));
        }
        return this;
    }

    /**
     * Appends text with proper Markdown escaping.
     * Same as escaped() method but with more descriptive name.
     *
     * @param text the text to escape and append
     * @return this builder for chaining
     */
    public MarkdownBuilder safeText(String text) {
        return escaped(text);
    }

    /**
     * Validates if the current builder content contains valid Markdown syntax.
     *
     * @return true if content appears valid, false otherwise
     */
    public boolean isValidContent() {
        return isValidMarkdown(build());
    }

    /**
     * Static utility method to validate Markdown syntax.
     *
     * @param markdown the markdown string to validate
     * @return true if appears valid, false otherwise
     */
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

    // ==================== Private Helper Methods ====================

    private String formatMetadataKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }

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

    private String escapeMarkdown(String text) {
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

    private String escapeCodeInline(String text) {
        if (text == null) {
            return "";
        }
        // For inline code, only escape backticks and backslashes
        return text.replace("`", "\\`").replace("\\", "\\\\");
    }
}