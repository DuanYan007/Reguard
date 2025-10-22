package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.impl.MarkdownEngineImpl;

/**
 * Factory class for creating MarkdownEngine instances.
 * Provides various factory methods for different use cases.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownEngineFactory {

    private static MarkdownEngine defaultEngine;

    /**
     * Creates a new MarkdownEngine instance with default configuration.
     *
     * @return new MarkdownEngine instance
     */
    public static MarkdownEngine createEngine() {
        return new MarkdownEngineImpl();
    }

    /**
     * Creates a new MarkdownEngine instance with specified configuration.
     *
     * @param config the configuration to use
     * @return new MarkdownEngine instance
     */
    public static MarkdownEngine createEngine(MarkdownConfig config) {
        return new MarkdownEngineImpl(config);
    }

    /**
     * Gets a singleton default engine instance.
     * This method creates a thread-safe singleton that can be shared across the application.
     *
     * @return default MarkdownEngine instance
     */
    public static MarkdownEngine getDefaultEngine() {
        if (defaultEngine == null) {
            synchronized (MarkdownEngineFactory.class) {
                if (defaultEngine == null) {
                    defaultEngine = new MarkdownEngineImpl();
                }
            }
        }
        return defaultEngine;
    }

    /**
     * Creates a new MarkdownEngine instance optimized for table rendering.
     *
     * @return MarkdownEngine configured for tables
     */
    public static MarkdownEngine createTableOptimizedEngine() {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(true)
                .tableFormat("github")
                .sortMapKeys(true)
                .build();
        return new MarkdownEngineImpl(config);
    }

    /**
     * Creates a new MarkdownEngine instance optimized for simple text output.
     *
     * @return MarkdownEngine configured for simple text
     */
    public static MarkdownEngine createSimpleTextEngine() {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(false)
                .includeMetadata(false)
                .escapeHtml(true)
                .listStyle("dash")
                .build();
        return new MarkdownEngineImpl(config);
    }

    /**
     * Creates a new MarkdownEngine instance with rich formatting enabled.
     *
     * @return MarkdownEngine configured for rich formatting
     */
    public static MarkdownEngine createRichFormattingEngine() {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(true)
                .tableFormat("github")
                .listStyle("dash")
                .headingStyle("atx")
                .wrapCodeBlocks(true)
                .customOption("useEmoji", true)
                .build();
        return new MarkdownEngineImpl(config);
    }

    /**
     * Creates a new MarkdownEngine instance configured for API documentation.
     *
     * @return MarkdownEngine configured for API docs
     */
    public static MarkdownEngine createApiDocEngine() {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(true)
                .tableFormat("github")
                .includeMetadata(true)
                .wrapCodeBlocks(true)
                .sortMapKeys(true)
                .dateFormat("yyyy-MM-dd")
                .build();
        return new MarkdownEngineImpl(config);
    }

    /**
     * Resets the default engine singleton.
     * This can be useful for testing or when you need to reconfigure the default engine.
     */
    public static void resetDefaultEngine() {
        synchronized (MarkdownEngineFactory.class) {
            defaultEngine = null;
        }
    }

    // Private constructor to prevent instantiation
    private MarkdownEngineFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}