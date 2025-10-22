package com.markdown.engine.impl;

import com.markdown.engine.*;
import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;
import com.markdown.engine.renderer.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

/**
 * Default implementation of MarkdownEngine interface.
 * Provides comprehensive markdown generation capabilities with extensible rendering system.
 * Completely independent from document conversion concerns.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownEngineImpl implements MarkdownEngine {

    private final Map<Class<?>, ObjectRenderer<?>> renderers;
    private final EngineInfo engineInfo;
    private final MarkdownConfig defaultConfig;

    /**
     * Creates a new markdown engine implementation with default configuration and renderers.
     */
    public MarkdownEngineImpl() {
        this(MarkdownConfig.builder().build());
    }

    /**
     * Creates a new markdown engine implementation with specified configuration.
     *
     * @param defaultConfig default configuration
     */
    public MarkdownEngineImpl(MarkdownConfig defaultConfig) {
        this.renderers = new ConcurrentHashMap<>();
        this.defaultConfig = defaultConfig != null ? defaultConfig : MarkdownConfig.builder().build();
        this.engineInfo = createEngineInfo();
        registerDefaultRenderers();
    }

    @Override
    public String convert(Object content) {
        return convert(content, defaultConfig);
    }

    @Override
    public String convert(Object content, MarkdownConfig config) {
        return convertWithMetadata(content, Collections.emptyMap(), config);
    }

    @Override
    public String convertWithMetadata(Object content, Map<String, Object> metadata) {
        return convertWithMetadata(content, metadata, defaultConfig);
    }

    @Override
    public String convertWithMetadata(Object content, Map<String, Object> metadata, MarkdownConfig config) {
        if (content == null) {
            return "";
        }

        MarkdownConfig actualConfig = config != null ? config : defaultConfig;
        RenderContext context = new RenderContext(actualConfig, metadata);

        // Add metadata section if enabled
        if (context.shouldIncludeMetadata() && !metadata.isEmpty()) {
            addMetadataSection(context);
        }

        // Render main content
        ObjectRenderer<?> renderer = findRenderer(content);
        String renderedContent = renderer != null ?
                renderer.render(content, context) :
                renderAsString(content, context);

        context.append(renderedContent);
        return context.getContent();
    }

    @Override
    public MarkdownBuilder createBuilder() {
        return new MarkdownBuilder(defaultConfig);
    }

    @Override
    public MarkdownBuilder createBuilder(MarkdownConfig config) {
        return new MarkdownBuilder(config != null ? config : defaultConfig);
    }

    @Override
    public <T> void registerRenderer(Class<T> objectType, ObjectRenderer<T> renderer) {
        if (objectType != null && renderer != null) {
            renderers.put(objectType, renderer);
        }
    }

    @Override
    public void unregisterRenderer(Class<?> objectType) {
        if (objectType != null) {
            renderers.remove(objectType);
        }
    }

    @Override
    public boolean hasRenderer(Class<?> objectType) {
        return objectType != null && renderers.containsKey(objectType);
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
    private void addMetadataSection(RenderContext context) {
        Map<String, Object> metadata = context.getMetadata();

        context.append("## Document Information")
               .newline()
               .newline();

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getValue() != null) {
                String key = formatMetadataKey(entry.getKey());
                String value = formatMetadataValue(entry.getValue(), context);
                context.appendf("- **%s:** %s", key, value)
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
    private String formatMetadataValue(Object value, RenderContext context) {
        if (value == null) {
            return "";
        }

        if (value instanceof Date) {
            return renderDate((Date) value, context);
        }

        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            return "[" + String.join(", ", collection.stream().map(Object::toString).toArray(String[]::new)) + "]";
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return String.valueOf(map.size()) + " items";
        }

        return escapeMarkdown(value.toString());
    }

    /**
     * Finds appropriate renderer for given object.
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

            // Check if the renderer class is a superclass or interface of the object class
            if (rendererClass.isAssignableFrom(objectClass) && rendererImpl.supports(content)) {
                return rendererImpl;
            }
        }

        return null;
    }

    /**
     * Renders object as string when no specific renderer is found.
     */
    private String renderAsString(Object content, RenderContext context) {
        if (content == null) {
            return "";
        }

        if (content instanceof String) {
            return renderString((String) content, context);
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

        if (content instanceof Date) {
            return renderDate((Date) content, context);
        }

        // Fallback: convert to string and escape
        return escapeMarkdown(content.toString());
    }

    /**
     * Renders a string with escaping.
     */
    private String renderString(String content, RenderContext context) {
        return escapeMarkdown(content);
    }

    /**
     * Renders a collection as a list.
     */
    private String renderCollection(Collection<?> collection, RenderContext context) {
        CollectionRenderer renderer = new CollectionRenderer();
        return renderer.render(collection, context);
    }

    /**
     * Renders a map as a table or definition list.
     */
    private String renderMap(Map<?, ?> map, RenderContext context) {
        MapRenderer renderer = new MapRenderer();
        return renderer.render(map, context);
    }

    /**
     * Renders an array as a list.
     */
    private String renderArray(List<?> array, RenderContext context) {
        return renderCollection(array, context);
    }

    /**
     * Renders a date.
     */
    private String renderDate(Date date, RenderContext context) {
        DateRenderer renderer = new DateRenderer();
        return renderer.render(date, context);
    }

    /**
     * Escapes markdown special characters in text.
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
                  .replace(".", "\\.")
                  .replace("!", "\\!");
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
                "extensible",
                "configurable",
                "thread-safe"
        );

        Set<String> languages = Set.of(
                "java", "javascript", "python", "sql", "json", "xml", "html", "css",
                "bash", "powershell", "markdown", "text", "yaml", "properties"
        );

        return new EngineInfo(
                "Independent Markdown Engine",
                "1.0.0",
                features,
                languages,
                "A standalone Markdown engine for Java objects, completely independent from document conversion",
                "duan yan"
        );
    }
}