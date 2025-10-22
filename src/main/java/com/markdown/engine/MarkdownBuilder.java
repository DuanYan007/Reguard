package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;

/**
 * Builder for creating complex Markdown documents step by step.
 * Provides fluent API for building structured markdown content.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
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
     * @param items     the list items
     * @param startNumber the starting number
     * @return this builder for chaining
     */
    public MarkdownBuilder orderedList(int startNumber, String[] items) {
        return orderedList(0, startNumber, items);
    }

    /**
     * Adds an ordered list with indentation level and starting number.
     *
     * @param level      list indentation level (0-based)
     * @param startNumber the starting number
     * @param items      the list items
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

    /**
     * Adds arbitrary text (with escaping if it's not raw).
     *
     * @param text the text
     * @return this builder for chaining
     */
    public MarkdownBuilder text(String text) {
        if (text != null) {
            content.append(escapeMarkdown(text));
        }
        return this;
    }

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

    // Helper methods
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