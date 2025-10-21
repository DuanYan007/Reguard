package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
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
public class MarkdownContext {

    private final ConversionOptions options;
    private final Map<String, Object> metadata;
    private final Map<String, Object> state;
    private final StringBuilder output;

    /**
     * Creates a new markdown context.
     *
     * @param options conversion options
     * @param metadata document metadata
     */
    public MarkdownContext(ConversionOptions options, Map<String, Object> metadata) {
        this.options = options != null ? options : new ConversionOptions();
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
     * Gets the conversion options.
     *
     * @return conversion options
     */
    public ConversionOptions getOptions() {
        return options;
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
     * @param key the state key
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
     * @param key the state key
     * @param type the expected type
     * @param <T> the type parameter
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

    /**
     * Checks if metadata should be included in output.
     *
     * @return true if metadata should be included
     */
    public boolean shouldIncludeMetadata() {
        return options.isIncludeMetadata();
    }

    /**
     * Checks if tables should be included in output.
     *
     * @return true if tables should be included
     */
    public boolean shouldIncludeTables() {
        return options.isIncludeTables();
    }

    /**
     * Checks if images should be included in output.
     *
     * @return true if images should be included
     */
    public boolean shouldIncludeImages() {
        return options.isIncludeImages();
    }

    /**
     * Gets the table format to use.
     *
     * @return table format string
     */
    public String getTableFormat() {
        return options.getTableFormat();
    }

    /**
     * Gets the image format to use.
     *
     * @return image format string
     */
    public String getImageFormat() {
        return options.getImageFormat();
    }

    /**
     * Gets the language for OCR operations.
     *
     * @return language code
     */
    public String getLanguage() {
        return options.getLanguage();
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
    public MarkdownContext append(String text) {
        output.append(text);
        return this;
    }

    /**
     * Appends a newline to the output buffer.
     *
     * @return this context for method chaining
     */
    public MarkdownContext newline() {
        output.append(System.lineSeparator());
        return this;
    }

    /**
     * Appends multiple newlines to the output buffer.
     *
     * @param count number of newlines to append
     * @return this context for method chaining
     */
    public MarkdownContext newline(int count) {
        for (int i = 0; i < count; i++) {
            output.append(System.lineSeparator());
        }
        return this;
    }
}