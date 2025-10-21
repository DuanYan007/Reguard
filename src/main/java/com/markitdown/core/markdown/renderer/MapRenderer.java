package com.markitdown.core.markdown.renderer;

import com.markitdown.core.markdown.MarkdownContext;
import com.markitdown.core.markdown.ObjectRenderer;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * Renderer for Map objects to Markdown format.
 * Can render maps as tables or definition lists based on complexity.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MapRenderer implements ObjectRenderer<Map<?, ?>> {

    @Override
    public String render(Object object, MarkdownContext context) {
        Map<?, ?> map = (Map<?, ?>) object;
        if (map == null || map.isEmpty()) {
            return "";
        }

        // Determine rendering strategy
        if (shouldRenderAsTable(map, context)) {
            return renderAsTable(map, context);
        } else {
            return renderAsDefinitionList(map, context);
        }
    }

    @Override
    public boolean supports(Object object) {
        return object instanceof Map;
    }

    @Override
    public int getPriority() {
        return 70; // High priority for structured data
    }

    @Override
    public String getName() {
        return "MapRenderer";
    }

    /**
     * Determines if map should be rendered as table.
     * Simple maps with string/number/boolean values render well as tables.
     */
    private boolean shouldRenderAsTable(Map<?, ?> map, MarkdownContext context) {
        // Only render as table if tables are enabled
        if (!context.shouldIncludeTables()) {
            return false;
        }

        // Check if all values are simple types
        for (Object value : map.values()) {
            if (value == null || value instanceof Map || value instanceof Collection) {
                return false;
            }
        }

        // Check if all keys are simple strings
        for (Object key : map.keySet()) {
            if (!(key instanceof String) || ((String) key).contains(System.lineSeparator())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Renders map as a markdown table.
     */
    private String renderAsTable(Map<?, ?> map, MarkdownContext context) {
        StringBuilder result = new StringBuilder();

        // Table header
        result.append("| ")
               .append(escapeMarkdownSpecialChars("Key"))
               .append(" | ")
               .append(escapeMarkdownSpecialChars("Value"))
               .append(" |")
               .append(System.lineSeparator());

        // Table separator
        String format = context.getTableFormat();
        if ("github".equals(format) || "markdown".equals(format)) {
            result.append("|---|------|");
        } else if ("pipe".equals(format)) {
            result.append("|:---|:----|");
        }

        result.append(System.lineSeparator());

        // Table rows
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().toString() : "";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";

            result.append("| ")
                   .append(escapeMarkdownSpecialChars(key))
                   .append(" | ")
                   .append(escapeMarkdownSpecialChars(value))
                   .append(" |")
                   .append(System.lineSeparator());
        }

        return result.toString();
    }

    /**
     * Renders map as a markdown definition list.
     */
    private String renderAsDefinitionList(Map<?, ?> map, MarkdownContext context) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                String key = entry.getKey() != null ? entry.getKey().toString() : "";
                String value = entry.getValue().toString();

                result.append(escapeMarkdownSpecialChars(key))
                       .append(": ")
                       .append(escapeMarkdownSpecialChars(value))
                       .append(System.lineSeparator());
            }
        }

        result.append(System.lineSeparator());
        return result.toString();
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