package com.markitdown.core.markdown.renderer;

import com.markitdown.core.markdown.MarkdownContext;
import com.markitdown.core.markdown.ObjectRenderer;

/**
 * Renderer for String objects to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringRenderer implements ObjectRenderer<String> {

    @Override
    public String render(Object object, MarkdownContext context) {
        String text = (String) object;
        if (text == null) {
            return "";
        }

        // Escape markdown special characters
        return escapeMarkdownSpecialChars(text);
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof String;
    }

    @Override
    public int getPriority() {
        return 50; // Medium priority
    }

    @Override
    public String getName() {
        return "StringRenderer";
    }

    /**
     * Escapes markdown special characters in text.
     */
    private String escapeMarkdownSpecialChars(String text) {
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