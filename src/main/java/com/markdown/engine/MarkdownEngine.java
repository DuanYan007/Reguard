package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;

import java.util.Map;

/**
 * Core interface for converting various objects to Markdown format.
 * This engine provides a unified way to generate Markdown content regardless of source format.
 * Completely independent from document conversion concerns.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MarkdownEngine {

    /**
     * Converts an object to Markdown format using default configuration.
     *
     * @param content the object to convert
     * @return Markdown formatted string
     */
    String convert(Object content);

    /**
     * Converts an object to Markdown format with specified configuration.
     *
     * @param content the object to convert
     * @param config  rendering configuration
     * @return Markdown formatted string
     */
    String convert(Object content, MarkdownConfig config);

    /**
     * Converts an object to Markdown with metadata using default configuration.
     *
     * @param content  the object to convert
     * @param metadata document metadata
     * @return Markdown formatted string with metadata included
     */
    String convertWithMetadata(Object content, Map<String, Object> metadata);

    /**
     * Converts an object to Markdown with metadata and configuration.
     *
     * @param content  the object to convert
     * @param metadata document metadata
     * @param config   rendering configuration
     * @return Markdown formatted string with metadata included
     */
    String convertWithMetadata(Object content, Map<String, Object> metadata, MarkdownConfig config);

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
     * Creates a new markdown builder with specified configuration.
     *
     * @param config rendering configuration
     * @return a new MarkdownBuilder instance
     */
    MarkdownBuilder createBuilder(MarkdownConfig config);

    /**
     * Registers a custom renderer for specific object types.
     *
     * @param objectType the class type this renderer handles
     * @param renderer   the renderer implementation
     * @param <T>         the object type
     */
    <T> void registerRenderer(Class<T> objectType, ObjectRenderer<T> renderer);

    /**
     * Unregisters a renderer for a specific object type.
     *
     * @param objectType the class type
     */
    void unregisterRenderer(Class<?> objectType);

    /**
     * Checks if a renderer is registered for a specific object type.
     *
     * @param objectType the class type
     * @return true if a renderer is registered
     */
    boolean hasRenderer(Class<?> objectType);

    /**
     * Gets information about the engine capabilities.
     *
     * @return engine information
     */
    EngineInfo getEngineInfo();

    /**
     * Creates a new RenderContext with the given configuration.
     *
     * @param config rendering configuration
     * @return new render context
     */
    default RenderContext createContext(MarkdownConfig config) {
        return new RenderContext(config);
    }

    /**
     * Creates a new RenderContext with the given configuration and metadata.
     *
     * @param config   rendering configuration
     * @param metadata document metadata
     * @return new render context
     */
    default RenderContext createContext(MarkdownConfig config, Map<String, Object> metadata) {
        return new RenderContext(config, metadata);
    }
}