package com.markdown.engine.context;

import com.markdown.engine.config.MarkdownConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context object that provides configuration and state during Markdown generation.
 * Maintains rendering options, metadata, and temporary state.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class RenderContext {

    private final MarkdownConfig config;
    private final Map<String, Object> metadata;
    private final Map<String, Object> state;
    private final StringBuilder output;

    /**
     * Creates a new render context.
     *
     * @param config   rendering configuration
     * @param metadata document metadata (can be empty or null)
     */
    public RenderContext(MarkdownConfig config, Map<String, Object> metadata) {
        this.config = config != null ? config : MarkdownConfig.builder().build();
        this.metadata = new ConcurrentHashMap<>();
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.metadata.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.state = new ConcurrentHashMap<>();
        this.output = new StringBuilder();
    }

    /**
     * Creates a new render context with empty metadata.
     *
     * @param config rendering configuration
     */
    public RenderContext(MarkdownConfig config) {
        this(config, null);
    }

    /**
     * Gets the rendering configuration.
     *
     * @return configuration
     */
    public MarkdownConfig getConfig() {
        return config;
    }

    /**
     * Gets the document metadata.
     *
     * @return metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Gets the output buffer for building markdown content.
     *
     * @return string builder
     */
    public StringBuilder getOutput() {
        return output;
    }

    /**
     * Sets a state value in the context.
     *
     * @param key   the state key
     * @param value the state value
     */
    public void setState(String key, Object value) {
        state.put(key, value);
    }

    /**
     * Gets a state value from the context.
     *
     * @param key the state key
     * @return the state value, or null if not found
     */
    public Object getState(String key) {
        return state.get(key);
    }

    /**
     * Gets a typed state value.
     *
     * @param key  the state key
     * @param type the expected type
     * @param <T>  the type parameter
     * @return the state value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getState(String key, Class<T> type) {
        Object value = state.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    // Convenience methods for common configuration options
    public boolean shouldIncludeMetadata() {
        return config.isIncludeMetadata();
    }

    public boolean shouldIncludeTables() {
        return config.isIncludeTables();
    }

    public String getTableFormat() {
        return config.getTableFormat();
    }

    public String getListStyle() {
        return config.getListStyle();
    }

    public String getHeadingStyle() {
        return config.getHeadingStyle();
    }

    public boolean shouldEscapeHtml() {
        return config.isEscapeHtml();
    }

    public boolean shouldWrapCodeBlocks() {
        return config.isWrapCodeBlocks();
    }

    public int getMaxListDepth() {
        return config.getMaxListDepth();
    }

    public boolean shouldSortMapKeys() {
        return config.isSortMapKeys();
    }

    public String getDateFormat() {
        return config.getDateFormat();
    }

    /**
     * Resets the context for reuse.
     */
    public void reset() {
        output.setLength(0);
        state.clear();
    }

    /**
     * Gets the current content as a string.
     *
     * @return current markdown content
     */
    public String getContent() {
        return output.toString();
    }

    /**
     * Appends text to the output buffer.
     *
     * @param text text to append
     * @return this context for method chaining
     */
    public RenderContext append(String text) {
        output.append(text);
        return this;
    }

    /**
     * Appends a newline to the output buffer.
     *
     * @return this context for method chaining
     */
    public RenderContext newline() {
        output.append(System.lineSeparator());
        return this;
    }

    /**
     * Appends multiple newlines to the output buffer.
     *
     * @param count number of newlines to append
     * @return this context for method chaining
     */
    public RenderContext newline(int count) {
        for (int i = 0; i < count; i++) {
            output.append(System.lineSeparator());
        }
        return this;
    }

    /**
     * Appends a formatted string to the output buffer.
     *
     * @param format string format
     * @param args   format arguments
     * @return this context for method chaining
     */
    public RenderContext appendf(String format, Object... args) {
        output.append(String.format(format, args));
        return this;
    }

    /**
     * Increases the current list depth for nested list rendering.
     *
     * @return the new depth value
     */
    public int incrementListDepth() {
        int currentDepth = getState("listDepth", Integer.class);
        if (currentDepth == null) {
            currentDepth = 0;
        }
        int newDepth = currentDepth + 1;
        setState("listDepth", newDepth);
        return newDepth;
    }

    /**
     * Decreases the current list depth for nested list rendering.
     *
     * @return the new depth value
     */
    public int decrementListDepth() {
        int currentDepth = getState("listDepth", Integer.class);
        if (currentDepth == null || currentDepth <= 0) {
            currentDepth = 1;
        }
        int newDepth = currentDepth - 1;
        setState("listDepth", newDepth);
        return newDepth;
    }

    /**
     * Gets the current list depth.
     *
     * @return current list depth (0 if not set)
     */
    public int getCurrentListDepth() {
        Integer depth = getState("listDepth", Integer.class);
        return depth != null ? depth : 0;
    }

    @Override
    public String toString() {
        return "RenderContext{" +
               "config=" + config +
               ", metadata=" + metadata +
               ", state=" + state +
               ", outputLength=" + output.length() +
               '}';
    }
}