package com.markitdown.core.markdown;

import com.markitdown.config.ConversionOptions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and configuring MarkdownEngine instances.
 * Provides singleton pattern for engine management.
 *
 * @author duan yan
 * @version 1.0.0
 * @since 1.0.0
 */
public class MarkdownEngineFactory {

    private static volatile MarkdownEngine defaultInstance;
    private static final Map<String, MarkdownEngine> engines = new ConcurrentHashMap<>();

    /**
     * Gets the default markdown engine instance.
     *
     * @return default engine
     */
    public static MarkdownEngine getDefaultEngine() {
        if (defaultInstance == null) {
            synchronized (MarkdownEngineFactory.class) {
                if (defaultInstance == null) {
                    defaultInstance = new MarkdownEngineImpl();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * Creates a new markdown engine instance.
     *
     * @return new engine
     */
    public static MarkdownEngine createEngine() {
        return new MarkdownEngineImpl();
    }

    /**
     * Gets or creates a named engine instance.
     *
     * @param engineName name of the engine
     * @return engine instance
     */
    public static MarkdownEngine getEngine(String engineName) {
        return engines.computeIfAbsent(engineName, name -> {
            // For now, return default engine for any name
            // Future: could create different engine configurations
            return new MarkdownEngineImpl();
        });
    }

    /**
     * Registers a custom engine instance.
     *
     * @param name name to register engine under
     * @param engine engine instance
     */
    public static void registerEngine(String name, MarkdownEngine engine) {
        if (name != null && engine != null) {
            engines.put(name, engine);
        }
    }

    /**
     * Removes a registered engine.
     *
     * @param name name of engine to remove
     * @return removed engine, or null if not found
     */
    public static MarkdownEngine removeEngine(String name) {
        return engines.remove(name);
    }

    /**
     * Gets information about all registered engines.
     *
     * @return map of engine names to their information
     */
    public static Map<String, EngineInfo> getEngineInfos() {
        Map<String, EngineInfo> infos = new ConcurrentHashMap<>();
        for (Map.Entry<String, MarkdownEngine> entry : engines.entrySet()) {
            infos.put(entry.getKey(), entry.getValue().getEngineInfo());
        }
        return infos;
    }

    /**
     * Clears all registered engines.
     */
    public static void clearEngines() {
        engines.clear();
    }

    /**
     * Converts an object using the default engine.
     *
     * @param content object to convert
     * @param options conversion options
     * @return markdown string
     */
    public static String convert(Object content, ConversionOptions options) {
        return getDefaultEngine().convert(content, options);
    }

    /**
     * Converts an object with metadata using the default engine.
     *
     * @param content object to convert
     * @param metadata document metadata
     * @param options conversion options
     * @return markdown string with metadata
     */
    public static String convertWithMetadata(Object content, Map<String, Object> metadata, ConversionOptions options) {
        return getDefaultEngine().convertWithMetadata(content, metadata, options);
    }

    /**
     * Validates markdown syntax using the default engine.
     *
     * @param markdown markdown string to validate
     * @return true if valid
     */
    public static boolean validateMarkdown(String markdown) {
        return getDefaultEngine().isValidMarkdown(markdown);
    }
}