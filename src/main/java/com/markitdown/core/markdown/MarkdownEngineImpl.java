package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
import com.markitdown.core.markdown.renderer.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

/**
 * Default implementation of the MarkdownEngine interface.
 * Provides comprehensive markdown generation capabilities with extensible rendering system.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownEngineImpl implements MarkdownEngine {

    private final Map<Class<?>, ObjectRenderer<?>> renderers;
    private final EngineInfo engineInfo;

    /**
     * Creates a new markdown engine implementation with default renderers.
     */
    public MarkdownEngineImpl() {
        this.renderers = new ConcurrentHashMap<>();
        this.engineInfo = createEngineInfo();
        registerDefaultRenderers();
    }

    @Override
    public String convert(Object content, ConversionOptions options) {
        return convertWithMetadata(content, Collections.emptyMap(), options);
    }

    @Override
    public String convertWithMetadata(Object content, Map<String, Object> metadata, ConversionOptions options) {
        if (content == null) {
            return "";
        }

        MarkdownContext context = new MarkdownContext(options, metadata);

        // Add metadata section if enabled
        if (context.shouldIncludeMetadata() && !metadata.isEmpty()) {
            addMetadataSection(context);
        }

        // Render the main content
        ObjectRenderer<?> renderer = findRenderer(content);
        String renderedContent = renderer != null ?
                renderer.render(content, context) :
                renderAsString(content, context);

        context.append(renderedContent);
        return context.getContent();
    }

    @Override
    public MarkdownBuilder createBuilder() {
        ConversionOptions defaultOptions = new ConversionOptions();
        MarkdownContext context = new MarkdownContext(defaultOptions, Collections.emptyMap());
        return new MarkdownBuilder(context);
    }

    @Override
    public <T> void registerRenderer(Class<T> objectType, ObjectRenderer<T> renderer) {
        if (objectType != null && renderer != null) {
            renderers.put(objectType, renderer);
        }
    }

    @Override
    public boolean isValidMarkdown(String markdown) {
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

    @Override
    public EngineInfo getEngineInfo() {
        return engineInfo;
    }

    /**
     * Adds metadata section to the context.
     */
    private void addMetadataSection(MarkdownContext context) {
        Map<String, Object> metadata = context.getMetadata();

        context.append("## Document Information")
               .newline()
               .newline();

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getValue() != null) {
                String key = formatMetadataKey(entry.getKey());
                String value = formatMetadataValue(entry.getValue());
                context.append(String.format("- **%s:** %s", key, value))
                       .newline();
            }
        }

        context.newline()
               .append("## Content")
               .newline()
               .newline();
    }

    /**
     * Formats metadata keys for display.
     */
    private String formatMetadataKey(String key) {
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }

    /**
     * Formats metadata values for display.
     */
    private String formatMetadataValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof Date) {
            return value.toString();
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

    /**
     * Finds the appropriate renderer for the given object.
     */
    @SuppressWarnings("unchecked")
    private ObjectRenderer<?> findRenderer(Object content) {
        if (content == null) {
            return null;
        }

        Class<?> objectClass = content.getClass();

        // Direct class match
        ObjectRenderer<?> renderer = renderers.get(objectClass);
        if (renderer != null && renderer.supports(content)) {
            return renderer;
        }

        // Interface and superclass match
        for (Map.Entry<Class<?>, ObjectRenderer<?>> entry : renderers.entrySet()) {
            Class<?> rendererClass = entry.getKey();
            ObjectRenderer<?> rendererImpl = entry.getValue();

            if (rendererImpl.supports(content)) {
                return rendererImpl;
            }
        }

        return null;
    }

    /**
     * Renders object as string when no specific renderer is found.
     */
    private String renderAsString(Object content, MarkdownContext context) {
        if (content == null) {
            return "";
        }

        if (content instanceof String) {
            return escapeMarkdownContent((String) content, context);
        }

        if (content instanceof Collection) {
            return renderCollection((Collection<?>) content, context);
        }

        if (content instanceof Map) {
            return renderMap((Map<?, ?>) content, context);
        }

        if (content.getClass().isArray()) {
            return renderArray(Arrays.asList((Object[]) content), context);
        }

        // Fallback: convert to string and escape
        return escapeMarkdownContent(content.toString(), context);
    }

    /**
     * Renders a collection as a list.
     */
    private String renderCollection(Collection<?> collection, MarkdownContext context) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }

        MarkdownBuilder builder = createBuilder();
        for (Object item : collection) {
            String itemText = item != null ? item.toString() : "";
            builder.paragraph(itemText);
        }

        return builder.build();
    }

    /**
     * Renders a map as a table or definition list.
     */
    private String renderMap(Map<?, ?> map, MarkdownContext context) {
        if (map == null || map.isEmpty()) {
            return "";
        }

        // Check if all values are simple (no nested structures)
        boolean isSimple = map.values().stream()
                .allMatch(value -> value == null ||
                        value instanceof String ||
                        value instanceof Number ||
                        value instanceof Boolean);

        if (isSimple && context.shouldIncludeTables()) {
            return renderSimpleMapAsTable(map, context);
        } else {
            return renderComplexMapAsDefinition(map, context);
        }
    }

    /**
     * Renders a simple map as a table.
     */
    private String renderSimpleMapAsTable(Map<?, ?> map, MarkdownContext context) {
        MarkdownBuilder builder = createBuilder();

        // Table header
        builder.paragraph("| Key | Value |")
               .paragraph("|-----|-------|");

        // Table rows
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().toString() : "";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            builder.paragraph("| " + escapeMarkdownContent(key, context) + " | " +
                         escapeMarkdownContent(value, context) + " |");
        }

        return builder.build();
    }

    /**
     * Renders a complex map as a definition list.
     */
    private String renderComplexMapAsDefinition(Map<?, ?> map, MarkdownContext context) {
        MarkdownBuilder builder = createBuilder();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().toString() : "";
            Object value = entry.getValue();

            if (value != null) {
                String renderedValue = renderAsString(value, context);
                builder.paragraph(key + ":")
                       .paragraph("  " + renderedValue);
            }
        }

        return builder.build();
    }

    /**
     * Renders an array as a list.
     */
    private String renderArray(List<?> array, MarkdownContext context) {
        if (array == null || array.isEmpty()) {
            return "";
        }

        MarkdownBuilder builder = createBuilder();
        for (Object item : array) {
            String itemText = item != null ? item.toString() : "";
            builder.paragraph("- " + itemText);
        }

        return builder.build();
    }

    /**
     * Escapes markdown special characters in text content.
     */
    private String escapeMarkdownContent(String text, MarkdownContext context) {
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

    /**
     * Registers default renderers for common object types.
     */
    private void registerDefaultRenderers() {
        registerRenderer(String.class, new StringRenderer());
        registerRenderer(Number.class, new NumberRenderer());
        registerRenderer(Boolean.class, new BooleanRenderer());
        registerRenderer(Date.class, new DateRenderer());
        // Register with raw types to avoid generic conflicts
        renderers.put(Collection.class, new CollectionRenderer());
        renderers.put(Map.class, new MapRenderer());
    }

    /**
     * Creates engine information.
     */
    private EngineInfo createEngineInfo() {
        Set<String> features = Set.of(
                "headings",
                "paragraphs",
                "lists",
                "tables",
                "code",
                "links",
                "images",
                "metadata",
                "extensible"
        );

        Set<String> languages = Set.of(
                "java", "javascript", "python", "sql", "json", "xml", "html", "css",
                "bash", "powershell", "markdown", "text"
        );

        return new EngineInfo("MarkItDown Markdown Engine", "1.0.0", features, languages);
    }
}