package com.markdown.engine.renderer;

import com.markdown.engine.ObjectRenderer;
import com.markdown.engine.context.RenderContext;

import java.util.*;

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
    public String render(Object object, RenderContext context) {
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

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<?, ?>> getTargetClass() {
        return Map.class;
    }

    @Override
    public String getDescription() {
        return "Renders Map objects to Markdown as tables or definition lists";
    }

    /**
     * Determines if map should be rendered as table.
     * Simple maps with string/number/boolean values render well as tables.
     */
    private boolean shouldRenderAsTable(Map<?, ?> map, RenderContext context) {
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

        // Check if all keys are simple strings without newlines
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
    private String renderAsTable(Map<?, ?> map, RenderContext context) {
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

        // Get sorted keys if sorting is enabled
        List<?> keys = new ArrayList<>(map.keySet());
        if (context.shouldSortMapKeys()) {
            keys.sort(Comparator.comparing(key -> key != null ? key.toString() : ""));
        }

        // Table rows
        for (Object key : keys) {
            String keyStr = key != null ? key.toString() : "";
            Object value = map.get(key);
            String valueStr = value != null ? value.toString() : "";

            result.append("| ")
                   .append(escapeMarkdownSpecialChars(keyStr))
                   .append(" | ")
                   .append(escapeMarkdownSpecialChars(valueStr))
                   .append(" |")
                   .append(System.lineSeparator());
        }

        result.append(System.lineSeparator());
        return result.toString();
    }

    /**
     * Renders map as a markdown definition list.
     */
    private String renderAsDefinitionList(Map<?, ?> map, RenderContext context) {
        StringBuilder result = new StringBuilder();

        // Get sorted keys if sorting is enabled
        List<?> keys = new ArrayList<>(map.keySet());
        if (context.shouldSortMapKeys()) {
            keys.sort(Comparator.comparing(key -> key != null ? key.toString() : ""));
        }

        // Definition list items
        for (Object key : keys) {
            if (map.get(key) != null) {
                String keyStr = key != null ? key.toString() : "";
                Object value = map.get(key);

                result.append(escapeMarkdownSpecialChars(keyStr))
                       .append(": ");

                // Handle different value types
                if (value instanceof Collection) {
                    result.append(renderCollectionAsSubList((Collection<?>) value, context));
                } else if (value instanceof Map) {
                    result.append(System.lineSeparator());
                    result.append(renderNestedMap((Map<?, ?>) value, context, 1));
                } else {
                    result.append(escapeMarkdownSpecialChars(value.toString()));
                }

                result.append(System.lineSeparator());
            }
        }

        result.append(System.lineSeparator());
        return result.toString();
    }

    /**
     * Renders a collection as a sub-list within a definition.
     */
    private String renderCollectionAsSubList(Collection<?> collection, RenderContext context) {
        StringBuilder result = new StringBuilder();
        String marker = getListMarker(context.getListStyle());

        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                result.append(" ");
            }
            if (item != null) {
                result.append(marker)
                       .append(" ")
                       .append(escapeMarkdownSpecialChars(item.toString()));
            }
            first = false;
        }

        return result.toString();
    }

    /**
     * Renders a nested map with indentation.
     */
    private String renderNestedMap(Map<?, ?> map, RenderContext context, int indentLevel) {
        StringBuilder result = new StringBuilder();
        String indent = "  ".repeat(indentLevel);

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                String key = entry.getKey() != null ? entry.getKey().toString() : "";
                String value = entry.getValue().toString();

                result.append(indent)
                       .append(escapeMarkdownSpecialChars(key))
                       .append(": ")
                       .append(escapeMarkdownSpecialChars(value))
                       .append(System.lineSeparator());
            }
        }

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