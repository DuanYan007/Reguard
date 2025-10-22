package com.markdown.engine.renderer;

import com.markdown.engine.ObjectRenderer;
import com.markdown.engine.context.RenderContext;

import java.util.Collection;

/**
 * Renderer for Collection objects to Markdown format.
 * Renders collections as unordered lists.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class CollectionRenderer implements ObjectRenderer<Collection<?>> {

    @Override
    public String render(Object object, RenderContext context) {
        Collection<?> collection = (Collection<?>) object;
        if (collection == null || collection.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String listStyle = context.getListStyle();
        String marker = getListMarker(listStyle);

        int currentDepth = context.getCurrentListDepth();
        String indent = "  ".repeat(currentDepth);

        // Check depth limit
        if (currentDepth >= context.getMaxListDepth()) {
            return renderAsInline(collection, context);
        }

        for (Object item : collection) {
            if (item != null) {
                String itemText = item.toString();
                if (!itemText.trim().isEmpty()) {
                    result.append(indent)
                           .append(marker)
                           .append(" ")
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

    @Override
    @SuppressWarnings("unchecked")
    public Class<Collection<?>> getTargetClass() {
        return Collection.class;
    }

    @Override
    public String getDescription() {
        return "Renders Collection objects to Markdown as unordered lists";
    }

    /**
     * Renders collection as inline content when depth limit is reached.
     */
    private String renderAsInline(Collection<?> collection, RenderContext context) {
        StringBuilder result = new StringBuilder();
        result.append("[");

        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                result.append(", ");
            }
            if (item != null) {
                result.append(escapeMarkdownSpecialChars(item.toString()));
            }
            first = false;
        }

        result.append("]");
        return result.toString();
    }

    /**
     * Gets the list marker based on style.
     */
    private String getListMarker(String listStyle) {
        if (listStyle == null) {
            return "-";
        }
        switch (listStyle.toLowerCase()) {
            case "asterisk":
                return "*";
            case "plus":
                return "+";
            default:
                return "-";
        }
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
                  .replace(".", "\\.")
                  .replace("!", "\\!");
    }
}