package com.markdown.engine.renderer;

import com.markdown.engine.ObjectRenderer;
import com.markdown.engine.context.RenderContext;

/**
 * Renderer for String objects to Markdown format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringRenderer implements ObjectRenderer<String> {

    @Override
    public String render(Object object, RenderContext context) {
        String text = (String) object;
        if (text == null) {
            return "";
        }

        // Escape markdown special characters
        return escapeMarkdownSpecialChars(text, context);
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

    @Override
    @SuppressWarnings("unchecked")
    public Class<String> getTargetClass() {
        return String.class;
    }

    @Override
    public String getDescription() {
        return "Renders String objects to Markdown with proper character escaping";
    }

    /**
     * Escapes markdown special characters in text.
     */
    private String escapeMarkdownSpecialChars(String text, RenderContext context) {
        if (text == null) {
            return "";
        }

        String result = text.replace("\\", "\\\\")
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

        // HTML escaping if enabled
        if (context.shouldEscapeHtml()) {
            result = result.replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("&", "&amp;");
        }

        return result;
    }
}