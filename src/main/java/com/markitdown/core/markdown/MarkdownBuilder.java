package com.markitdown.core.markdown;

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
    private final MarkdownContext context;

    /**
     * Creates a new markdown builder.
     *
     * @param context the markdown context
     */
    public MarkdownBuilder(MarkdownContext context) {
        this.context = context;
        this.content = new StringBuilder();
    }

    /**
     * Adds a heading.
     *
     * @param text the heading text
     * @param level the heading level (1-6)
     * @return this builder for chaining
     */
    public MarkdownBuilder heading(String text, int level) {
        if (text == null || text.trim().isEmpty()) {
            return this;
        }

        int safeLevel = Math.max(1, Math.min(6, level));
        content.append("#".repeat(safeLevel))
               .append(" ")
               .append(text.trim())
               .append(System.lineSeparator())
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
     * Adds inline code.
     *
     * @param text the code text
     * @return this builder for chaining
     */
    public MarkdownBuilder inlineCode(String text) {
        if (text != null) {
            content.append("`").append(escapeMarkdown(text)).append("`");
        }
        return this;
    }

    /**
     * Adds a fenced code block.
     *
     * @param code the code content
     * @param language the programming language (optional)
     * @return this builder for chaining
     */
    public MarkdownBuilder codeBlock(String code, String language) {
        if (code != null) {
            content.append("```");
            if (language != null && !language.trim().isEmpty()) {
                content.append(language.trim());
            }
            content.append(System.lineSeparator())
                   .append(code)
                   .append(System.lineSeparator())
                   .append("```")
                   .append(System.lineSeparator())
                   .append(System.lineSeparator());
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
        if (items != null) {
            for (String item : items) {
                if (item != null && !item.trim().isEmpty()) {
                    content.append("- ").append(escapeMarkdown(item.trim()))
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
     * @param startNumber the starting number (default: 1)
     * @return this builder for chaining
     */
    public MarkdownBuilder orderedList(String[] items, int startNumber) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (item != null && !item.trim().isEmpty()) {
                    content.append((startNumber + i) + ". ")
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
     * @param url the link URL
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
     * @param url the image URL
     * @param title the image title (optional)
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
     * Adds arbitrary text.
     *
     * @param text the text
     * @return this builder for chaining
     */
    public MarkdownBuilder text(String text) {
        if (text != null) {
            content.append(text);
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
     * Escapes markdown special characters in text.
     *
     * @param text the text to escape
     * @return escaped text
     */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return "";
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
                  .replace(".", "\\.");
        // Note: ! is only escaped in specific contexts in Markdown, not globally
    }
}