package com.markitdown.core.markdown.renderer;

import com.markitdown.core.markdown.MarkdownContext;
import com.markitdown.core.markdown.ObjectRenderer;
import java.util.Collection;

/**
 * Renderer for Collection objects to Markdown format.
 * Renders collections as unordered lists.
 *
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class CollectionRenderer implements ObjectRenderer<Collection<?>> {

    @Override
    public String render(Object object, MarkdownContext context) {
        Collection<?> collection = (Collection<?>) object;
        if (collection == null || collection.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (Object item : collection) {
            if (item != null) {
                String itemText = item.toString();
                if (!itemText.trim().isEmpty()) {
                    result.append("- ")
                           .append(escapeMarkdownSpecialChars(itemText))
                           .append(System.lineSeparator());
                }
            }
        }

        // Add trailing newline
        result.append(System.lineSeparator());

        return result.toString();
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Collection;
    }

    @Override
    public int getPriority() {
        return 60; // Medium-high priority
    }

    @Override
    public String getName() {
        return "CollectionRenderer";
    }

    /**
     * Escapes markdown special characters in text.
     */
    private String escapeMarkdownSpecialChars(String text) {
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