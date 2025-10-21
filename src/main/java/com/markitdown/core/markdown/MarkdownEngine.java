package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
import java.util.Map;

/**
 * Core interface for converting various objects to Markdown format.
 * This engine provides a unified way to generate Markdown content regardless of source format.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MarkdownEngine {

    /**
     * Converts an object to Markdown format.
     *
     * @param content the object to convert
     * @param options conversion options
     * @return Markdown formatted string
     */
    String convert(Object content, ConversionOptions options);

    /**
     * Converts an object to Markdown with metadata.
     *
     * @param content the object to convert
     * @param metadata document metadata
     * @param options conversion options
     * @return Markdown formatted string with metadata included
     */
    String convertWithMetadata(Object content, Map<String, Object> metadata, ConversionOptions options);

    /**
     * Validates if a string contains valid Markdown syntax.
     *
     * @param markdown the markdown string to validate
     * @return true if valid, false otherwise
     */
    boolean isValidMarkdown(String markdown);

    /**
     * Creates a new markdown builder for building complex documents.
     *
     * @return a new MarkdownBuilder instance
     */
    MarkdownBuilder createBuilder();

    /**
     * Registers a custom renderer for specific object types.
     *
     * @param objectType the class type this renderer handles
     * @param renderer the renderer implementation
     * @param <T> the object type
     */
    <T> void registerRenderer(Class<T> objectType, ObjectRenderer<T> renderer);

    /**
     * Gets information about the engine capabilities.
     *
     * @return engine information
     */
    EngineInfo getEngineInfo();
}